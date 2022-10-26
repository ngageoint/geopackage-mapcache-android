package mil.nga.mapcache;

import android.content.Context;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import mil.nga.geopackage.extension.nga.style.FeatureStyle;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.map.features.StyleCache;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeType;
import mil.nga.geopackage.map.geom.MultiLatLng;
import mil.nga.geopackage.map.geom.MultiMarker;
import mil.nga.geopackage.map.geom.MultiPolygon;
import mil.nga.geopackage.map.geom.MultiPolygonOptions;
import mil.nga.geopackage.map.geom.MultiPolyline;
import mil.nga.geopackage.map.geom.MultiPolylineOptions;

/**
 * Prepares the shapes and its options.
 */
public class ShapeHelper {

    /**
     * The instance of this class.
     */
    private static final ShapeHelper instance = new ShapeHelper();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static ShapeHelper getInstance() {
        return instance;
    }

    /**
     * Prepare the shape options
     *
     * @param shape      map shape
     * @param styleCache style cache
     * @param featureRow feature row
     * @param editable   editable flag
     * @param topLevel   top level flag
     * @param context    The application context.
     */
    public void prepareShapeOptions(GoogleMapShape shape, StyleCache styleCache, FeatureRow featureRow, boolean editable,
                                    boolean topLevel, Context context) {

        FeatureStyle featureStyle = null;
        if (styleCache != null) {
            featureStyle = styleCache.getFeatureStyleExtension().getFeatureStyle(featureRow, shape.getGeometryType());
        }

        switch (shape.getShapeType()) {

            case LAT_LNG:
                LatLng latLng = (LatLng) shape.getShape();
                MarkerOptions markerOptions = getMarkerOptions(styleCache, featureStyle, editable, topLevel, context);
                markerOptions.position(latLng);
                shape.setShape(markerOptions);
                shape.setShapeType(GoogleMapShapeType.MARKER_OPTIONS);
                break;

            case POLYLINE_OPTIONS:
                PolylineOptions polylineOptions = (PolylineOptions) shape
                        .getShape();
                setPolylineOptions(styleCache, featureStyle, editable, polylineOptions, context);
                break;

            case POLYGON_OPTIONS:
                PolygonOptions polygonOptions = (PolygonOptions) shape.getShape();
                setPolygonOptions(styleCache, featureStyle, editable, polygonOptions, context);
                break;

            case MULTI_LAT_LNG:
                MultiLatLng multiLatLng = (MultiLatLng) shape.getShape();
                MarkerOptions sharedMarkerOptions = getMarkerOptions(styleCache, featureStyle, editable,
                        false, context);
                multiLatLng.setMarkerOptions(sharedMarkerOptions);
                break;

            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shape
                        .getShape();
                PolylineOptions sharedPolylineOptions = new PolylineOptions();
                setPolylineOptions(styleCache, featureStyle, editable, sharedPolylineOptions, context);
                multiPolylineOptions.setOptions(sharedPolylineOptions);
                break;

            case MULTI_POLYGON_OPTIONS:
                MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shape
                        .getShape();
                PolygonOptions sharedPolygonOptions = new PolygonOptions();
                setPolygonOptions(styleCache, featureStyle, editable, sharedPolygonOptions, context);
                multiPolygonOptions.setOptions(sharedPolygonOptions);
                break;

            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shape
                        .getShape();
                for (int i = 0; i < shapes.size(); i++) {
                    prepareShapeOptions(shapes.get(i), styleCache, featureRow, editable, false, context);
                }
                break;
            default:
        }
    }

    /**
     * Set the Polyline Option attributes
     *
     * @param styleCache      style cache
     * @param featureStyle    feature style
     * @param editable        editable flag
     * @param polylineOptions polyline options
     * @param context         The application context.
     */
    private void setPolylineOptions(StyleCache styleCache, FeatureStyle featureStyle, boolean editable,
                                    PolylineOptions polylineOptions, Context context) {
        if (editable) {
            polylineOptions.color(ContextCompat.getColor(context, R.color.polyline_edit_color));
        } else if (styleCache == null || !styleCache.setFeatureStyle(polylineOptions, featureStyle)) {
            polylineOptions.color(ContextCompat.getColor(context, R.color.polyline_color));
        }
    }

    /**
     * Set the Polygon Option attributes
     *
     * @param styleCache     style cache
     * @param featureStyle   feature style
     * @param editable       True if it should be displayed as editable.
     * @param polygonOptions The polygon options to set.
     * @param context        The application context.
     */
    private void setPolygonOptions(StyleCache styleCache, FeatureStyle featureStyle, boolean editable,
                                   PolygonOptions polygonOptions, Context context) {
        if (editable) {
            polygonOptions.strokeColor(ContextCompat.getColor(context, R.color.polygon_edit_color));
            polygonOptions.fillColor(ContextCompat.getColor(context, R.color.polygon_edit_fill_color));
        } else if (styleCache == null || !styleCache.setFeatureStyle(polygonOptions, featureStyle)) {
            polygonOptions.strokeColor(ContextCompat.getColor(context, R.color.polygon_color));
            polygonOptions.fillColor(ContextCompat.getColor(context, R.color.polygon_fill_color));
        }
    }

    /**
     * Get marker options
     *
     * @param styleCache   style cache
     * @param featureStyle feature style
     * @param editable     editable flag
     * @param clickable    clickable flag
     * @param context      The application context
     * @return marker options
     */
    private MarkerOptions getMarkerOptions(
            StyleCache styleCache,
            FeatureStyle featureStyle,
            boolean editable,
            boolean clickable,
            Context context) {
        MarkerOptions markerOptions = new MarkerOptions();
        if (editable) {
            TypedValue typedValue = new TypedValue();
            if (clickable) {
                context.getResources().getValue(R.dimen.marker_edit_color, typedValue,
                        true);
            } else {
                context.getResources().getValue(R.dimen.marker_edit_read_only_color,
                        typedValue, true);
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue
                    .getFloat()));

        } else if (styleCache == null || !styleCache.setFeatureStyle(markerOptions, featureStyle)) {

            TypedValue typedValue = new TypedValue();
            context.getResources().getValue(R.dimen.marker_color, typedValue, true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue.getFloat()));
        }

        return markerOptions;
    }

    /**
     * Add editable shape
     *
     * @param context   The application context.
     * @param map       The map to add the marker to.
     * @param model     The model used by the map.
     * @param featureId The id of the feature.
     * @param shape     The shape to add.
     * @return marker The google map marker to add.
     */
    public Marker addEditableShape(
            Context context,
            GoogleMap map,
            MapModel model,
            long featureId,
            GoogleMapShape shape) {

        Marker marker;

        if (shape.getShapeType() == GoogleMapShapeType.MARKER) {
            marker = (Marker) shape.getShape();
        } else {
            marker = getMarker(context, map, shape);
            if (marker != null) {
                model.getEditFeatureObjects().put(marker.getId(), shape);
            }
        }

        if (marker != null) {
            model.getEditFeatureIds().put(marker.getId(), featureId);
        }

        return marker;
    }

    /**
     * Get the first marker of the shape or create one at the location
     *
     * @param context The application context.
     * @param map     The map to add the marker to.
     * @param shape   The shape to get the marker for.
     * @return The marker to add to the map.
     */
    private Marker getMarker(Context context, GoogleMap map, GoogleMapShape shape) {

        Marker marker = null;

        switch (shape.getShapeType()) {

            case MARKER:
                Marker shapeMarker = (Marker) shape.getShape();
                marker = createEditMarker(context, map, shapeMarker.getPosition());
                break;

            case POLYLINE:
                Polyline polyline = (Polyline) shape.getShape();
                LatLng polylinePoint = polyline.getPoints().get(0);
                marker = createEditMarker(context, map, polylinePoint);
                break;

            case POLYGON:
                Polygon polygon = (Polygon) shape.getShape();
                LatLng polygonPoint = polygon.getPoints().get(0);
                marker = createEditMarker(context, map, polygonPoint);
                break;

            case MULTI_MARKER:
                MultiMarker multiMarker = (MultiMarker) shape.getShape();
                marker = createEditMarker(context, map, multiMarker.getMarkers().get(0)
                        .getPosition());
                break;

            case MULTI_POLYLINE:
                MultiPolyline multiPolyline = (MultiPolyline) shape.getShape();
                LatLng multiPolylinePoint = multiPolyline.getPolylines().get(0)
                        .getPoints().get(0);
                marker = createEditMarker(context, map, multiPolylinePoint);
                break;

            case MULTI_POLYGON:
                MultiPolygon multiPolygon = (MultiPolygon) shape.getShape();
                LatLng multiPolygonPoint = multiPolygon.getPolygons().get(0)
                        .getPoints().get(0);
                marker = createEditMarker(context, map, multiPolygonPoint);
                break;

            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shape
                        .getShape();
                for (GoogleMapShape listShape : shapes) {
                    marker = getMarker(context, map, listShape);
                    if (marker != null) {
                        break;
                    }
                }
                break;
            default:
        }

        return marker;
    }

    /**
     * Create an edit marker to edit polylines and polygons
     *
     * @param context The application context.
     * @param map     The map to add the marker to.
     * @param latLng  The latitude and longitude of the markers location.
     * @return The marker to add to the map.
     */
    private Marker createEditMarker(Context context, GoogleMap map, LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_edit));
        TypedValue typedValueWidth = new TypedValue();
        context.getResources().getValue(R.dimen.shape_edit_icon_anchor_width,
                typedValueWidth, true);
        TypedValue typedValueHeight = new TypedValue();
        context.getResources().getValue(R.dimen.shape_edit_icon_anchor_height,
                typedValueHeight, true);
        markerOptions.anchor(typedValueWidth.getFloat(),
                typedValueHeight.getFloat());
        return map.addMarker(markerOptions);
    }

    /**
     * Helps ensure a singleton.
     */
    private ShapeHelper() {

    }
}

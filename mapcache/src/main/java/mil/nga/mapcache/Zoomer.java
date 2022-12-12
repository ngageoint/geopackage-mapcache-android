package mil.nga.mapcache;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.map.MapUtils;
import mil.nga.geopackage.map.tiles.TileBoundingBoxMapUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.utils.ProjUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.ProjectionConstants;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;

/**
 * Controls zooming to various parts on the map.
 */
public class Zoomer {

    /**
     * Debug logging flag.
     */
    private static final boolean isDebug = false;

    /**
     * Contains the various states of the map.
     */
    private final MapModel model;

    /**
     * Contains the geoPackages.
     */
    private final GeoPackageViewModel geoPackageViewModel;

    /**
     * The application context.
     */
    private final Context context;

    /**
     * The map to zoom around.
     */
    private final GoogleMap map;

    /**
     * The view containing the map.
     */
    private final View mainView;

    /**
     * Constructor.
     *
     * @param model               Contains the various states of the map.
     * @param geoPackageViewModel Contains the geoPackages.
     * @param context             The application context.
     * @param map                 The map to zoom around.
     * @param mainView            The view containing the map.
     */
    public Zoomer(MapModel model, GeoPackageViewModel geoPackageViewModel, Context context, GoogleMap map, View mainView) {
        this.model = model;
        this.geoPackageViewModel = geoPackageViewModel;
        this.context = context;
        this.map = map;
        this.mainView = mainView;
    }

    /**
     * Zoom to the active feature and tile table data bounds
     */
    public void zoomToActiveBounds() {

        model.setFeaturesBoundingBox(null);
        model.setTilesBoundingBox(null);

        // Pre zoom
        List<GeoPackageDatabase> activeDatabases = new ArrayList<>(model.getActive().getDatabases());
        for (GeoPackageDatabase database : activeDatabases) {
            GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(database.getDatabase());
            if (geoPackage != null) {

                Set<String> featureTableDaos = new HashSet<>();
                Collection<GeoPackageFeatureTable> features = database.getFeatures();
                if (!features.isEmpty()) {
                    for (GeoPackageFeatureTable featureTable : features) {
                        featureTableDaos.add(featureTable.getName());
                    }
                }

                for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                    if (featureOverlay.isActive()) {
                        featureTableDaos.add(featureOverlay.getFeatureTable());
                    }
                }

                if (!featureTableDaos.isEmpty()) {
                    for (String featureTable : featureTableDaos) {
                        if (featureTable != null && !featureTable.isEmpty()) {
                            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                            String[] columns = featureDao.getIdAndGeometryColumnNames();
                            BoundingBox contentsBoundingBox = null;
                            try (FeatureCursor cursor = featureDao.query(columns)) {
                                List<BoundingBox> boxes = new ArrayList<>();
                                while (cursor.moveToNext()) {
                                    FeatureRow row = cursor.getRow();
                                    try {
                                        GeometryType type = row.getGeometryType();
                                        GeometryEnvelope envelope = row.getGeometry().buildEnvelope();
                                        BoundingBox rowBox = new BoundingBox(envelope);
                                        if (isDebug) {
                                            boxes.add(new BoundingBox(rowBox));
                                        }
                                        if (contentsBoundingBox == null) {
                                            contentsBoundingBox = rowBox;
                                        } else {
                                            contentsBoundingBox = contentsBoundingBox.union(rowBox);
                                        }
                                    } catch (Exception e){
                                        Log.e("Opening GeometryEnvelope: ", e.toString());
                                    }
                                }

                                for (BoundingBox box : boxes) {
                                    Log.d(Zoomer.class.getSimpleName(),
                                            "BBox "
                                                    + box.getMinLatitude() + " "
                                                    + box.getMinLongitude() + " "
                                                    + box.getMaxLatitude() + " "
                                                    + box.getMaxLongitude());
                                }

                                if (isDebug && contentsBoundingBox != null) {
                                    Log.d(Zoomer.class.getSimpleName(),
                                            "Contents BBox "
                                                    + contentsBoundingBox.getMinLatitude() + " "
                                                    + contentsBoundingBox.getMinLongitude() + " "
                                                    + contentsBoundingBox.getMaxLatitude() + " "
                                                    + contentsBoundingBox.getMaxLongitude());
                                }
                            }

                            if (contentsBoundingBox != null) {
                                contentsBoundingBox = ProjUtils.getInstance()
                                        .transformBoundingBoxToWgs84(
                                                contentsBoundingBox,
                                                featureDao.getSrs());

                                if (model.getFeaturesBoundingBox() != null) {
                                    model.setFeaturesBoundingBox(model.getFeaturesBoundingBox().union(contentsBoundingBox));
                                } else {
                                    model.setFeaturesBoundingBox(contentsBoundingBox);
                                }
                            }
                        }
                    }
                }

                Collection<GeoPackageTileTable> tileTables = database.getTiles();
                if (!tileTables.isEmpty()) {

                    TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

                    for (GeoPackageTileTable tileTable : tileTables) {

                        try {
                            TileMatrixSet tileMatrixSet = tileMatrixSetDao.queryForId(tileTable.getName());
                            BoundingBox tileMatrixSetBoundingBox = tileMatrixSet.getContents().getBoundingBox();

                            tileMatrixSetBoundingBox = ProjUtils.getInstance()
                                    .transformBoundingBoxToWgs84(
                                            tileMatrixSetBoundingBox,
                                            tileMatrixSet.getSrs());

                            if (model.getTilesBoundingBox() != null) {
                                model.setTilesBoundingBox(model.getTilesBoundingBox().union(tileMatrixSetBoundingBox));
                            } else {
                                model.setTilesBoundingBox(tileMatrixSetBoundingBox);
                            }
                        } catch (SQLException e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    e.getMessage());
                        }
                    }
                }
            }
        }

        zoomToActive();
    }

    /**
     * Zoom to features on the map, or tiles if no features
     */
    public void zoomToActive() {
        zoomToActive(false);
    }

    /**
     * Zoom to features on the map, or tiles if no features
     *
     * @param nothingVisible zoom only if nothing is currently visible
     */
    public void zoomToActive(boolean nothingVisible) {

        BoundingBox bbox = model.getFeaturesBoundingBox();
        boolean tileBox = false;

        float paddingPercentage;
        if (bbox == null) {
            bbox = model.getTilesBoundingBox();
            tileBox = true;
            if (model.isFeatureOverlayTiles()) {
                paddingPercentage = context.getResources().getInteger(
                        R.integer.map_feature_tiles_zoom_padding_percentage) * .01f;
            } else {
                paddingPercentage = context.getResources().getInteger(
                        R.integer.map_tiles_zoom_padding_percentage) * .01f;
            }
        } else {
            paddingPercentage = context.getResources().getInteger(
                    R.integer.map_features_zoom_padding_percentage) * .01f;
        }

        if (bbox != null) {

            boolean zoomToActive = true;
            if (nothingVisible) {
                BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
                if (TileBoundingBoxUtils.overlap(bbox, mapViewBoundingBox, ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH) != null) {

                    double longitudeDistance = TileBoundingBoxMapUtils.getLongitudeDistance(bbox);
                    double latitudeDistance = TileBoundingBoxMapUtils.getLatitudeDistance(bbox);
                    double mapViewLongitudeDistance = TileBoundingBoxMapUtils.getLongitudeDistance(mapViewBoundingBox);
                    double mapViewLatitudeDistance = TileBoundingBoxMapUtils.getLatitudeDistance(mapViewBoundingBox);

                    if (mapViewLongitudeDistance > longitudeDistance && mapViewLatitudeDistance > latitudeDistance) {

                        double longitudeRatio = longitudeDistance / mapViewLongitudeDistance;
                        double latitudeRatio = latitudeDistance / mapViewLatitudeDistance;

                        double zoomAlreadyVisiblePercentage;
                        if (tileBox) {
                            zoomAlreadyVisiblePercentage = context.getResources().getInteger(
                                    R.integer.map_tiles_zoom_already_visible_percentage) * .01f;
                        } else {
                            zoomAlreadyVisiblePercentage = context.getResources().getInteger(
                                    R.integer.map_features_zoom_already_visible_percentage) * .01f;
                        }

                        if (longitudeRatio >= zoomAlreadyVisiblePercentage && latitudeRatio >= zoomAlreadyVisiblePercentage) {
                            zoomToActive = false;
                        }
                    }
                }
            }

            if (zoomToActive) {
                double minLatitude = Math.max(bbox.getMinLatitude(), ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE);
                double maxLatitude = Math.min(bbox.getMaxLatitude(), ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);

                LatLng lowerLeft = new LatLng(minLatitude, bbox.getMinLongitude());
                LatLng lowerRight = new LatLng(minLatitude, bbox.getMaxLongitude());
                LatLng topLeft = new LatLng(maxLatitude, bbox.getMinLongitude());
                LatLng topRight = new LatLng(maxLatitude, bbox.getMaxLongitude());

                if (lowerLeft.longitude == lowerRight.longitude) {
                    double adjustLongitude = lowerRight.longitude - .0000000000001;
                    lowerRight = new LatLng(minLatitude, adjustLongitude);
                    topRight = new LatLng(maxLatitude, adjustLongitude);
                }

                final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(lowerLeft);
                boundsBuilder.include(lowerRight);
                boundsBuilder.include(topLeft);
                boundsBuilder.include(topRight);

                int minViewLength = mainView != null ? Math.min(mainView.getWidth(), mainView.getHeight()) : 1;
                final int padding = (int) Math.floor(minViewLength
                        * paddingPercentage);

                try {
                    LatLngBounds bounds = boundsBuilder.build();
                    if (isDebug) {
                        Log.d(Zoomer.class.getSimpleName(), "LatLngBounds  "
                                + bounds.southwest.latitude + " "
                                + bounds.southwest.longitude + " "
                                + bounds.northeast.latitude + " "
                                + bounds.northeast.longitude);
                    }
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            bounds, padding));
                } catch (Exception e) {
                    Log.w(GeoPackageMapFragment.class.getSimpleName(),
                            "Unable to move camera", e);
                }
            }
        }
    }
}

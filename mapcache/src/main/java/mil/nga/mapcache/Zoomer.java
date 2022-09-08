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

public class Zoomer {

    private MapModel model;

    private GeoPackageViewModel geoPackageViewModel;

    private Context context;

    private GoogleMap map;

    private View mainView;

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

        model.featuresBoundingBox = null;
        model.tilesBoundingBox = null;

        // Pre zoom
        List<GeoPackageDatabase> activeDatabases = new ArrayList<>(model.active.getDatabases());
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
                                while (cursor.moveToNext()) {
                                    FeatureRow row = cursor.getRow();
                                    GeometryEnvelope envelope =  row.getGeometry().buildEnvelope();
                                    BoundingBox rowBox = new BoundingBox(envelope);
                                    if(contentsBoundingBox == null) {
                                        contentsBoundingBox = rowBox;
                                    } else {
                                        contentsBoundingBox.union(rowBox);
                                    }
                                }
                            }

                            if (contentsBoundingBox != null) {
                                contentsBoundingBox = ProjUtils.getInstance()
                                        .transformBoundingBoxToWgs84(
                                                contentsBoundingBox,
                                                featureDao.getSrs());

                                if (model.featuresBoundingBox != null) {
                                    model.featuresBoundingBox = model.featuresBoundingBox.union(contentsBoundingBox);
                                } else {
                                    model.featuresBoundingBox = contentsBoundingBox;
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
                            BoundingBox tileMatrixSetBoundingBox = tileMatrixSet.getBoundingBox();

                            tileMatrixSetBoundingBox = ProjUtils.getInstance()
                                    .transformBoundingBoxToWgs84(
                                            tileMatrixSetBoundingBox,
                                            tileMatrixSet.getSrs());

                            if (model.tilesBoundingBox != null) {
                                model.tilesBoundingBox = model.tilesBoundingBox.union(tileMatrixSetBoundingBox);
                            } else {
                                model.tilesBoundingBox = tileMatrixSetBoundingBox;
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

        BoundingBox bbox = model.featuresBoundingBox;
        boolean tileBox = false;

        float paddingPercentage = 0f;
        if (bbox == null) {
            bbox = model.tilesBoundingBox;
            tileBox = true;
            if (model.featureOverlayTiles) {
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
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(), padding));
                } catch (Exception e) {
                    Log.w(GeoPackageMapFragment.class.getSimpleName(),
                            "Unable to move camera", e);
                }
            }
        }
    }
}

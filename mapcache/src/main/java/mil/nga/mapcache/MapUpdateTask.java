package mil.nga.mapcache;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.extension.nga.link.FeatureTileTableLinker;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.extension.nga.scale.TileTableScaling;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.map.tiles.overlay.BoundedOverlay;
import mil.nga.geopackage.map.tiles.overlay.FeatureOverlay;
import mil.nga.geopackage.map.tiles.overlay.FeatureOverlayQuery;
import mil.nga.geopackage.map.tiles.overlay.GeoPackageOverlayFactory;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.utils.ProjUtils;
import mil.nga.mapcache.utils.ThreadUtils;
import mil.nga.mapcache.view.map.BasemapApplier;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;

/**
 * Update the map in the background
 */
public class MapUpdateTask implements Runnable {

    /**
     * The main activity.
     */
    private final Activity activity;

    /**
     * The map to update.
     */
    private final GoogleMap map;

    /**
     * The applies the selected basemap to the map.
     */
    private final BasemapApplier basemapApplier;

    /**
     * The model used by the map.
     */
    private final MapModel model;

    /**
     * Contains all the geoPackages.
     */
    private final GeoPackageViewModel geoPackageViewModel;

    /**
     * Indicates if this task has been cancelled.
     */
    private boolean isCancelled = false;

    /**
     * Notified when this task is done.
     */
    private Runnable finishListener;

    /**
     * Constructor.
     *
     * @param activity            The main activity.
     * @param map                 The map to update.
     * @param basemapApplier      The applies the selected basemap to the map.
     * @param model               The model used by the map.
     * @param geoPackageViewModel Contains all the geoPackages.
     */
    public MapUpdateTask(
            Activity activity,
            GoogleMap map,
            BasemapApplier basemapApplier,
            MapModel model,
            GeoPackageViewModel geoPackageViewModel) {
        this.activity = activity;
        this.map = map;
        this.basemapApplier = basemapApplier;
        this.model = model;
        this.geoPackageViewModel = geoPackageViewModel;
    }

    /**
     * Runs this task within a background thread.
     */
    public void execute() {
        ThreadUtils.getInstance().runBackground(this);
    }

    /**
     * Cancels the running of this task.
     */
    public void cancel() {
        this.isCancelled = true;
    }

    /**
     * Sets the finish listener.
     *
     * @param listener The object to be notified when this task is done.
     */
    public void setFinishListener(Runnable listener) {
        this.finishListener = listener;
    }

    @Override
    public void run() {
        update();
        activity.runOnUiThread(() -> basemapApplier.applyBasemaps(map));
        if(finishListener != null) {
            finishListener.run();
        }
    }

    /**
     * Update the map
     */
    private void update() {

        if (model.getActive() != null) {

            // Open active GeoPackages and create feature DAOS, display tiles and feature tiles
            List<GeoPackageDatabase> activeDatabases = new ArrayList<>(model.getActive().getDatabases());
            for (GeoPackageDatabase database : activeDatabases) {

                if (this.isCancelled) {
                    break;
                }

                try {
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
                            Map<String, FeatureDao> databaseFeatureDaos = new HashMap<>();
                            model.getFeatureDaos().put(database.getDatabase(), databaseFeatureDaos);
                            for (String featureTable : featureTableDaos) {

                                if (this.isCancelled) {
                                    break;
                                }

                                FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                                databaseFeatureDaos.put(featureTable, featureDao);
                            }
                        }

                        // Display the tiles
                        for (GeoPackageTileTable tiles : database.getTiles()) {
                            if (this.isCancelled) {
                                break;
                            }
                            try {
                                displayTiles(tiles);
                            } catch (Exception e) {
                                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                        e.getMessage());
                            }
                        }

                        // Display the feature tiles
                        for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                            if (this.isCancelled) {
                                break;
                            }
                            if (featureOverlay.isActive()) {
                                try {
                                    displayFeatureTiles(featureOverlay);
                                } catch (Exception e) {
                                    Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                            e.getMessage());
                                }
                            }
                        }

                    } else {
                        model.getActive().removeDatabase(database.getDatabase(), false);
                    }
                } catch (Exception e) {
                    Log.e(GeoPackageMapFragment.class.getSimpleName(), "Error opening geopackage: " + database.getDatabase(), e);
                }
            }
        }

    }

    /**
     * Display tiles
     *
     * @param tiles The tiles to display.
     */
    private void displayTiles(GeoPackageTileTable tiles) {

        GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(tiles.getDatabase());

        TileDao tileDao = geoPackage.getTileDao(tiles.getName());

        TileTableScaling tileTableScaling = new TileTableScaling(geoPackage, tileDao);
        TileScaling tileScaling = tileTableScaling.get();

        BoundedOverlay overlay = GeoPackageOverlayFactory
                .getBoundedOverlay(tileDao, activity.getResources().getDisplayMetrics().density, tileScaling);

        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();

        FeatureTileTableLinker linker = new FeatureTileTableLinker(geoPackage);
        List<FeatureDao> featureDaos = linker.getFeatureDaosForTileTable(tileDao.getTableName());

        for (FeatureDao featureDao : featureDaos) {

            // Create the feature tiles
            FeatureTiles featureTiles = new DefaultFeatureTiles(activity, geoPackage, featureDao,
                    activity.getResources().getDisplayMetrics().density);

            model.setFeatureOverlayTiles(true);

            // Add the feature overlay query
            FeatureOverlayQuery featureOverlayQuery = new FeatureOverlayQuery(activity, overlay, featureTiles);
            featureOverlayQuery.calculateStylePixelBounds();
            model.getFeatureOverlayQueries().add(featureOverlayQuery);
        }

        // Set the tiles index to be -2 of it is behind features and tiles drawn from features
        int zIndex = -2;

        // If these tiles are linked to features, set the zIndex to -1 so they are placed before imagery tiles
        if (!featureDaos.isEmpty()) {
            zIndex = -1;
        }

        BoundingBox displayBoundingBox = tileMatrixSet.getBoundingBox();
        Contents contents = tileMatrixSet.getContents();
        BoundingBox contentsBoundingBox = contents.getBoundingBox();
        if (contentsBoundingBox != null) {
            ProjectionTransform transform = contents.getSrs().getProjection().getTransformation(tileMatrixSet.getSrs().getProjection());
            BoundingBox transformedContentsBoundingBox = contentsBoundingBox;
            if (!transform.isSameProjection()) {
                transformedContentsBoundingBox = transformedContentsBoundingBox.transform(transform);
            }
            displayBoundingBox = displayBoundingBox.overlap(transformedContentsBoundingBox);
        }

        displayTiles(overlay, displayBoundingBox, tileMatrixSet.getSrs(), zIndex, null);
    }

    /**
     * Display feature tiles
     *
     * @param featureOverlayTable The overlay table to display.
     */
    private void displayFeatureTiles(GeoPackageFeatureOverlayTable featureOverlayTable) {

        GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(featureOverlayTable.getDatabase());
        Map<String, FeatureDao> daos = model.getFeatureDaos().get(featureOverlayTable.getDatabase());
        if (daos != null) {
            FeatureDao featureDao = daos.get(featureOverlayTable.getFeatureTable());

            BoundingBox boundingBox = new BoundingBox(featureOverlayTable.getMinLon(),
                    featureOverlayTable.getMinLat(), featureOverlayTable.getMaxLon(), featureOverlayTable.getMaxLat());

            // Load tiles
            FeatureTiles featureTiles = new DefaultFeatureTiles(activity, geoPackage, featureDao,
                    activity.getResources().getDisplayMetrics().density);
            if (featureOverlayTable.isIgnoreGeoPackageStyles()) {
                featureTiles.ignoreFeatureTableStyles();
            }

            featureTiles.setMaxFeaturesPerTile(featureOverlayTable.getMaxFeaturesPerTile());
            if (featureOverlayTable.getMaxFeaturesPerTile() != null) {
                featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(activity));
            }

            Paint pointPaint = featureTiles.getPointPaint();
            pointPaint.setColor(Color.parseColor(featureOverlayTable.getPointColor()));

            pointPaint.setAlpha(featureOverlayTable.getPointAlpha());
            featureTiles.setPointRadius(featureOverlayTable.getPointRadius());

            Paint linePaint = featureTiles.getLinePaintCopy();
            linePaint.setColor(Color.parseColor(featureOverlayTable.getLineColor()));

            linePaint.setAlpha(featureOverlayTable.getLineAlpha());
            linePaint.setStrokeWidth(featureOverlayTable.getLineStrokeWidth());
            featureTiles.setLinePaint(linePaint);

            Paint polygonPaint = featureTiles.getPolygonPaintCopy();
            polygonPaint.setColor(Color.parseColor(featureOverlayTable.getPolygonColor()));

            polygonPaint.setAlpha(featureOverlayTable.getPolygonAlpha());
            polygonPaint.setStrokeWidth(featureOverlayTable.getPolygonStrokeWidth());
            featureTiles.setPolygonPaint(polygonPaint);

            featureTiles.setFillPolygon(featureOverlayTable.isPolygonFill());
            if (featureTiles.isFillPolygon()) {
                Paint polygonFillPaint = featureTiles.getPolygonFillPaintCopy();
                polygonFillPaint.setColor(Color.parseColor(featureOverlayTable.getPolygonFillColor()));

                polygonFillPaint.setAlpha(featureOverlayTable.getPolygonFillAlpha());
                featureTiles.setPolygonFillPaint(polygonFillPaint);
            }

            featureTiles.calculateDrawOverlap();

            FeatureOverlay featureOverlay = new FeatureOverlay(featureTiles);
            featureOverlay.setBoundingBox(boundingBox, ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM));
            featureOverlay.setMinZoom(featureOverlayTable.getMinZoom());
            featureOverlay.setMaxZoom(featureOverlayTable.getMaxZoom());

            // Get the tile linked overlay
            BoundedOverlay overlay = GeoPackageOverlayFactory.getLinkedFeatureOverlay(featureOverlay, geoPackage);

            if (featureDao != null) {
                GeometryColumns geometryColumns = featureDao.getGeometryColumns();
                Contents contents = geometryColumns.getContents();

                GeoPackageUtils.prepareFeatureTiles(featureTiles);

                model.setFeatureOverlayTiles(true);

                FeatureOverlayQuery featureOverlayQuery = new FeatureOverlayQuery(activity, overlay, featureTiles);
                featureOverlayQuery.calculateStylePixelBounds();
                model.getFeatureOverlayQueries().add(featureOverlayQuery);

                displayTiles(overlay, contents.getBoundingBox(), contents.getSrs(), -1, boundingBox);
            }
        }
    }

    /**
     * Display tiles
     *
     * @param overlay              The tile overlay.
     * @param dataBoundingBox      The bounding box of the data.
     * @param srs                  The spatial reference system of the tiles.
     * @param zIndex               The zoom level.
     * @param specifiedBoundingBox The specified bounding box.
     */
    private void displayTiles(TileProvider overlay, BoundingBox dataBoundingBox, SpatialReferenceSystem srs, int zIndex, BoundingBox specifiedBoundingBox) {

        final TileOverlayOptions overlayOptions = new TileOverlayOptions();
        overlayOptions.tileProvider(overlay);
        overlayOptions.zIndex(zIndex);

        BoundingBox boundingBox = dataBoundingBox;
        if (boundingBox != null) {
            boundingBox = ProjUtils.getInstance().transformBoundingBoxToWgs84(boundingBox, srs);
        } else {
            boundingBox = new BoundingBox(-ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
                    ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE,
                    ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
                    ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);
        }

        if (specifiedBoundingBox != null) {
            boundingBox = boundingBox.overlap(specifiedBoundingBox);
        }

        if (model.getTilesBoundingBox() == null) {
            model.setTilesBoundingBox(boundingBox);
        } else {
            model.setTilesBoundingBox(model.getTilesBoundingBox().union(boundingBox));
        }

        activity.runOnUiThread(() -> map.addTileOverlay(overlayOptions));
    }
}

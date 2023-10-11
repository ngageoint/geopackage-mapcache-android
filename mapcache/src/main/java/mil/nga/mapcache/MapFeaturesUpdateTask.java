package mil.nga.mapcache;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.locationtech.proj4j.units.Units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.MultipleFeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.map.features.StyleCache;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeConverter;
import mil.nga.geopackage.map.geom.GoogleMapShapeType;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.utils.ThreadUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.GeometryType;

/**
 * Update the map features in the background
 */
public class MapFeaturesUpdateTask implements Runnable {

    /**
     * The application context.
     */
    private final Activity activity;

    /**
     * The model used by the map.
     */
    private final MapModel model;

    /**
     * Contains the geoPackages.
     */
    private final GeoPackageViewModel geoPackageViewModel;

    /**
     * The map showing the geoPackages.
     */
    private final GoogleMap map;

    /**
     * Flag indicating if it was cancelled.
     */
    private boolean cancelled = false;

    /**
     * The maximum number of features.
     */
    private int maxFeatures;

    /**
     * The extent of the maps view.
     */
    private BoundingBox mapViewBoundingBox;

    /**
     * The tolerance to apply when filtering out features that are outside the maps view.
     */
    private double toleranceDistance;

    /**
     * Flag indicating if we should filter out records not in view.
     */
    private boolean filter;

    /**
     * Keep track of any errors when displaying features on the map
     */
    private int errorCount = 0;

    /**
     * Constructor.
     *
     * @param activity            The application activity.
     * @param map                 The map showing the geoPackages.
     * @param model               The model used by the map.
     * @param geoPackageViewModel Contains the geoPackages.
     */
    public MapFeaturesUpdateTask(Activity activity, GoogleMap map, MapModel model, GeoPackageViewModel geoPackageViewModel) {
        this.activity = activity;
        this.map = map;
        this.model = model;
        this.geoPackageViewModel = geoPackageViewModel;
    }

    /**
     * Add a shape to the map
     *
     * @param featureId The id of the feature.
     * @param database  The name of the geopackage.
     * @param tableName The name of the layer.
     * @param shape     The type of shape to add.
     */
    public void addToMap(long featureId, String database, String tableName, GoogleMapShape shape) {
        this.activity.runOnUiThread(() -> {
            synchronized (model.getFeatureShapes()) {

                if (NotCancelled() && !model.getFeatureShapes().exists(featureId, database, tableName)) {

                    GoogleMapShape mapShape = GoogleMapShapeConverter.addShapeToMap(
                            map, shape);

                    if (model.isEditFeaturesMode()) {
                        Marker marker = ShapeHelper.getInstance().addEditableShape(
                                activity, map, model, featureId, mapShape);
                        if (marker != null) {
                            GoogleMapShape mapPointShape = new GoogleMapShape(GeometryType.POINT, GoogleMapShapeType.MARKER, marker);
                            model.getFeatureShapes().addMapMetadataShape(mapPointShape, featureId, database, tableName);
                        }
                    } else {
                        addMarkerShape(featureId, database, tableName, mapShape);
                    }
                    model.getFeatureShapes().addMapShape(mapShape, featureId, database, tableName);
                }
            }
        });
    }

    /**
     * Cancels the task.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Indicates if this task has been cancelled.
     *
     * @return True if its been cancelled false if not cancelled.
     */
    public boolean NotCancelled() {
        return !cancelled;
    }

    /**
     * Performs this task on a background thread.
     *
     * @param maxFeatures        The maximum number of features.
     * @param mapViewBoundingBox The maps view extent.
     * @param toleranceDistance  The tolerance to apply when filtering out features that are outside the maps view.
     * @param filter             Flag indicating if we should filter out records not in view.
     */
    public void execute(int maxFeatures, BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {
        this.maxFeatures = maxFeatures;
        this.mapViewBoundingBox = mapViewBoundingBox;
        this.toleranceDistance = toleranceDistance;
        this.filter = filter;
        ThreadUtils.getInstance().runBackground(this);
    }

    /**
     * Add features to the map
     *
     * @param maxFeatures        max features
     * @param mapViewBoundingBox map view bounding box
     * @param toleranceDistance  tolerance distance
     * @param filter             filter
     */
    private void addFeatures(final int maxFeatures, BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {

        AtomicInteger count = new AtomicInteger();

        Map<String, List<String>> featureTables = new HashMap<>();
        if (model.isEditFeaturesMode()) {
            List<String> databaseFeatures = new ArrayList<>();
            databaseFeatures.add(model.getEditFeaturesTable());
            featureTables.put(model.getEditFeaturesDatabase(), databaseFeatures);
            GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
            Map<String, FeatureDao> databaseFeatureDaos = model.getFeatureDaos().get(model.getEditFeaturesDatabase());
            if (databaseFeatureDaos == null) {
                databaseFeatureDaos = new HashMap<>();
                model.getFeatureDaos().put(model.getEditFeaturesDatabase(), databaseFeatureDaos);
            }
            FeatureDao featureDao = databaseFeatureDaos.get(model.getEditFeaturesTable());
            if (featureDao == null) {
                featureDao = geoPackage.getFeatureDao(model.getEditFeaturesTable());
                databaseFeatureDaos.put(model.getEditFeaturesTable(), featureDao);
            }
        } else {
            for (GeoPackageDatabase database : model.getActive().getDatabases()) {
                if (!database.getFeatures().isEmpty()) {
                    List<String> databaseFeatures = new ArrayList<>();
                    featureTables.put(database.getDatabase(),
                            databaseFeatures);
                    for (GeoPackageTable features : database.getFeatures()) {
                        databaseFeatures.add(features.getName());
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> databaseFeaturesEntry : featureTables
                .entrySet()) {

            if (count.get() >= maxFeatures) {
                break;
            }

            String databaseName = databaseFeaturesEntry.getKey();

            List<String> databaseFeatures = databaseFeaturesEntry.getValue();
            Map<String, FeatureDao> databaseFeatureDaos = model.getFeatureDaos().get(databaseName);

            if (databaseFeatureDaos != null) {

                GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(databaseName);
                StyleCache styleCache = new StyleCache(geoPackage, activity.getResources().getDisplayMetrics().density);

                for (String features : databaseFeatures) {

                    if (databaseFeatureDaos.containsKey(features)) {

                        displayFeatures(geoPackage, styleCache, features, count,
                                maxFeatures, model.isEditFeaturesMode(), mapViewBoundingBox,
                                toleranceDistance, filter);
                        if (cancelled || count.get() >= maxFeatures) {
                            break;
                        }
                    }
                }

                styleCache.clear();
            }

            if (cancelled) {
                break;
            }
        }
    }

    /**
     * Display features
     *
     * @param geoPackage         The geopackage to display.
     * @param styleCache         the style cache.
     * @param features           The features.
     * @param count              The number of features.
     * @param maxFeatures        The maximum number of features the map will display.
     * @param editable           True if its editable.
     * @param mapViewBoundingBox The views bounding box.
     * @param toleranceDistance  Used to simplify geometries for performance.
     * @param filter             True if features should be filtered.
     */
    private void displayFeatures(GeoPackage geoPackage, StyleCache styleCache, String features,
                                 AtomicInteger count, final int maxFeatures, final boolean editable,
                                 BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {

        // Get the GeoPackage and feature DAO
        String database = geoPackage.getName();
        setErrorCount(0);

        Map<String, FeatureDao> dataAccessObjects = model.getFeatureDaos().get(database);
        if (dataAccessObjects != null) {
            FeatureDao featureDao = dataAccessObjects.get(features);
            if (featureDao != null) {
                GoogleMapShapeConverter converter = new GoogleMapShapeConverter(featureDao.getProjection());

                converter.setSimplifyTolerance(toleranceDistance);

                if (!styleCache.getFeatureStyleExtension().has(features)) {
                    styleCache = null;
                }

                count.getAndAdd(model.getFeatureShapes().getFeatureIdsCount(database, features));

                if (!cancelled && count.get() < maxFeatures) {

                    mil.nga.proj.Projection mapViewProjection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

                    String[] columns = featureDao.getIdAndGeometryColumnNames();

                    FeatureIndexManager indexer = new FeatureIndexManager(activity, geoPackage, featureDao);
                    if (filter && indexer.isIndexed()) {

                        FeatureIndexResults indexResults = indexer.query(columns, mapViewBoundingBox, mapViewProjection);
                        BoundingBox complementary = mapViewBoundingBox.complementaryWgs84();
                        if (complementary != null) {
                            FeatureIndexResults indexResults2 = indexer.query(columns, complementary, mapViewProjection);
                            indexResults = new MultipleFeatureIndexResults(indexResults, indexResults2);
                        }

                        processFeatureIndexResults(indexResults, database, featureDao, converter, styleCache,
                                count, maxFeatures, editable);

                    } else {

                        BoundingBox filterBoundingBox = null;
                        double filterMaxLongitude = 0;

                        if (filter) {
                            mil.nga.proj.Projection featureProjection = featureDao.getProjection();
                            ProjectionTransform projectionTransform = mapViewProjection.getTransformation(featureProjection);
                            BoundingBox boundedMapViewBoundingBox = mapViewBoundingBox.boundWgs84Coordinates();
                            BoundingBox transformedBoundingBox = boundedMapViewBoundingBox.transform(projectionTransform);
                            if (featureProjection.isUnit(Units.DEGREES)) {
                                filterMaxLongitude = ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH;
                            } else if (featureProjection.isUnit(Units.METRES)) {
                                filterMaxLongitude = ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH;
                            }
                            filterBoundingBox = transformedBoundingBox.expandCoordinates(filterMaxLongitude);
                        }

                        // Query for all rows
                        try (FeatureCursor cursor = featureDao.query(columns)) {
                            while (!cancelled && count.get() < maxFeatures
                                    && cursor.moveToNext()) {
                                try {
                                    FeatureRow row = cursor.getRow();

                                    // Process the feature row in the thread pool
                                    FeatureRowProcessor processor = new FeatureRowProcessor(
                                            this, database, featureDao, row, count, maxFeatures, editable, converter,
                                            styleCache, filterBoundingBox, filterMaxLongitude,
                                            filter, model, activity);
                                    ThreadUtils.getInstance().runBackground(processor);
                                } catch (Exception e) {
                                    Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                            "Failed to display feature. database: " + database
                                                    + ", feature table: " + features
                                                    + ", row: " + cursor.getPosition(), e);
                                }
                            }
                            int totalErrors = getErrorCount();
                            if(totalErrors > 0){
                                new Handler(Looper.getMainLooper()).post(() -> {
                                            Toast toast = Toast.makeText(activity, "Error loading geometry", Toast.LENGTH_SHORT);
                                            toast.show();
                                        });
                                setErrorCount(0);
                            }
                        }

                    }
                    indexer.close();

                }
            }
        }
    }

    /**
     * Process the feature index results
     *
     * @param indexResults The index results.
     * @param database     The geoPackage to process features for.
     * @param featureDao   The feature data access object.
     * @param converter    Convert the features shapes to those that can go on a google map.
     * @param styleCache   The style cache.
     * @param count        Keeps track of how many features we have added to the map.
     * @param maxFeatures  The maximum number of features we can add to the map.
     * @param editable     True if the feature added to the map should look editable.
     */
    private void processFeatureIndexResults(FeatureIndexResults indexResults, String database, FeatureDao featureDao,
                                            GoogleMapShapeConverter converter, StyleCache styleCache, AtomicInteger count, final int maxFeatures, final boolean editable) {
        try {
            setErrorCount(0);

            for (FeatureRow row : indexResults) {

                if (cancelled || count.get() >= maxFeatures) {
                    break;
                }

                try {

                    // Process the feature row in the thread pool
                    FeatureRowProcessor processor = new FeatureRowProcessor(
                            this, database, featureDao, row, count, maxFeatures, editable, converter,
                            styleCache, null, 0, true, model, activity);
                    ThreadUtils.getInstance().runBackground(processor);

                } catch (Exception e) {
                    Log.e(GeoPackageMapFragment.class.getSimpleName(),
                            "Failed to display feature. database: " + database
                                    + ", feature table: " + featureDao.getTableName()
                                    + ", row id: " + row.getId(), e);
                }
            }
        } finally {
            indexResults.close();
            int totalErrors = getErrorCount();
            if(totalErrors > 0){
                new Handler(Looper.getMainLooper()).post(() -> {
                            Toast toast = Toast.makeText(activity, totalErrors + " Geometries failed to load", Toast.LENGTH_SHORT);
                            toast.show();
                        });
                setErrorCount(0);

            }
        }
    }

    /**
     * Add marker shape
     *
     * @param featureId The id of the feature.
     * @param database  The name of the geopackage the feature belongs to.
     * @param tableName The name of the layer the feature belongs to.
     * @param shape     The shape to add.
     */
    private void addMarkerShape(long featureId, String database, String tableName, GoogleMapShape shape) {
        if (shape.getShapeType() == GoogleMapShapeType.MARKER) {
            Marker marker = (Marker) shape.getShape();
            MarkerFeature markerFeature = new MarkerFeature(featureId, database, tableName);
            model.getMarkerIds().put(marker.getId(), markerFeature);
        }
    }
    /**
     * Update the number of errors encountered while processing features
     */
    public int getErrorCount() {
        return errorCount;
    }
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    @Override
    public void run() {
        addFeatures(maxFeatures, mapViewBoundingBox, toleranceDistance, filter);
    }
}

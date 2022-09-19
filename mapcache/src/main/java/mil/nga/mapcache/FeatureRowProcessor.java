package mil.nga.mapcache;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.map.features.StyleCache;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeConverter;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

/**
 * Single feature row processor
 *
 * @author osbornb
 */
public class FeatureRowProcessor implements Runnable {

    /**
     * Map update task
     */
    private final MapFeaturesUpdateTask task;

    /**
     * Database
     */
    private final String database;

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Feature row
     */
    private final FeatureRow row;

    /**
     * Total feature count
     */
    private final AtomicInteger count;

    /**
     * Total max features
     */
    private final int maxFeatures;

    /**
     * Editable shape flag
     */
    private final boolean editable;

    /**
     * Shape converter
     */
    private final GoogleMapShapeConverter converter;

    /**
     * Style Cache
     */
    private final StyleCache styleCache;

    /**
     * Filter bounding box
     */
    private final BoundingBox filterBoundingBox;

    /**
     * Max projection longitude
     */
    private final double maxLongitude;

    /**
     * Filter flag
     */
    private final boolean filter;

    /**
     * Contains various states for the map.
     */
    private final MapModel model;

    /**
     * Lock for concurrently updating the features bounding box
     */
    private final Lock featuresBoundingBoxLock = new ReentrantLock();

    /**
     * The application context.
     */
    private final Context context;

    /**
     * Constructor
     *
     * @param task              The update task.
     * @param database          The name of the geopackage the features belong too.
     * @param featureDao        The feature data access object.
     * @param row               The row to process.
     * @param count             The current total count of features.
     * @param maxFeatures       The maximum features to display on the map.
     * @param editable          True if the feature should look editable on the map.
     * @param converter         Converts the feature's shape to one to use on the map.
     * @param styleCache        The style cache.
     * @param filterBoundingBox The bounding box to use for filtering.
     * @param maxLongitude      The maximum longitude.
     * @param filter            True if we should filter using the passed in bounding box.
     * @param model             Contains various states for the map.
     * @param context           The application context.
     */
    public FeatureRowProcessor(MapFeaturesUpdateTask task, String database, FeatureDao featureDao,
                               FeatureRow row, AtomicInteger count, int maxFeatures,
                               boolean editable, GoogleMapShapeConverter converter, StyleCache styleCache,
                               BoundingBox filterBoundingBox, double maxLongitude, boolean filter,
                               MapModel model, Context context) {
        this.task = task;
        this.database = database;
        this.featureDao = featureDao;
        this.row = row;
        this.count = count;
        this.maxFeatures = maxFeatures;
        this.editable = editable;
        this.converter = converter;
        this.styleCache = styleCache;
        this.filterBoundingBox = filterBoundingBox;
        this.maxLongitude = maxLongitude;
        this.filter = filter;
        this.model = model;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        processFeatureRow(task, database, featureDao, converter, styleCache, row, count, maxFeatures,
                editable, filterBoundingBox, maxLongitude, filter);
    }

    /**
     * Process the feature row
     *
     * @param task         The map update task.
     * @param database     The geopackage name the feature row belongs too.
     * @param featureDao   The feature data access object.
     * @param converter    Converts the feature shape to one that can be used on a google map.
     * @param styleCache   The style cache.
     * @param row          The row to process.
     * @param count        The current feature count displayed on map.
     * @param maxFeatures  The maximum features to display on the map.
     * @param editable     True if the feature should look editable on the map.
     * @param boundingBox  The bounding box to use to filter features.
     * @param maxLongitude The maximum longitude.
     * @param filter       True if we should filer using the bounding box.
     */
    private void processFeatureRow(MapFeaturesUpdateTask task, String database, FeatureDao featureDao,
                                   GoogleMapShapeConverter converter, StyleCache styleCache, FeatureRow row, AtomicInteger count,
                                   int maxFeatures, boolean editable, BoundingBox boundingBox, double maxLongitude,
                                   boolean filter) {

        boolean exists;
        synchronized (model.getFeatureShapes()) {
            exists = model.getFeatureShapes().exists(row.getId(), database, featureDao.getTableName());
        }

        if (!exists && !task.isCancelled()) {

            try {
                GeoPackageGeometryData geometryData = row.getGeometry();
                if (geometryData != null && !geometryData.isEmpty()) {

                    final Geometry geometry = geometryData.getGeometry();

                    if (geometry != null) {

                        boolean passesFilter = true;

                        if (filter && boundingBox != null) {
                            GeometryEnvelope envelope = geometryData.getEnvelope();
                            if (envelope == null) {
                                envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
                            }
                            if (envelope != null) {
                                if (geometry.getGeometryType() == GeometryType.POINT) {
                                    mil.nga.sf.Point point = (mil.nga.sf.Point) geometry;
                                    passesFilter = TileBoundingBoxUtils.isPointInBoundingBox(point, boundingBox, maxLongitude);
                                } else {
                                    BoundingBox geometryBoundingBox = new BoundingBox(envelope);
                                    passesFilter = TileBoundingBoxUtils.overlap(boundingBox, geometryBoundingBox, maxLongitude) != null;
                                }
                            }
                        }

                        if (passesFilter && count.getAndIncrement() < maxFeatures) {
                            final long featureId = row.getId();
                            final GoogleMapShape shape = converter.toShape(geometry);
                            updateFeaturesBoundingBox(shape);
                            ShapeHelper.getInstance().prepareShapeOptions(shape, styleCache, row, editable, true, context);
                            task.addToMap(featureId, database, featureDao.getTableName(), shape);
                        }
                    }
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast toast = Toast.makeText(context, "Error loading geometry", Toast.LENGTH_SHORT);
                    toast.show();
                });
            }
        }
    }

    /**
     * Update the features bounding box with the shape
     *
     * @param shape The shape to use to expand the features bounding box.
     */
    private void updateFeaturesBoundingBox(GoogleMapShape shape) {
        try {
            featuresBoundingBoxLock.lock();
            if (model.getFeaturesBoundingBox() != null) {
                shape.expandBoundingBox(model.getFeaturesBoundingBox());
            } else {
                model.setFeaturesBoundingBox(shape.boundingBox());
            }
        } finally {
            featuresBoundingBoxLock.unlock();
        }
    }
}

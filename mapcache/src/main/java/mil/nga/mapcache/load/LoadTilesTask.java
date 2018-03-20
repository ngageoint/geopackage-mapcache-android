package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.PowerManager;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.extension.scale.TileScaling;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabases;

/**
 * Load tiles task
 *
 * @author osbornb
 */
public class LoadTilesTask extends AsyncTask<String, Integer, String> implements
        GeoPackageProgress {

    /**
     * Load tiles from a URL
     *
     * @param activity
     * @param callback
     * @param active
     * @param database
     * @param tableName
     * @param tileUrl
     * @param minZoom
     * @param maxZoom
     * @param compressFormat
     * @param compressQuality
     * @param googleTiles
     * @param boundingBox
     * @param scaling
     * @param authority
     * @param code
     */
    public static void loadTiles(Activity activity, ILoadTilesTask callback,
                                 GeoPackageDatabases active, String database, String tableName,
                                 String tileUrl, int minZoom, int maxZoom,
                                 CompressFormat compressFormat, Integer compressQuality,
                                 boolean googleTiles, BoundingBox boundingBox, TileScaling scaling, String authority, String code) {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackage geoPackage = manager.open(database);

        Projection projection = ProjectionFactory.getProjection(authority, code);
        BoundingBox bbox = transform(boundingBox, projection);

        TileGenerator tileGenerator = new UrlTileGenerator(activity, geoPackage,
                tableName, tileUrl, minZoom, maxZoom, bbox, projection);
        setTileGenerator(activity, tileGenerator, minZoom, maxZoom, compressFormat, compressQuality, googleTiles, boundingBox, scaling);

        loadTiles(activity, callback, active, geoPackage, tableName, tileGenerator);
    }

    /**
     * Load tiles from features
     *
     * @param activity
     * @param callback
     * @param active
     * @param geoPackage
     * @param tableName
     * @param featureTiles
     * @param minZoom
     * @param maxZoom
     * @param compressFormat
     * @param compressQuality
     * @param googleTiles
     * @param boundingBox
     * @param scaling
     * @param authority
     * @param code
     */
    public static void loadTiles(Activity activity, ILoadTilesTask callback,
                                 GeoPackageDatabases active, GeoPackage geoPackage, String tableName,
                                 FeatureTiles featureTiles, int minZoom, int maxZoom,
                                 CompressFormat compressFormat, Integer compressQuality,
                                 boolean googleTiles, BoundingBox boundingBox, TileScaling scaling, String authority, String code) {

        GeoPackageUtils.prepareFeatureTiles(featureTiles);

        Projection projection = ProjectionFactory.getProjection(authority, code);
        BoundingBox bbox = transform(boundingBox, projection);

        TileGenerator tileGenerator = new FeatureTileGenerator(activity, geoPackage,
                tableName, featureTiles, minZoom, maxZoom, bbox, projection);
        setTileGenerator(activity, tileGenerator, minZoom, maxZoom, compressFormat, compressQuality, googleTiles, boundingBox, scaling);

        loadTiles(activity, callback, active, geoPackage, tableName, tileGenerator);
    }

    /**
     * Transform the WGS84 bounding box to the provided projection
     *
     * @param boundingBox bounding box in WGS84
     * @param projection  projection
     * @return projected bounding box
     */
    public static BoundingBox transform(BoundingBox boundingBox, Projection projection) {

        BoundingBox transformedBox = boundingBox;

        if (!projection.equals(ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)) {
            BoundingBox bounded = TileBoundingBoxUtils.boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
            Projection wgs84 = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            ProjectionTransform transform = wgs84.getTransformation(projection);
            transformedBox = transform.transform(bounded);
        }

        return transformedBox;
    }

    /**
     * Set the tile generator settings
     *
     * @param activity
     * @param tileGenerator
     * @param minZoom
     * @param maxZoom
     * @param compressFormat
     * @param compressQuality
     * @param googleTiles
     * @param boundingBox
     * @param scaling
     */
    private static void setTileGenerator(Activity activity, TileGenerator tileGenerator, int minZoom, int maxZoom,
                                         CompressFormat compressFormat, Integer compressQuality,
                                         boolean googleTiles, BoundingBox boundingBox, TileScaling scaling) {

        if (minZoom > maxZoom) {
            throw new GeoPackageException(
                    activity.getString(R.string.generate_tiles_min_zoom_label)
                            + " can not be larger than "
                            + activity
                            .getString(R.string.generate_tiles_max_zoom_label));
        }

        tileGenerator.setCompressFormat(compressFormat);
        tileGenerator.setCompressQuality(compressQuality);
        tileGenerator.setGoogleTiles(googleTiles);
        tileGenerator.setScaling(scaling);
    }

    /**
     * Load tiles
     *
     * @param activity
     * @param callback
     * @param active
     * @param geoPackage
     * @param tableName
     * @param tileGenerator
     */
    private static void loadTiles(Activity activity, ILoadTilesTask callback,
                                  GeoPackageDatabases active, GeoPackage geoPackage, String tableName, TileGenerator tileGenerator) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        final LoadTilesTask loadTilesTask = new LoadTilesTask(activity,
                callback, progressDialog, active);

        tileGenerator.setProgress(loadTilesTask);

        loadTilesTask.setTileGenerator(tileGenerator);

        progressDialog.setMessage(activity
                .getString(R.string.geopackage_create_tiles_label)
                + ": "
                + geoPackage.getName() + " - " + tableName);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(tileGenerator.getTileCount());
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                activity.getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadTilesTask.cancel(true);
                    }
                });

        loadTilesTask.execute();
    }

    private Activity activity;
    private Integer max = null;
    private int progress = 0;
    private TileGenerator tileGenerator;
    private ILoadTilesTask callback;
    private ProgressDialog progressDialog;
    private GeoPackageDatabases active;
    private PowerManager.WakeLock wakeLock;

    /**
     * Constructor
     *
     * @param activity
     * @param callback
     * @param progressDialog
     * @param active
     */
    public LoadTilesTask(Activity activity, ILoadTilesTask callback,
                         ProgressDialog progressDialog, GeoPackageDatabases active) {
        this.activity = activity;
        this.callback = callback;
        this.progressDialog = progressDialog;
        this.active = active;
    }

    /**
     * Set the tile generator
     *
     * @param tileGenerator
     */
    public void setTileGenerator(TileGenerator tileGenerator) {
        this.tileGenerator = tileGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProgress(int progress) {
        this.progress += progress;
        publishProgress(this.progress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return !isCancelled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cleanupOnCancel() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
        PowerManager pm = (PowerManager) activity
                .getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
                .getName());
        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressDialog.setProgress(progress[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled(String result) {
        tileGenerator.close();
        wakeLock.release();
        progressDialog.dismiss();
        callback.onLoadTilesCancelled(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String result) {
        tileGenerator.close();
        wakeLock.release();
        progressDialog.dismiss();
        callback.onLoadTilesPostExecute(result);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            int count = tileGenerator.generateTiles();
            if (count > 0) {
                active.setModified(true);
            }
            if (count < max && !(tileGenerator instanceof FeatureTileGenerator)) {
                return "Fewer tiles were generated than expected. Expected: "
                        + max + ", Actual: " + count;
            }
        } catch (final Exception e) {
            return e.toString();
        }
        return null;
    }

}

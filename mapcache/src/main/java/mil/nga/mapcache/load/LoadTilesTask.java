package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap.CompressFormat;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.HttpUtils;
import mil.nga.mapcache.utils.ThreadUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;

/**
 * Load tiles task
 *
 * @author osbornb
 */
public class LoadTilesTask implements GeoPackageProgress, Runnable {

    /**
     * Load tiles from a URL
     *
     * @param activity The main activity.
     * @param callback Called when the load tiles task has completed or was cancelled.
     * @param viewModel Used to get the geoPackage.
     * @param database The geoPackage name to load tiles for.
     * @param tableName The tile layer to load tiles for.
     * @param tileUrl The url to the server hosting the tiles.
     * @param minZoom The minimum zoom.
     * @param maxZoom The maximum zoom.
     * @param compressFormat The image format to use.
     * @param compressQuality The compression quality to use.
     * @param xyzTiles True if xyz tiles, false if not.
     * @param boundingBox The area we will download the tiles for.
     * @param scaling Scaling information.
     * @param authority The projection authority.
     * @param code The projection code.
     * @param headers Any header values that need to be added to the tile download requests.
     */
    public static void loadTiles(Activity activity, ILoadTilesTask callback,
                                 GeoPackageViewModel viewModel, String database, String tableName,
                                 String tileUrl, int minZoom, int maxZoom,
                                 CompressFormat compressFormat, Integer compressQuality,
                                 boolean xyzTiles, BoundingBox boundingBox, TileScaling scaling, String authority, String code,
                                 Map<String, List<String>> headers) {

        GeoPackage geoPackage = viewModel.getGeoPackage(database);

        Projection projection = ProjectionFactory.getProjection(authority, code);
        BoundingBox bBox = transform(boundingBox, projection);

        UrlTileGenerator tileGenerator = new UrlTileGenerator(activity, geoPackage,
                tableName, tileUrl, minZoom, maxZoom, bBox, projection);
        tileGenerator.addHTTPHeaderValue(
                HttpUtils.getInstance().getUserAgentKey(),
                HttpUtils.getInstance().getUserAgentValue(activity));
        if(headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                for (String value : header.getValue()) {
                    tileGenerator.addHTTPHeaderValue(header.getKey(), value);
                }
            }
        }

        setTileGenerator(activity, tileGenerator, minZoom, maxZoom, compressFormat, compressQuality, xyzTiles, scaling);

        loadTiles(activity, callback, viewModel, geoPackage, tableName, tileGenerator);
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
            transformedBox = bounded.transform(transform);
        }

        return transformedBox;
    }

    /**
     * Set the tile generator settings
     *
     * @param activity The main activity.
     * @param tileGenerator The tile generator.
     * @param minZoom The minimum zoom.
     * @param maxZoom The maximum zoom.
     * @param compressFormat The image format to use.
     * @param compressQuality The compression quality to use.
     * @param xyzTiles True if xyz tiles, false if not.
     * @param scaling Scaling information.
     */
    private static void setTileGenerator(Activity activity, TileGenerator tileGenerator, int minZoom, int maxZoom,
                                         CompressFormat compressFormat, Integer compressQuality,
                                         boolean xyzTiles, TileScaling scaling) {

        if (minZoom > maxZoom) {
            throw new GeoPackageException(
                    activity.getString(R.string.generate_tiles_min_zoom_label)
                            + " can not be larger than "
                            + activity
                            .getString(R.string.generate_tiles_max_zoom_label));
        }

        tileGenerator.setCompressFormat(compressFormat);
        tileGenerator.setCompressQuality(compressQuality);
        tileGenerator.setXYZTiles(xyzTiles);
        tileGenerator.setScaling(scaling);
    }

    /**
     * Load tiles
     *
     * @param activity The main activity.
     * @param callback Called when the load tiles task has completed or was cancelled.
     * @param viewModel Used to get the geoPackage.
     * @param geoPackage The geoPackage to load tiles into.
     * @param tableName The tile layer to load tiles into.
     * @param tileGenerator The tile generator.
     */
    private static void loadTiles(Activity activity, ILoadTilesTask callback,
                                  GeoPackageViewModel viewModel, GeoPackage geoPackage, String tableName, TileGenerator tileGenerator) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        final LoadTilesTask loadTilesTask = new LoadTilesTask(activity,
                callback, progressDialog, viewModel);

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
                (DialogInterface dialog, int which) -> loadTilesTask.isCancelled = true);

        progressDialog.show();
        ThreadUtils.getInstance().runBackground(loadTilesTask);
    }

    private final Activity activity;
    private Integer max = null;
    private int progress = 0;
    private TileGenerator tileGenerator;
    private final ILoadTilesTask callback;
    private final ProgressDialog progressDialog;
    private final GeoPackageViewModel viewModel;
    private PowerManager.WakeLock wakeLock;
    private boolean isCancelled = false;

    /**
     * Constructor
     *
     * @param activity The main activity.
     * @param callback Called when the load tiles task has completed or was cancelled.
     * @param progressDialog The progress dialog.
     * @param viewModel Used to get the geoPackage.
     */
    public LoadTilesTask(Activity activity, ILoadTilesTask callback,
                         ProgressDialog progressDialog, GeoPackageViewModel viewModel) {
        this.activity = activity;
        this.callback = callback;
        this.progressDialog = progressDialog;
        this.viewModel = viewModel;
    }

    /**
     * Set the tile generator
     *
     * @param tileGenerator The tile generator.
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
        return !isCancelled;
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
    private void publishProgress(Integer... progress) {
        activity.runOnUiThread(() -> progressDialog.setProgress(progress[0]));
    }

    @Override
    public void run() {
        try {
            PowerManager pm = (PowerManager) activity
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
                    .getName());
            wakeLock.acquire(43200000);

            int count = tileGenerator.generateTiles();
            String result = null;
            if (count == 0) {
                result = "No tiles were generated for your new layer.  " +
                        "This could be an issue with your tile URL or the tile server.  " +
                        "Please verify the server URL and try again.";
            }
            if (count > 0 && viewModel.getActive().getValue() != null) {
                viewModel.getActive().getValue().setModified(true);
            }
            if (count < max && !(tileGenerator instanceof FeatureTileGenerator)) {
                result = "Fewer tiles were generated than " +
                        "expected. Expected: " + max + ", Actual: " + count +
                        ".  This is likely an issue with the tile server or a slow / " +
                        "intermittent network connection.";
            }

            callback.onLoadTilesPostExecute(result);
        } catch (final Exception e) {
            Log.e(LoadTilesTask.class.getSimpleName(), e.getMessage(), e);
        } finally {
            tileGenerator.close();
            wakeLock.release();
            progressDialog.dismiss();
        }
    }
}

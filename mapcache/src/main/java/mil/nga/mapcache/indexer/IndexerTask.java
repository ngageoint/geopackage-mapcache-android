package mil.nga.mapcache.indexer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.mapcache.R;

/**
 * Indexer task
 *
 * @author osbornb
 */
public class IndexerTask extends AsyncTask<String, Integer, String> implements
        GeoPackageProgress {

    /**
     * Index features
     *
     * @param activity
     * @param callback
     * @param database
     * @param tableName
     * @param indexLocation
     */
    public static void indexFeatures(Activity activity, IIndexerTask callback,
                                     String database, String tableName,
                                     FeatureIndexType indexLocation) {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackage geoPackage = manager.open(database);

        FeatureDao featureDao = geoPackage.getFeatureDao(tableName);

        FeatureIndexManager indexer = new FeatureIndexManager(activity, geoPackage, featureDao);
        indexer.setIndexLocation(indexLocation);

        ProgressDialog progressDialog = new ProgressDialog(activity);
        final IndexerTask indexTask = new IndexerTask(activity,
                callback, progressDialog, geoPackage, indexer);

        int max = featureDao.count();
        indexTask.setMax(max);
        indexer.setProgress(indexTask);

        progressDialog.setMessage(activity
                .getString(R.string.geopackage_table_index_features_index_title)
                + ": "
                + geoPackage.getName() + " - " + tableName);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(max);
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                activity.getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        indexTask.cancel(true);
                    }
                });

        indexTask.execute();
    }

    private Activity activity;
    private Integer max = null;
    private GeoPackage geoPackage;
    private int progress = 0;
    private FeatureIndexManager indexer;
    private IIndexerTask callback;
    private ProgressDialog progressDialog;
    private PowerManager.WakeLock wakeLock;

    /**
     * Constructor
     *
     * @param activity
     * @param callback
     * @param progressDialog
     * @param geoPackage
     * @param indexer
     */
    public IndexerTask(Activity activity, IIndexerTask callback,
                       ProgressDialog progressDialog, GeoPackage geoPackage, FeatureIndexManager indexer) {
        this.activity = activity;
        this.callback = callback;
        this.progressDialog = progressDialog;
        this.geoPackage = geoPackage;
        this.indexer = indexer;
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
        geoPackage.close();
        wakeLock.release();
        progressDialog.dismiss();
        callback.onIndexerCancelled(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String result) {
        indexer.close();
        geoPackage.close();
        wakeLock.release();
        progressDialog.dismiss();
        callback.onIndexerPostExecute(result);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            int count = indexer.index(true);
            if (count < max) {
                return "Fewer features were indexed than expected. Expected: "
                        + max + ", Actual: " + count;
            }
        } catch (final Exception e) {
            return e.toString();
        }
        return null;
    }

}

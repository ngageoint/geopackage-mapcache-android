package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;

import java.net.URL;

import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * Download a GeoPackage from a URL in the background
 */
public class DownloadTask extends AsyncTask<String, Integer, String>
        implements GeoPackageProgress {

    private Integer max = null;
    private int progress = 0;
    private final String database;
    private final String url;
    private PowerManager.WakeLock wakeLock;
    private Activity activity;
    private GeoPackageViewModel geoPackageViewModel;
    private String cancel;
    private String importLabel;

    /**
     * Progress dialog for network operations
     */
    private ProgressDialog progressDialog;

    /**
     * Constructor
     *
     * @param database
     * @param url
     */
    public DownloadTask(String database, String url, FragmentActivity activity) {
        this.activity = activity;
        this.database = database;
        this.url = url;
        geoPackageViewModel = new ViewModelProvider(activity).get(GeoPackageViewModel.class);
        cancel = activity.getApplicationContext().getResources().getString(R.string.button_cancel_label);
        importLabel = activity.getApplicationContext().getResources().getString(R.string.geopackage_import_label);
        progressDialog = createDownloadProgressDialog(database, url, this, null);
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
        if (max != null) {
            int total = (int) (this.progress / ((double) max) * 100);
            publishProgress(total);
        }
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
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
        PowerManager pm = (PowerManager) activity.getSystemService(
                Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        // If the indeterminate progress dialog is still showing, swap to a
        // determinate horizontal bar
        if (progressDialog.isIndeterminate()) {

            String messageSuffix = "\n\n"
                    + GeoPackageIOUtils.formatBytes(max);

            ProgressDialog newProgressDialog = createDownloadProgressDialog(
                    database, url, this, messageSuffix);
            newProgressDialog
                    .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            newProgressDialog.setIndeterminate(false);
            newProgressDialog.setMax(100);

            newProgressDialog.show();
            progressDialog.dismiss();
            progressDialog = newProgressDialog;
        }

        // Set the progress
        progressDialog.setProgress(progress[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled(String result) {
        wakeLock.release();
        progressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String result) {
        wakeLock.release();
        progressDialog.dismiss();
        if (result != null) {
            GeoPackageUtils.showMessage(activity, importLabel, result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(String... params) {
        try {
            URL theUrl = new URL(url);
            if (!geoPackageViewModel.importGeoPackage(database, theUrl, this)) {
                return "Failed to import GeoPackage '" + database
                        + "' at url '" + url + "'";
            }
        } catch (final Exception e) {
            return "Couldn't download GeoPackage from: " + url + "\n\nFull error:\n" + e.toString();
        }
        return null;
    }

    /**
     * Create a download progress dialog
     *
     * @param database
     * @param url
     * @param downloadTask
     * @param suffix
     * @return
     */
    public ProgressDialog createDownloadProgressDialog(String database,
                                                        String url, final DownloadTask downloadTask, String suffix) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage(importLabel + " "
                + database + "\n\n" + url + (suffix != null ? suffix : ""));
        dialog.setCancelable(false);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadTask.cancel(true);
                    }
                });
        dialog.setIndeterminate(true);

        return dialog;
    }

}



package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.app.*;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.BuildConfig;
import mil.nga.mapcache.GeoPackageManagerFragment;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

public class ShareTask {

    /**
     * Intent activity request code when sharing a file
     */
    public static final int ACTIVITY_SHARE_FILE = 3343;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".fileprovider";

    private Activity activity;
    private GeoPackageViewModel geoPackageViewModel;

    public ShareTask(FragmentActivity activity) {
        this.activity = activity;
        geoPackageViewModel = ViewModelProviders.of(activity).get(GeoPackageViewModel.class);
    }


    /**
     * Share database option
     *
     * @param database
     */
    public void shareDatabaseOption(final String database) {

        try {
            // Get the database file
            File databaseFile = geoPackageViewModel.getDatabaseFile(database);

            // Create the share intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("*/*");

            // If external database, no permission is needed
            if (geoPackageViewModel.isExternal(database)) {
                // Create the Uri and share
                Uri databaseUri = FileProvider.getUriForFile(activity,
                        AUTHORITY,
                        databaseFile);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                launchShareIntent(shareIntent, databaseUri);
            }
            // If internal database, file must be copied to cache for permission
            else {
                // Launch the share copy task
                ShareCopyTask shareCopyTask = new ShareCopyTask(shareIntent);
                shareCopyTask.execute(databaseFile, database);
            }

        } catch (Exception e) {
            GeoPackageUtils.showMessage(activity, "Share", e.getMessage());
        }
    }




    /**
     * Get the database cache directory
     *
     * @return
     */
    private File getDatabaseCacheDirectory() {
        return new File(activity.getCacheDir(), "databases");
    }




    /**
     * Launch the provided share intent with the database Uri
     *
     * @param shareIntent
     * @param databaseUri
     */
    private void launchShareIntent(Intent shareIntent, Uri databaseUri) {

        // Add the Uri
        shareIntent.putExtra(Intent.EXTRA_STREAM, databaseUri);

        // Start the share activity for result to delete the cache when done
        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share"), ACTIVITY_SHARE_FILE);

    }



    /**
     * Copy an internal database to a shareable location and share
     */
    private class ShareCopyTask extends AsyncTask<Object, Void, String> {

        /**
         * Share intent
         */
        private Intent shareIntent;

        /**
         * Share copy dialog
         */
        private ProgressDialog shareCopyDialog = null;

        /**
         * Cache file created
         */
        private File cacheFile = null;


    /**
     * Constructor
     *
     * @param shareIntent
     */
    ShareCopyTask(Intent shareIntent) {
        this.shareIntent = shareIntent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        shareCopyDialog = new ProgressDialog(activity);
        shareCopyDialog
                .setMessage("Preparing internal GeoPackage for sharing");
        shareCopyDialog.setCancelable(false);
        shareCopyDialog.setIndeterminate(true);
        shareCopyDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel(true);
                    }
                });
        shareCopyDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(Object... params) {

        File databaseFile = (File) params[0];
        String database = (String) params[1];

        // Copy the database to cache
        File cacheDirectory = getDatabaseCacheDirectory();
        cacheDirectory.mkdir();
        cacheFile = new File(cacheDirectory, database + "."
                + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        try {
            GeoPackageIOUtils.copyFile(databaseFile, cacheFile);
        } catch (IOException e) {
            return e.getMessage();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled(String result) {
        shareCopyDialog.dismiss();
        deleteCachedDatabaseFiles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String result) {
        shareCopyDialog.dismiss();
        if (result != null) {
            GeoPackageUtils.showMessage(activity,
                    "Share", result);
        } else {
            // Create the content Uri and add intent permissions
            Uri databaseUri = FileProvider.getUriForFile(activity,
                    AUTHORITY,
                    cacheFile);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            launchShareIntent(shareIntent, databaseUri);
        }
    }
}

    /**
     * Delete any cached database files
     */
    private void deleteCachedDatabaseFiles() {
        File databaseCache = getDatabaseCacheDirectory();
        if (databaseCache.exists()) {
            File[] cacheFiles = databaseCache.listFiles();
            if (cacheFiles != null) {
                for (File cacheFile : cacheFiles) {
                    cacheFile.delete();
                }
            }
            databaseCache.delete();
        }
    }

}




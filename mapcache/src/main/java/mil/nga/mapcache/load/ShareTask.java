package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.BuildConfig;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.ViewAnimation;
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
     * @param database GeoPackage name
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
     * Save the given database to disk
     * @param database GeoPackage name
     */
    private void saveDatabaseOption(final String database){
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
                // Launch the save to disk task
                SaveToDiskTask saveTask = new SaveToDiskTask(shareIntent);
                saveTask.execute(databaseFile, database);
            }

        } catch (Exception e) {
            GeoPackageUtils.showMessage(activity, "Save", e.getMessage());
        }
    }



    /**
     * Shows a popup to ask if the user wants to save to disk or share with external apps
     * @return constant representing either share or save
     */
    public void askToSaveOrShare(String database){
        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.share_file_popup, null);
        ViewAnimation.setScaleAnimatiom(alertView, 200);
        // title
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Share GeoPackage");

        // Initial dialog asking for create or import
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // Click listener for "Share"
        alertView.findViewById(R.id.share_menu_share_card)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareDatabaseOption(database);
                        alertDialog.dismiss();
                    }
                });

        // Click listener for "Save"
        alertView.findViewById(R.id.share_menu_save_card)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveDatabaseOption(database);
                        alertDialog.dismiss();
                    }
                });

        alertDialog.show();
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
     * Saves a given file to disk
     */
    private class SaveToDiskTask extends AsyncTask<Object, Void, String> {
        /**
         * save intent
         */
        private Intent saveIntent;

        /**
         * Cache file created
         */
        private File cacheFile = null;

        /**
         * Save dialog
         */
        private ProgressDialog saveDialog = null;

        /**
         * Constructor
         *
         * @param saveIntent
         */
        SaveToDiskTask(Intent saveIntent) {
            this.saveIntent = saveIntent;
        }

        /**
         * pre execute - show dialog
         */
        @Override
        protected void onPreExecute() {
            saveDialog = new ProgressDialog(activity);
            saveDialog
                    .setMessage("Saving GeoPackage to Downloads");
            saveDialog.setCancelable(false);
            saveDialog.setIndeterminate(true);
            saveDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(true);
                        }
                    });
            saveDialog.show();
        }

        /**
         * Save file to the downloads directory
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Object... params) {

            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File databaseFile = (File) params[0];
            String database = (String) params[1];

            // Copy the database to cache
            File cacheDirectory = getDatabaseCacheDirectory();
            cacheDirectory.mkdir();
            cacheFile = new File(downloadDir, database + "."
                    + GeoPackageConstants.GEOPACKAGE_EXTENSION);
            try {
                GeoPackageIOUtils.copyFile(databaseFile, cacheFile);
            } catch (IOException e) {
                return e.getMessage();
            }
            return null;
        }

        /**
         * post execute - close dialog
         */
        @Override
        protected void onPostExecute(String result) {
            saveDialog.dismiss();
            if (result != null) {
                GeoPackageUtils.showMessage(activity,
                        "Save", result);
            }
            Toast.makeText(activity, "GeoPackage saved to Downloads", Toast.LENGTH_SHORT).show();
            deleteCachedDatabaseFiles();
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




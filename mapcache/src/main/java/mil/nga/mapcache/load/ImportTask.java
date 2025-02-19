package mil.nga.mapcache.load;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.InputStream;

import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.io.MapCacheFileUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * Import a GeoPackage from file
 */

public class ImportTask {

    private String importLabel = "Import";
    private String okLabel = "OK";
    private String cancelLabel = "Cancel";
    private Activity activity;
    private GeoPackageViewModel geoPackageViewModel;
    private ProgressDialog progressDialog;
    private final Uri uri;




    public ImportTask(FragmentActivity activity, Intent data){
        this.activity = activity;
        geoPackageViewModel = ViewModelProviders.of(activity).get(GeoPackageViewModel.class);
        uri = data.getData();
    }



    /**
     * Import the GeoPackage file selected
     *
     */
    public void importFile() {
        if (activity != null && uri != null){
            // Try to get the file path and name
            final String path = FileUtils.getPath(activity, uri);
            String name = MapCacheFileUtils.INSTANCE.getDisplayName(uri, path);

            LayoutInflater inflater = LayoutInflater.from(activity);
            View importFileView = inflater.inflate(R.layout.import_file, null);
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
            dialog.setView(importFileView);

            final TextInputEditText nameInput = (TextInputEditText) importFileView
                    .findViewById(R.id.import_file_name_input);

            // Set the default name
            if (name != null) {
                nameInput.setText(name);
            }

            dialog.setPositiveButton(importLabel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    String value = nameInput.getText().toString();
                                    if (!value.isEmpty()) {
                                        try {
                                            // Import the GeoPackage by copying the file
                                            importGeoPackage(value, uri, path);
                                        } catch (final Exception e) {
                                            try {
                                                activity.runOnUiThread(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                GeoPackageUtils
                                                                        .showMessage(
                                                                                activity,
                                                                                "File Import",
                                                                                "Uri: "
                                                                                        + uri.getPath()
                                                                                        + ", "
                                                                                        + e.getMessage());
                                                            }
                                                        });
                                            } catch (Exception e2) {
                                                // eat
                                            }
                                        }
                                    }
                                }
                            })
                    .setNegativeButton(cancelLabel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.cancel();
                                }
                            });

            dialog.show();
        }
    }

    /**
     * Import the GeoPackage by linking to the file
     *
     * @param name
     * @param uri
     * @param path
     */
    public void importGeoPackageExternalLink(final String name, final Uri uri, String path) {
        // Check if a database already exists with the name
        if (geoPackageViewModel.exists(name)) {
            // If the existing is not an external file, error
            boolean alreadyExistsError = !geoPackageViewModel.isExternal(name);
            if (!alreadyExistsError) {
                // If the existing external file has a different file path, error
                File existingFile = geoPackageViewModel.getDatabaseFile(name);
                alreadyExistsError = !(new File(path)).equals(existingFile);
            }
            if (alreadyExistsError) {
                try {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    GeoPackageUtils.showMessage(activity,
                                            "Import Cancelled",
                                            "A different GeoPackage already exists with the name '" + name + "'");
                                }
                            });
                } catch (Exception e) {
                    // eat
                }
            }
        } else {
            // Import the GeoPackage by linking to the file
            boolean imported = geoPackageViewModel
                    .importGeoPackageAsExternalLink(
                            path, name);

            if (!imported) {
                try {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    GeoPackageUtils.showMessage(activity,
                                            "URL Import",
                                            "Failed to import Uri: "
                                                    + uri.getPath());
                                }
                            });
                } catch (Exception e) {
                    // eat
                }
            }
        }
    }




    /**
     * Run the import task to import a GeoPackage by copying it
     *
     * @param name
     * @param uri
     * @param path
     */
    public void importGeoPackage(final String name, Uri uri, String path) {

        // Check if a database already exists with the name
        if (geoPackageViewModel.geoPackageNameExists(name)) {
            try {
                activity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                GeoPackageUtils.showMessage(activity,
                                        "Import Cancelled",
                                        "A GeoPackage already exists with the name '" + name + "'");
                            }
                        });
            } catch (Exception e) {
                // eat
            }
        } else {

            ImportGeoTask importGeoTask = new ImportGeoTask(name, path, uri);
            progressDialog = createImportProgressDialog(name,
                    importGeoTask, path, uri, null);
            progressDialog.setIndeterminate(true);
            importGeoTask.execute();
        }
    }




    /**
     * Import a GeoPackage from a stream in the background
     */
    private class ImportGeoTask extends AsyncTask<String, Integer, String>
            implements GeoPackageProgress {

        private Integer max = null;
        private int progress = 0;
        private final String database;
        private final String path;
        private final Uri uri;
        private PowerManager.WakeLock wakeLock;

        /**
         * Constructor
         *
         * @param database
         * @param path
         * @param uri
         */
        public ImportGeoTask(String database, String path, Uri uri) {
            this.database = database;
            this.path = path;
            this.uri = uri;
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

                ProgressDialog newProgressDialog = createImportProgressDialog(
                        database, this, path, uri, messageSuffix);
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
                GeoPackageUtils.showMessage(activity,
                        "Import",
                        "Failed to import: "
                                + (path != null ? path : uri.getPath()));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                final ContentResolver resolver = activity.getContentResolver();
                InputStream stream = resolver.openInputStream(uri);
                if (!geoPackageViewModel.importGeoPackage(database, stream, this)) {
                    return "Failed to import GeoPackage '" + database + "'";
                }
            } catch (final Exception e) {
                return e.toString();
            }
            return null;
        }

    }



    /**
     * Create a import progress dialog
     *
     * @param database
     * @param importGeoTask
     * @param path
     * @param uri
     * @param suffix
     * @return
     */
    private ProgressDialog createImportProgressDialog(String database, final ImportGeoTask importGeoTask,
                                                      String path, Uri uri,
                                                      String suffix) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setTitle(importLabel);
        dialog.setMessage("\n" + database + "\n\n" + (path != null ? path : uri.getPath()) + (suffix != null ? suffix : ""));
        dialog.setCancelable(false);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                cancelLabel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importGeoTask.cancel(true);
                    }
                });
        return dialog;
    }

}

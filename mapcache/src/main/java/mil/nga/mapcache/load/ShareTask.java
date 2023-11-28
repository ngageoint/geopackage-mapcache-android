package mil.nga.mapcache.load;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.BuildConfig;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.load.SaveToDiskExecutor;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;


/**
 * Handles sending a GeoPackage to external apps or saving that file to external disk
 * Used from the GeoPackage detail view share button
 */
public class ShareTask {

    /**
     * Intent activity request code when sharing a file
     */
    public static final int ACTIVITY_SHARE_FILE = 3343;

    /**
     * file provider id
     */
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".fileprovider";

    /**
     * Save a reference to the parent activity
     */
    private Activity activity;

    /**
     * Name should be saved to the task
     */
    private String geoPackageName;

    /**
     * GeoPackage file for sharing
     */
    private File geoPackageFile;

    /**
     * Is the saved geoPackage an external file
     */
    private boolean isFileExternal = false;

    public ShareTask(FragmentActivity activity) {
        this.activity = activity;
    }


    /**
     * Share database with external apps via intent
     */
    private void shareDatabaseOption() {
        try {
            // Create the share intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("*/*");

            // If external database, no permission is needed
            if (isFileExternal) {
                // Create the Uri and share
                Uri databaseUri = FileProvider.getUriForFile(activity, AUTHORITY, geoPackageFile);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                launchShareIntent(shareIntent, databaseUri);
            }
            // If internal database, file must be copied to cache for permission
            else {
                // Launch the share copy task
                ShareCopyExecutor shareCopyExecutor = new ShareCopyExecutor(activity, shareIntent);
                shareCopyExecutor.shareGeoPackage(getDatabaseCacheDirectory(), geoPackageFile, geoPackageName);
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(activity, "Error sharing GeoPackage", e.getMessage());
        }
    }



    /**
     * Save the given database to the downloads directory
     */
    private void saveDatabaseOption(){
        try {
            // Create the share intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            // Launch the save to disk task
            SaveToDiskExecutor diskExecutor = new SaveToDiskExecutor(activity);
            diskExecutor.saveToDisk(getDatabaseCacheDirectory(), geoPackageFile, geoPackageName);
        } catch (Exception e) {
            GeoPackageUtils.showMessage(activity, "Error saving to file", e.getMessage());
        }
    }



    /**
     * Shows a popup to ask if the user wants to save to disk or share with external apps
     * @return constant representing either share or save
     */
    public void askToSaveOrShare(){
        try {
            if(geoPackageFile == null || geoPackageName == null){
                throw new Exception("GeoPackage could not be found");
            }
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
                            shareDatabaseOption();
                            alertDialog.dismiss();
                        }
                    });

            // Click listener for "Save"
            alertView.findViewById(R.id.share_menu_save_card)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveDatabaseOption();
                            alertDialog.dismiss();
                        }
                    });

            alertDialog.show();
        } catch (Exception e) {
            GeoPackageUtils.showMessage(activity, "Error sharing", e.getMessage());
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
        shareIntent.putExtra(Intent.EXTRA_STREAM, databaseUri);
        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share"), ACTIVITY_SHARE_FILE);
    }

    public String getGeoPackageName() {
        return geoPackageName;
    }

    public void setGeoPackageName(String geoPackageName) {
        this.geoPackageName = geoPackageName;
    }

    public void setGeoPackageFile(File geoPackageFile){
        this.geoPackageFile = geoPackageFile;
    }

    public File getGeoPackageFile(){
        return this.geoPackageFile;
    }

    public boolean isFileExternal() {
        return isFileExternal;
    }

    public void setFileExternal(boolean fileExternal) {
        isFileExternal = fileExternal;
    }
}




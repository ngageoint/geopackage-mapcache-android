package mil.nga.mapcache;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.io.GeoPackageIOUtils;

/**
 * Detail page after clicking on a GeoPackage in the manage view
 */
public class GeoPackageDetail extends AppCompatActivity {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".fileprovider";
    private GeoPackageManager manager;
    private GeoPackage selectedGeo;
    private String geoPackageName;
    /**
     * Intent activity request code when sharing a file
     */
    public static final int ACTIVITY_SHARE_FILE = 3343;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = GeoPackageFactory.getManager(getBaseContext());
        setContentView(R.layout.activity_geo_package_detail);
        Intent intent = getIntent();

        // Get the geopackage
        geoPackageName = intent.getStringExtra(GeoPackageManagerFragment.GEO_PACKAGE_DETAIL);
        selectedGeo = manager.open(geoPackageName, false);

        // Set listeners for geopackage action buttons
        Button renameButton = (Button) findViewById(R.id.detail_rename);
        renameButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                renameDatabaseOption(geoPackageName);
            }
        });
        Button deleteButton = (Button) findViewById(R.id.detail_delete);
        deleteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                deleteDatabaseOption(geoPackageName);
            }
        });
        Button shareButton = (Button) findViewById(R.id.detail_share);
        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                shareDatabaseOption(geoPackageName);
            }
        });
        Button copyButton = (Button) findViewById(R.id.detail_copy);
        copyButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                copyDatabaseOption(geoPackageName);
            }
        });

        // Get list of layer names and assign icons, then generate list items
        final ListView listview = (ListView) findViewById(R.id.layer_list);
        List<String> layers = new ArrayList<>();
        List<Integer> icons = new ArrayList<>();
        if(selectedGeo.getFeatureTables() != null) {
            Iterator<String> featureIterator = selectedGeo.getFeatureTables().iterator();
            while (featureIterator.hasNext()) {
                layers.add(featureIterator.next());
                icons.add(R.drawable.material_feature);
            }
        }
        if(selectedGeo.getTileTables() != null) {
            Iterator<String> tileIterator = selectedGeo.getTileTables().iterator();
            while (tileIterator.hasNext()) {
                layers.add(tileIterator.next());
                icons.add(R.drawable.material_tile);
            }
        }

        LayerRowAdapter layerRowAdapter = new LayerRowAdapter(this, layers, icons);
        listview.setAdapter(layerRowAdapter);


        // update for the first time to load initial data
        update();

    }


    /**
     *   Adapter to draw a row of layer names with a matching icon in the row
     */
    public class LayerRowAdapter extends BaseAdapter {

        private Context mContext;
        private List<String>  Title;
        private List<Integer> imge;

        public LayerRowAdapter(Context context, List<String> text1,List<Integer> imageIds) {
            mContext = context;
            Title = text1;
            imge = imageIds;

        }
        public int getCount() {
            return Title.size();
        }
        public Object getItem(int arg0) {
            return null;
        }
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.layer_row_layout, parent, false);
            TextView title;
            ImageView i1;
            i1 = (ImageView) row.findViewById(R.id.layer_icon);
            title = (TextView) row.findViewById(R.id.layer_label);
            title.setText(Title.get(position));
            i1.setImageResource(imge.get(position));

            return (row);
        }
    }






    /**
     *   Update the currently loaded geopackage data for page display
     */
    private void update(){
        // reload the selected geopackage file from manager
        selectedGeo = manager.open(geoPackageName, false);
        TextView nameText = (TextView) findViewById(R.id.text_name);
        TextView sizeText = (TextView) findViewById(R.id.text_size);
        TextView tileText = (TextView) findViewById(R.id.text_tiles);
        TextView featureText = (TextView) findViewById(R.id.text_features);

        // Set the page data
        nameText.setText(selectedGeo.getName());
        sizeText.setText(manager.readableSize(geoPackageName));
        int tileCount = selectedGeo.getTileTables().size();
        int featureCount = selectedGeo.getFeatureTables().size();
        tileText.setText(tileCount + " " + pluralize(tileCount, "Tile layer"));
        featureText.setText(featureCount + " " + pluralize(featureCount, "Feature layer"));
    }







//    private void setAllLayersActive(){
//        manager.
//    }


    /**
     * Rename database dialog window
     *
     * @param database
     */
    private void renameDatabaseOption(final String database) {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_edit);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Rename GeoPackage");
        // GeoPackage name
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        inputName.setHint(database);
        inputName.setText(database);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialogBuilder.setView(alertView);
        dialogBuilder.setPositiveButton(R.string.button_ok_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = inputName.getText().toString();
                if (value != null && !value.isEmpty()
                        && !value.equals(database)) {
                    try{
                        if(manager.rename(database, value)) {
                            geoPackageName = value;
                            update();
//                            Toast.makeText(GeoPackageDetail.this,"Renamed " + database, Toast.LENGTH_SHORT).show();
                        } else{
                            GeoPackageUtils
                                    .showMessage(
                                            GeoPackageDetail.this,
                                            getString(R.string.geopackage_rename_label),
                                            "Rename from "
                                                    + database
                                                    + " to "
                                                    + value
                                                    + " was not successful");
                        };
                    } catch (Exception e){
                        GeoPackageUtils
                                .showMessage(
                                        GeoPackageDetail.this,
                                        getString(R.string.geopackage_rename_label),
                                        e.getMessage());
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.button_cancel_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }




    /**
     * Share geopackage
     *
     * @param database
     */
    private void shareDatabaseOption(final String database) {
        try{

            // Get the database file
            File databaseFile = manager.getFile(database);

            // Create the share intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("*/*");

            // If external database, no permission is needed
            if (manager.isExternal(database)) {
                // Create the Uri and share
                Uri databaseUri = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        databaseFile);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                launchShareIntent(shareIntent, databaseUri);
            }
            // If internal database, file must be copied to cache for permission
            else {
                // Launch the share copy task
                GeoPackageDetail.ShareCopyTask shareCopyTask = new GeoPackageDetail.ShareCopyTask(shareIntent);
                shareCopyTask.execute(databaseFile, database);
            }

        } catch (Exception e){
            GeoPackageUtils.showMessage(this,
                    getString(R.string.geopackage_share_label), e.getMessage());
        }
    }



    /**
     * Copy database option
     *
     * @param database
     */
    private void copyDatabaseOption(final String database) {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_copy);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Copy GeoPackage");
        // GeoPackage name
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        inputName.setHint(database + getString(R.string.geopackage_copy_suffix));
        inputName.setText(database + getString(R.string.geopackage_copy_suffix));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = inputName.getText().toString();
                                if (value != null && !value.isEmpty()
                                        && !value.equals(database)) {
                                    try {
                                        if (manager.copy(database, value)) {
                                            Toast.makeText(GeoPackageDetail.this, "Copied " + database, Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            GeoPackageDetail.this,
                                                            getString(R.string.geopackage_copy_label),
                                                            "Copy from "
                                                                    + database
                                                                    + " to "
                                                                    + value
                                                                    + " was not successful");
                                        }
                                    } catch (Exception e) {
                                        GeoPackageUtils
                                                .showMessage(
                                                        GeoPackageDetail.this,
                                                        getString(R.string.geopackage_copy_label),
                                                        e.getMessage());
                                    }
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });

        dialog.show();
    }



    /**
     * Delete database alert option
     *
     * @param database
     */
    private void deleteDatabaseOption(final String database) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setIcon(R.drawable.material_delete);
        dialogBuilder.setTitle(R.string.geopackage_delete_label);
        dialogBuilder.setMessage("Delete GeoPackage: " + database + "?");
        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (manager.delete(database)) {
                        Toast.makeText(GeoPackageDetail.this, "Deleted " + database, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e){
                    GeoPackageUtils
                            .showMessage(
                                    GeoPackageDetail.this,
                                    getString(R.string.geopackage_delete_label),
                                    e.getMessage());
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.button_cancel_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
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
        startActivityForResult(Intent.createChooser(shareIntent, getResources()
                .getText(R.string.geopackage_share_label)), ACTIVITY_SHARE_FILE);
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
            shareCopyDialog = new ProgressDialog(GeoPackageDetail.this);
            shareCopyDialog
                    .setMessage(getString(R.string.geopackage_share_copy_message));
            shareCopyDialog.setCancelable(false);
            shareCopyDialog.setIndeterminate(true);
            shareCopyDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                    getString(R.string.button_cancel_label),
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
                GeoPackageUtils.showMessage(GeoPackageDetail.this,
                        getString(R.string.geopackage_share_label), result);
            } else {
                // Create the content Uri and add intent permissions
                Uri databaseUri = FileProvider.getUriForFile(GeoPackageDetail.this,
                        AUTHORITY,
                        cacheFile);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                launchShareIntent(shareIntent, databaseUri);
            }
        }

    }



    /**
     * Get the database cache directory
     *
     * @return
     */
    private File getDatabaseCacheDirectory() {
        return new File(this.getCacheDir(), "databases");
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




    /**
     * makes a string plural based on the count
     * @param count
     * @param text singular word
     * @return
     */
    private String pluralize(int count, String text){
        if(count == 1){
            return text;
        } else {
            return text + "s";
        }
    }
}
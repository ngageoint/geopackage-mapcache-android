package mil.nga.mapcache;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;

/**
 * Detail page after clicking on a GeoPackage in the manage view
 */
public class GeoPackageDetail extends AppCompatActivity {

    private GeoPackageManager manager;
    private GeoPackage selectedGeo;
    private String geoPackageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = GeoPackageFactory.getManager(getBaseContext());

        setContentView(R.layout.activity_geo_package_detail);
        Intent intent = getIntent();

        // Get the geopackage
        geoPackageName = intent.getStringExtra(GeoPackageManagerFragment.GEO_PACKAGE_DETAIL);
        selectedGeo = manager.open(geoPackageName, false);

        // Set details
        TextView nameText = (TextView) findViewById(R.id.text_name);
        TextView sizeText = (TextView) findViewById(R.id.text_size);
        TextView tileText = (TextView) findViewById(R.id.text_tiles);
        TextView featureText = (TextView) findViewById(R.id.text_features);

        nameText.setText(selectedGeo.getName());
        sizeText.setText(manager.readableSize(geoPackageName));
        int tileCount = selectedGeo.getTileTables().size();
        int featureCount = selectedGeo.getFeatureTables().size();
        tileText.setText(tileCount + " " + pluralize(tileCount, "Tile layer"));
        featureText.setText(featureCount + " " + pluralize(featureCount, "Feature layer"));

        // Set listeners for geopackage action buttons
        Button renameButton = (Button) findViewById(R.id.detail_rename);
        renameButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                renameDatabaseOption(geoPackageName);
            }
        });

    }

    /**
     * Rename database dialog window
     *
     * @param database
     */
    private void renameDatabaseOption(final String database) {

        final EditText input = new EditText(getApplicationContext());
        input.setText(database);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.geopackage_rename_label);
        dialogBuilder.setView(input);
        dialogBuilder.setPositiveButton(R.string.button_ok_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (value != null && !value.isEmpty()
                        && !value.equals(database)) {
                    try{
                        if(manager.rename(database, value)) {
                            geoPackageName = value;
                            update();
                            Toast.makeText(GeoPackageDetail.this,"Renamed " + database, Toast.LENGTH_SHORT).show();
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
     *   Update the currently loaded geopackage data for page display
     */
    private void update(){
        selectedGeo = manager.open(geoPackageName, false);
        TextView nameText = (TextView) findViewById(R.id.text_name);
        nameText.setText(selectedGeo.getName());
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

package mil.nga.mapcache;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;

/**
 * Detail page after clicking on a GeoPackage in the manage view
 */
public class GeoPackageDetail extends AppCompatActivity {

    private GeoPackageManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = GeoPackageFactory.getManager(getBaseContext());

        setContentView(R.layout.activity_geo_package_detail);
        Intent intent = getIntent();

        // Get the geopackage
        String geoPackageName = intent.getStringExtra(GeoPackageManagerFragment.GEO_PACKAGE_DETAIL);
        GeoPackage selectedGeo = manager.open(geoPackageName, false);

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

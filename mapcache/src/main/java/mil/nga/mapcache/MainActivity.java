package mil.nga.mapcache;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.Arrays;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.TrackerConfig;
import org.piwik.sdk.extra.TrackHelper;

import mil.nga.mapcache.io.MapCacheFileUtils;

/**
 * Main Activity
 *
 * @author osbornb
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Map permissions request code for accessing fine locations
     */
    public static final int MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    /**
     * Map fragment
     */
    private GeoPackageMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view
        setContentView(R.layout.activity_main);

        // Retrieve the fragments
        mapFragment = (GeoPackageMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);

        // Set up the map fragment
        createMainFragment();

        hideActionBar();

        // Handle opening and importing GeoPackages
        if(getIntent() != null) {
            Intent intent = getIntent();
            if (checkIntentDataForGpkgImport(intent)) {
                mapFragment.showGpkgImportFromFileDialog(intent);
            }
        }

        String siteUrl = getString(R.string.matomo_url);
        int siteId = getResources().getInteger(R.integer.matomo_site_id);
        Tracker piWik = Piwik.getInstance(getApplicationContext()).newTracker(new TrackerConfig(siteUrl, siteId, "MapCacheTracker"));
        TrackHelper.track().screen("/Main Activity").title("App Opened").with(piWik);
        piWik.dispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (checkIntentDataForGpkgImport(intent)) {
            mapFragment.showGpkgImportFromFileDialog(intent);
        }
    }

    //check if intent is populated with a mime type contained in the gpkg mime type list
    private boolean checkIntentDataForGpkgImport(Intent intent) {
        boolean isGpkgData = false;

        if (intent != null && !TextUtils.isEmpty(intent.getType()) && intent.getData() != null) {
            String mimeTypesListLowerCase = Arrays.toString(MapCacheFileUtils.INSTANCE.getGpkgMimeTypes()).toLowerCase();
            if (mimeTypesListLowerCase.contains(intent.getType().toLowerCase())) {
                isGpkgData = true;
            }
        }

        return isGpkgData;
    }

    /**
     * Set up the map fragment
     */
    private void createMainFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(mapFragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Hide the action bar
     */
    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mapFragment.handleMenuClick(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if permission was granted
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if(granted && requestCode == MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            mapFragment.setMyLocationEnabled();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapCacheApplication.Companion.deleteCacheFiles();
    }
}

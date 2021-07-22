package mil.nga.mapcache;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.TrackerConfig;
import org.piwik.sdk.extra.TrackHelper;

import mil.nga.mapcache.io.MapCacheFileUtils;

//import org.matomo.sdk.Matomo;
//import org.matomo.sdk.TrackMe;
//import org.matomo.sdk.Tracker;
//import org.matomo.sdk.TrackerBuilder;
//import org.matomo.sdk.extra.MatomoApplication;
//import org.matomo.sdk.extra.TrackHelper;

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
     * Manager permissions request code for importing a GeoPackage as an external link
     */
    public static final int MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL = 200;

    /**
     * Manager permissions request code for reading / writing to GeoPackages already externally linked
     */
    public static final int MANAGER_PERMISSIONS_REQUEST_ACCESS_EXISTING_EXTERNAL = 201;

    /**
     * Manager permissions request code for exporting a GeoPackage to external storage
     */
    public static final int MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE = 202;

    /**
     * Used to store the last screen title. For use in
     * {@link #hideActionBar()}.
     */
    private CharSequence title;

    /**
     * Map fragment
     */
    private GeoPackageMapFragment mapFragment;

    /**
     * Manager fragment
     */
//    private GeoPackageManagerFragment managerFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view
        setContentView(R.layout.activity_main);

        // Retrieve the fragments
//        managerFragment = (GeoPackageManagerFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.fragment_manager);
        mapFragment = (GeoPackageMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);

        // Set up the map fragment
        createMainFragment();

        hideActionBar();

        // Handle opening and importing GeoPackages
        if(getIntent() != null) {
            Intent intent = getIntent();
            Uri uri = intent.getData();
            if (uri == null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object objectUri = bundle.get(Intent.EXTRA_STREAM);
                    if (objectUri != null) {
                        uri = (Uri) objectUri;
                    }
                }
            }
            if (uri != null) {
                handleIntentUri(uri, intent);
            }
        }

        /**
         * Use Matomo to track when users open the app
         */
        String siteUrl = getString(R.string.matomo_url);
        int siteId = getResources().getInteger(R.integer.matomo_site_id);
        Tracker piwik = Piwik.getInstance(getApplicationContext()).newTracker(new TrackerConfig(siteUrl, siteId, "MapCacheTracker"));
        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        TrackHelper.track().screen("/Main Activity").title("App Opened").with(piwik);
        piwik.dispatch();

    }


    /**
     * Set up the map fragment
     */
    private void createMainFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(mapFragment);
        title = getString(R.string.title_map);
        transaction.commit();
    }



    /**
     * Handle the URI from an intent for opening or importing a GeoPackage
     *
     * @param uri intent uri
     */
    private void handleIntentUri(final Uri uri, Intent intent) {
        String path = FileUtils.getPath(this, uri);
        String name = MapCacheFileUtils.getDisplayName(this, uri, path);
        try {
            if (path != null) {
//                managerFragment.importGeoPackageExternalLinkWithPermissions(name, uri, path);
                mapFragment.startImportTaskWithPermissions(name, uri, path, intent);

            } else {
//                managerFragment.importGeoPackage(name, uri, path);
                mapFragment.startImportTask(name, uri, path, intent);
            }
        } catch (final Exception e) {
            try {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                GeoPackageUtils.showMessage(MainActivity.this,
                                        "Open GeoPackage",
                                        "Could not open file as a GeoPackage"
                                                + "\n\n"
                                                + e.getMessage());
                            }
                        });
            } catch (Exception e2) {
                // eat
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Hide the action bar
     */
    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(title);
        actionBar.hide();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mapFragment.handleMenuClick(item)) {
            return true;
        }
//        if (managerFragment.handleMenuClick(item)) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        // Check if permission was granted
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        switch(requestCode) {

            case MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                mapFragment.setMyLocationEnabled();
                break;

            case MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL:
                mapFragment.importGeopackageFromFile();
                break;

            case MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE:
                mapFragment.exportGeoPackageToExternal();
                break;

            case MANAGER_PERMISSIONS_REQUEST_ACCESS_EXISTING_EXTERNAL:
//                managerFragment.update(granted);
                break;

        }
    }

}

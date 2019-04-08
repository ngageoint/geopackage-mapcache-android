package mil.nga.mapcache;

import androidx.lifecycle.ViewModelProviders;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.ipaulpro.afilechooser.utils.FileUtils;

import mil.nga.mapcache.io.MapCacheFileUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * Main Activity
 *
 * @author osbornb
 */
public class MainActivity extends AppCompatActivity { //,
        //NavigationDrawerFragment.NavigationDrawerCallbacks


    /**
     * Manager drawer position
     */
    private static final int MANAGER_POSITION = 0;

    /**
     * Map drawer position
     */
    private static final int MAP_POSITION = 1;

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
     * Permission request from the geopackage detail page to export a database
     */
    public static final int DETAIL_FRAGMENT_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE = 401;


    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
//    private NavigationBarFragment navigationBarFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #hideActionBar()}.
     */
    private CharSequence title;

    /**
     * Current drawer position
     */
    private int navigationPosition = MANAGER_POSITION;

    /**
     * Map fragment
     */
    private GeoPackageMapFragment mapFragment;

    /**
     * Manager fragment
     */
//    private GeoPackageManagerFragment managerFragment;

    private GeoPackageViewModel geoPackageViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        geoPackageViewModel = ViewModelProviders.of(this).get(GeoPackageViewModel.class);

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


//        navigationBarFragment = (NavigationBarFragment) getFragmentManager()
//                .findFragmentById(R.id.navigation_bar);
//        BottomNavigationView bottomNav = findViewById(R.id.navigation_bar);
//        bottomNav.setOnNavigationItemSelectedListener(
//                new BottomNavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                        switch(item.getItemId()){
//                            case R.id.MapButton:
//                                onNavBarClick(MAP_POSITION);
//                                break;
//                            case R.id.ManageButton:
//                                onNavBarClick(MANAGER_POSITION);
//                                break;
//                        }
//                        return true;
//                    }
//                }
//        );

        // Set the initial navigation position
//        bottomNav.setSelectedItemId(R.id.MapButton);
//        onNavBarClick(MAP_POSITION);
//        bottomNav.setSelectedItemId(R.id.MapButton);
//        onNavBarClick(MAP_POSITION);

//        // Set up the drawer.
//        navigationDrawerFragment.setUp(R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));


        // Handle opening and importing GeoPackages
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if(uri == null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Object objectUri = bundle.get(Intent.EXTRA_STREAM);
                if(objectUri != null){
                    uri = (Uri)objectUri;
                }
            }
        }
        if (uri != null) {
            handleIntentUri(uri, intent);
        }
    }


    /**
     * Set up the map fragment
     */
    private void createMainFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(mapFragment);
        title = getString(R.string.title_map);
//        if (managerFragment != null && managerFragment.isAdded()) {
//            transaction.hide(managerFragment);
//        }
        navigationPosition = MAP_POSITION;
        transaction.commit();
    }



    /**
     * Handle the URI from an intent for opening or importing a GeoPackage
     *
     * @param uri
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



//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        if (!navigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
////            getMenuInflater().inflate(R.menu.main, menu);
//
////            if (navigationPosition != MANAGER_POSITION) {
////                menu.setGroupVisible(R.id.menu_group_list, false);
////            }
////            if (navigationPosition != MAP_POSITION) {
////                menu.setGroupVisible(R.id.menu_group_map, false);
////            } else if (mapFragment != null) {
////                mapFragment.handleMenu(menu);
////            }
//
////            hideActionBar();
//            return true;
////        }
//        //return super.onCreateOptionsMenu(menu);
//    }

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
//                managerFragment.importGeoPackageExternalLinkAfterPermissionGranted(granted);
//                mapFragment.importGeoPackageExternalLinkAfterPermissionGranted(granted);
                mapFragment.importGeopackageFromFile();
                break;

            case MANAGER_PERMISSIONS_REQUEST_ACCESS_EXISTING_EXTERNAL:
//                managerFragment.update(granted);
                break;

            case DETAIL_FRAGMENT_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE:
                // Tell the mapFragment to find the open detail fragment and share the GP
                mapFragment.giveSharePermissions(requestCode, permissions, grantResults);
                break;

                // old export option
            //case MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE:
                //managerFragment.exportDatabaseAfterPermission(granted);
                //break;
        }
    }

//    @Override
//    public void onNavBarClick(int position) {
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//
//        switch (position) {
//
//            case MANAGER_POSITION:
//                if (managerFragment != null) {
//                    transaction.show(managerFragment);
//                    title = getString(R.string.title_manager);
//                }
//                break;
//            case MAP_POSITION:
//                if (mapFragment != null) {
//                    transaction.show(mapFragment);
//                    title = getString(R.string.title_map);
//                }
//                break;
//            default:
//
//        }
//
//        if (position != MANAGER_POSITION) {
//            if (managerFragment != null && managerFragment.isAdded()) {
//                transaction.hide(managerFragment);
//            }
//        }
//        if (position != MAP_POSITION) {
//            if (mapFragment != null && mapFragment.isAdded()) {
//                transaction.hide(mapFragment);
//            }
//        }
//
//        navigationPosition = position;
//
//        transaction.commit();
//    }
}

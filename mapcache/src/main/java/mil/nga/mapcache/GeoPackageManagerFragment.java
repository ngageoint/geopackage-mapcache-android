package mil.nga.mapcache;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract.Document;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.locationtech.proj4j.units.Units;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.extension.nga.link.FeatureTileLink;
import mil.nga.geopackage.extension.nga.link.FeatureTileTableLinker;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.extension.nga.scale.TileTableScaling;
import mil.nga.geopackage.extension.nga.style.FeatureTableStyles;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTableMetadata;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileTableMetadata;
import mil.nga.geopackage.user.UserColumn;
import mil.nga.geopackage.user.UserTable;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTableType;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.filter.InputFilterMinMax;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.indexer.IndexerTask;
import mil.nga.mapcache.io.MapCacheFileUtils;
import mil.nga.mapcache.listeners.GeoPackageClickListener;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.GeometryType;
import mil.nga.sf.wkb.GeometryCodes;

/**
 * GeoPackage Manager Fragment
 *
 * @author osbornb
 */
public class GeoPackageManagerFragment extends Fragment implements
        ILoadTilesTask, IIndexerTask {

    /**
     * Intent activity request code when choosing a file
     */
    public static final int ACTIVITY_CHOOSE_FILE = 3342;

    /**
     * Intent activity request code when sharing a file
     */
    public static final int ACTIVITY_SHARE_FILE = 3343;

    /**
     * Intent activity request code when opening app settings
     */
    public static final int ACTIVITY_APP_SETTINGS = 3344;

    /**
     * Active GeoPackages
     */
    private GeoPackageDatabases active;

    /**
     * List of databases
     */
    private List<String> databases = new ArrayList<String>();

    /**
     * List of database tables within each database
     */
    private List<List<GeoPackageTable>> databaseTables = new ArrayList<List<GeoPackageTable>>();

    /**
     * Layout inflater
     */
    private LayoutInflater inflater;

    /**
     * GeoPackage manager
     */
    private GeoPackageManager manager;

    /**
     * Progress dialog for network operations
     */
    private ProgressDialog progressDialog;

    /**
     * Import external GeoPackage name holder when asking for external write permission
     */
    private String importExternalName;

    /**
     * Import external GeoPackage URI holder when asking for external write permission
     */
    private Uri importExternalUri;

    /**
     * Import external GeoPackage path holder when asking for external write permission
     */
    private String importExternalPath;

    /**
     * Export GeoPackage database name holder when asking for external write permission
     */
    private String exportDatabaseName;

    /**
     * Flag used to track the first time hidden external GeoPackages exist to warn the user
     */
    private boolean hiddenExternalWarning = true;

    /**
     * View holding the recyler view list of geopackages
     */
    private RecyclerView geoPackageRecyclerView;

    /**
     * Text view to show "no geopackages found" message when the list is empty
     */
    private TextView emptyView;

    /**
     * main view
     */
    private View v;

    /**
     * GeoPackage name constant
     */
    public static final String GEO_PACKAGE_DETAIL = "mil.nga.mapcache.extra.GEOPACKAGEDETAIL";


    List<List<GeoPackageTable>> geoPackageData = new ArrayList<List<GeoPackageTable>>();

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".fileprovider";

    private GeoPackageViewModel geoPackageViewModel;


    /**
     * Constructor
     */
    public GeoPackageManagerFragment() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        active = GeoPackageDatabases.getInstance(getActivity());
        this.inflater = inflater;
        manager = GeoPackageFactory.getManager(getActivity());
        v = inflater.inflate(R.layout.fragment_manager, null, false);

        // Floating action button
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGeoPackage();
            }
        });

        // Listener for clicking on a geopackage, sends you to the detail activity with the geopackage name
        GeoPackageClickListener listener = new GeoPackageClickListener() {
            @Override
            public void onClick(View view, int position, GeoPackageDatabase name) {
//                Intent detailIntent = new Intent(v.getContext(), GeoPackageDetail.class);
//                String geoPackageName = databases.get(position);
//                detailIntent.putExtra(GEO_PACKAGE_DETAIL, geoPackageName);
//
//                startActivity(detailIntent);
//                update();
            }
        };
        geoPackageRecyclerView = (RecyclerView) v.findViewById(R.id.listings_view);
        geoPackageRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        geoPackageViewModel = ViewModelProviders.of(getActivity()).get(GeoPackageViewModel.class);
        update();

        return v;
    }

    /**
     * Sets the visibility of the recycler view vs "no geopackages found" message bases on the
     * recycler view being empty
     */
    private void setListVisibility(){
        emptyView = (TextView) v.findViewById(R.id.empty_view);
        if(databases.isEmpty()){
            geoPackageRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else{
            geoPackageRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        update();
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && active.isModified()) {
            active.setModified(false);
            update();
        }
    }

    /**
     * Update the listing of databases and tables
     */
    public void update() {

        // If there are no external GeoPackages or if we have external storage permission, update the database list with all GeoPackages
        if (manager.externalCount() == 0 || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            databases = manager.databases();
            updateWithCurrentDatabaseList();
        } else {
            // Should we justify why we need permission?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(R.string.storage_access_rational_title)
                        .setMessage(R.string.storage_access_rational_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Request permission
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_EXISTING_EXTERNAL);
                            }
                        })
                        .create()
                        .show();

            } else {
                // Request permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_EXISTING_EXTERNAL);
            }
        }

        // Set the visibility of the recycler list
        setListVisibility();

    }

    /**
     * Update the listing of databases and tables, including external GeoPackages only if set to true
     *
     * @param includeExternal true to include external GeoPackages
     */
    public void update(boolean includeExternal) {

        if (includeExternal) {
            databases = manager.databases();
        } else {
            // We don't have permission to show external databases

            showDisabledExternalGeoPackagesPermissionsDialog();
            databases = manager.internalDatabases();

            // Disable any active external databases
            for (String externalDatabase : manager.externalDatabaseSet()) {
                active.removeDatabase(externalDatabase, true);
            }
        }
        updateWithCurrentDatabaseList();
    }

    /**
     * Update the listing of databases and tables
     */
    private void updateWithCurrentDatabaseList() {
        databaseTables.clear();
//        geoAdapter.clear();
        StringBuilder errorMessage = new StringBuilder();
        Iterator<String> databasesIterator = databases.iterator();
        while (databasesIterator.hasNext()) {
            String database = databasesIterator.next();

            // Delete any databases with invalid headers
            if (!manager.validateHeader(database)) {
                if (manager.delete(database)) {
                    databasesIterator.remove();
                }
            } else {

                // Read the feature and tile tables from the GeoPackage
                List<Exception> exceptions = new ArrayList<>();
                GeoPackage geoPackage = null;
                List<GeoPackageTable> tables = new ArrayList<GeoPackageTable>();
                try {
                    geoPackage = manager.open(database, false);
                    ContentsDao contentsDao = geoPackage.getContentsDao();

                    List<String> featureTables = null;
                    try {
                        featureTables = geoPackage.getFeatureTables();
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                    if (featureTables != null) {
                        try {
                            for (String tableName : featureTables) {
                                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                                int count = featureDao.count();

                                GeometryType geometryType = null;
                                try {
                                    Contents contents = contentsDao.queryForId(tableName);
                                    GeometryColumns geometryColumns = contents
                                            .getGeometryColumns();
                                    geometryType = geometryColumns.getGeometryType();
                                } catch (Exception e) {
                                    Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
                                }

                                GeoPackageTable table = new GeoPackageFeatureTable(database,
                                        tableName, geometryType, count);
                                //table.setActive(active.exists(table));
                                tables.add(table);
                            }
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

                    List<String> tileTables = null;
                    try {
                        tileTables = geoPackage.getTileTables();
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                    if (tileTables != null) {
                        try {
                            for (String tableName : tileTables) {
                                TileDao tileDao = geoPackage.getTileDao(tableName);
                                int count = tileDao.count();
                                GeoPackageTable table = new GeoPackageTileTable(database,
                                        tableName, count);
                                //table.setActive(active.exists(table));
                                tables.add(table);
                            }
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }

                if (geoPackage != null) {
                    geoPackage.close();
                }

                // If There are no tables under the database, create a blank table so that we can at
                // least pass the database name up to the recycler view
                if(tables.isEmpty()){
                    GeoPackageTable table = new GeoPackageFeatureTable(database, "", GeometryType.GEOMETRY, 0);
                    tables.add(table);
                }

                if (exceptions.isEmpty()) {
                    databaseTables.add(tables);
//                    geoAdapter.insertToEnd(tables);
                } else {

                    // On exception, check the integrity of the database and delete if not valid
                    if (!manager.validateIntegrity(database) && manager.delete(database)) {
                        databasesIterator.remove();
                    } else {
                        databaseTables.add(tables);
//                        geoAdapter.insertToEnd(tables);
                    }

                    if (errorMessage.length() > 0) {
                        errorMessage.append("\n\n\n");
                    }
                    errorMessage.append(database).append(" Errors:");
                    for (Exception exception : exceptions) {
                        errorMessage.append("\n\n");
                        errorMessage.append(exception.getMessage());
                    }
                }
            }
        }

        if (errorMessage.length() > 0) {
            GeoPackageUtils
                    .showMessage(
                            getActivity(),
                            "GeoPackage Errors",
                            errorMessage.toString());
        }

//        geoPackageViewModel.setGeoPackageTables(databaseTables);
//        List<GeoPackage> geoPackages = new ArrayList<>();
//        geoPackageViewModel.setGeoPackages(geoPackages);
//        geoPackageViewModel.generateGeoPackageList();
//        geoPackageViewModel.init();
//        geoAdapter.notifyDataSetChanged();
    }

    /**
     * Show a disabled external GeoPackages permissions dialog when external GeoPackages exist that can not be accessed
     */
    private void showDisabledExternalGeoPackagesPermissionsDialog() {
        // If the user has declared to no longer get asked about permissions and we haven't notified them that there are hidden GeoPackages
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) && hiddenExternalWarning) {
            hiddenExternalWarning = false;
            showDisabledPermissionsDialog(
                    getResources().getString(R.string.external_geopackage_access_title),
                    getResources().getString(R.string.external_geopackage_access_message));
        }
    }

    /**
     * Show a disabled external export permissions dialog when external GeoPackages can not be exported
     */
    private void showDisabledExternalExportPermissionsDialog() {
        // If the user has declared to no longer get asked about permissions
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showDisabledPermissionsDialog(
                    getResources().getString(R.string.external_export_geopackage_access_title),
                    getResources().getString(R.string.external_export_geopackage_access_message));
        }
    }

    /**
     * Show a disabled external import permissions dialog when external GeoPackages can not be imported
     */
    private void showDisabledExternalImportPermissionsDialog() {
        // If the user has declared to no longer get asked about permissions
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showDisabledPermissionsDialog(
                    getResources().getString(R.string.external_import_geopackage_access_title),
                    getResources().getString(R.string.external_import_geopackage_access_message));
        }
    }

    /**
     * Show a disabled permissions dialog
     *
     * @param title
     * @param message
     */
    private void showDisabledPermissionsDialog(String title, String message) {
        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.settings, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                        startActivityForResult(intent, ACTIVITY_APP_SETTINGS);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * View database information
     *
     * @param database
     */
    private void viewDatabaseOption(final String database) {
        StringBuilder databaseInfo = new StringBuilder();
        GeoPackage geoPackage = manager.open(database, false);
        try {
            SpatialReferenceSystemDao srsDao = geoPackage
                    .getSpatialReferenceSystemDao();

            List<SpatialReferenceSystem> srsList = srsDao.queryForAll();
            databaseInfo.append("Size: ")
                    .append(manager.readableSize(database));
            databaseInfo.append("\n\nLocation: ").append(
                    manager.isExternal(database) ? "External" : "Local");
            databaseInfo.append("\nPath: ").append(manager.getPath(database));
            databaseInfo.append("\n\nFeature Tables: ").append(
                    geoPackage.getFeatureTables().size());
            databaseInfo.append("\nTile Tables: ").append(
                    geoPackage.getTileTables().size());
            databaseInfo.append("\n\nSpatial Reference Systems: ").append(
                    srsList.size());
            for (SpatialReferenceSystem srs : srsList) {
                databaseInfo.append("\n");
                addSrs(databaseInfo, srs);
            }

        } catch (Exception e) {
            databaseInfo.append(e.getMessage());
        } finally {
            geoPackage.close();
        }
        AlertDialog viewDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(database)
                .setPositiveButton(getString(R.string.button_ok_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage(databaseInfo.toString()).create();
        viewDialog.show();
    }

    /**
     * Add Spatial Reference System to the info
     *
     * @param info
     * @param srs
     */
    private void addSrs(StringBuilder info, SpatialReferenceSystem srs) {
        info.append("\nSRS Name: ").append(srs.getSrsName());
        info.append("\nSRS ID: ").append(srs.getSrsId());
        info.append("\nOrganization: ").append(srs.getOrganization());
        info.append("\nCoordsys ID: ").append(srs.getOrganizationCoordsysId());
        info.append("\nDefinition: ").append(srs.getDefinition());
        info.append("\nDescription: ").append(srs.getDescription());
    }

    /**
     * Delete database alert option
     *
     * @param database
     */
    private void deleteDatabaseOption(final String database) {
        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.geopackage_delete_label))
                .setMessage(
                        getString(R.string.geopackage_delete_label) + " "
                                + database + "?")
                .setPositiveButton(getString(R.string.geopackage_delete_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                manager.delete(database);
                                active.removeDatabase(database, false);
                                update();
                            }
                        })

                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();
        deleteDialog.show();
    }

    /**
     * Rename database option
     *
     * @param database
     */
    private void renameDatabaseOption(final String database) {

        final EditText input = new EditText(getActivity());
        input.setText(database);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.geopackage_rename_label))
                .setView(input)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                if (value != null && !value.isEmpty()
                                        && !value.equals(database)) {
                                    try {
                                        if (manager.rename(database, value)) {
                                            active.renameDatabase(database,
                                                    value);
                                            update();
                                        } else {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            getActivity(),
                                                            getString(R.string.geopackage_rename_label),
                                                            "Rename from "
                                                                    + database
                                                                    + " to "
                                                                    + value
                                                                    + " was not successful");
                                        }
                                    } catch (Exception e) {
                                        GeoPackageUtils
                                                .showMessage(
                                                        getActivity(),
                                                        getString(R.string.geopackage_rename_label),
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
     * Copy database option
     *
     * @param database
     */
    private void copyDatabaseOption(final String database) {

        final EditText input = new EditText(getActivity());
        input.setText(database + getString(R.string.geopackage_copy_suffix));

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.geopackage_copy_label))
                .setView(input)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                if (value != null && !value.isEmpty()
                                        && !value.equals(database)) {
                                    try {
                                        if (manager.copy(database, value)) {
                                            update();
                                        } else {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            getActivity(),
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
                                                        getActivity(),
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
     * Export database option
     *
     * @param database
     */
    private void exportDatabaseOptionCheckPermissions(final String database) {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportDatabaseOption(database);
        } else {
            exportDatabaseName = database;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE);
        }

    }

    /**
     * Export database option after given permission
     *
     * @param granted true if permission was granted
     */
    public void exportDatabaseAfterPermission(boolean granted) {
        if (granted) {
            exportDatabaseOption(exportDatabaseName);
        } else {
            showDisabledExternalExportPermissionsDialog();
        }
    }

    /**
     * Export database option
     *
     * @param database
     */
    private void exportDatabaseOption(final String database) {

        final File directory = Environment.getExternalStorageDirectory();
        final EditText input = new EditText(getActivity());
        input.setText(database);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.geopackage_export_label))
                .setMessage(directory.getPath() + File.separator)
                .setView(input)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                if (value != null && !value.isEmpty()) {
                                    try {
                                        manager.exportGeoPackage(database,
                                                value, directory);
                                    } catch (Exception e) {
                                        GeoPackageUtils
                                                .showMessage(
                                                        getActivity(),
                                                        getString(R.string.geopackage_export_label),
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
     * Share database option
     *
     * @param database
     */
    private void shareDatabaseOption(final String database) {

        try {
            // Get the database file
            File databaseFile = manager.getFile(database);

            // Create the share intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("*/*");

            // If external database, no permission is needed
            if (manager.isExternal(database)) {
                // Create the Uri and share
                Uri databaseUri = FileProvider.getUriForFile(getActivity(),
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
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_share_label), e.getMessage());
        }
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
            shareCopyDialog = new ProgressDialog(getActivity());
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
                    + GeoPackageConstants.EXTENSION);
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
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_share_label), result);
            } else {
                // Create the content Uri and add intent permissions
                Uri databaseUri = FileProvider.getUriForFile(getActivity(),
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
        return new File(getActivity().getCacheDir(), "databases");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesCancelled(String result) {
        update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesPostExecute(String result) {
        if (result != null) {
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_create_tiles_label), result);
        }
        update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIndexerCancelled(String result) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIndexerPostExecute(String result) {
        if (result != null) {
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_table_index_features_label), result);
        }
    }

    /**
     * validate input
     * @param inputLayout
     * @return true if input is not empty and is valid
     */
    private boolean validateInput(TextInputLayout inputLayout, TextInputEditText inputName){
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayout.setError(inputLayout.getHint() + " " + getString(R.string.err_msg_invalid));
            return false;
        }
        return true;
    }



    /**
     * Create a download progress dialog
     *
     * @param database
     * @param url
     * @param downloadTask
     * @param suffix
     * @return
     */
    private ProgressDialog createDownloadProgressDialog(String database,
                                                        String url, final DownloadTask downloadTask, String suffix) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.geopackage_import_label) + " "
                + database + "\n\n" + url + (suffix != null ? suffix : ""));
        dialog.setCancelable(false);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadTask.cancel(true);
                    }
                });
        return dialog;
    }

    /**
     * Download a GeoPackage from a URL in the background
     */
    private class DownloadTask extends AsyncTask<String, Integer, String>
            implements GeoPackageProgress {

        private Integer max = null;
        private int progress = 0;
        private final String database;
        private final String url;
        private PowerManager.WakeLock wakeLock;

        /**
         * Constructor
         *
         * @param database
         * @param url
         */
        public DownloadTask(String database, String url) {
            this.database = database;
            this.url = url;
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
            PowerManager pm = (PowerManager) getActivity().getSystemService(
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

                ProgressDialog newProgressDialog = createDownloadProgressDialog(
                        database, url, this, messageSuffix);
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
            update();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(String result) {
            wakeLock.release();
            progressDialog.dismiss();
            if (result != null) {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_import_label), result);
            }
            update();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                URL theUrl = new URL(url);
                if (!manager.importGeoPackage(database, theUrl, this)) {
                    return "Failed to import GeoPackage '" + database
                            + "' at url '" + url + "'";
                }
            } catch (final Exception e) {
                return e.toString();
            }
            return null;
        }

    }

    /**
     * Import a GeoPackage from a file
     */
    private void importGeopackageFromFile() {

        try {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            Intent intent = Intent.createChooser(chooseFile,
                    "Choose a GeoPackage file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        } catch (Exception e) {
            Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = true;

        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    importFile(data);
                }
                break;

            case ACTIVITY_SHARE_FILE:
                deleteCachedDatabaseFiles();
                break;

            case ACTIVITY_APP_SETTINGS:
                update();
                break;

            default:
                handled = false;
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
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

    /**
     * Import the GeoPackage file selected
     *
     * @param data
     */
    private void importFile(Intent data) {

        // Get the Uri
        final Uri uri = data.getData();

        // Try to get the file path and name
        final String path = FileUtils.getPath(getActivity(), uri);
        String name = MapCacheFileUtils.getDisplayName(getActivity(), uri, path);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View importFileView = inflater.inflate(R.layout.import_file, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(importFileView);

        final EditText nameInput = (EditText) importFileView
                .findViewById(R.id.import_file_name_input);
        final RadioButton copyRadioButton = (RadioButton) importFileView
                .findViewById(R.id.import_file_copy_radio_button);
        final RadioButton externalRadioButton = (RadioButton) importFileView
                .findViewById(R.id.import_file_external_radio_button);

        // Set the default name
        if (name != null) {
            nameInput.setText(name);
        }

        // If no file path could be found, disable the external link option
        if (path == null) {
            externalRadioButton.setEnabled(false);
        }

        dialog.setTitle(getString(R.string.geopackage_import_label))
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                String value = nameInput.getText().toString();
                                if (value != null && !value.isEmpty()) {

                                    boolean copy = copyRadioButton.isChecked();

                                    try {
                                        if (copy) {
                                            // Import the GeoPackage by copying the file
                                            importGeoPackage(value, uri, path);
                                        } else {
                                            // Import the GeoPackage by linking to the file
                                            importGeoPackageExternalLinkWithPermissions(value, uri, path);
                                        }
                                    } catch (final Exception e) {
                                        try {
                                            getActivity().runOnUiThread(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            GeoPackageUtils
                                                                    .showMessage(
                                                                            getActivity(),
                                                                            "File Import",
                                                                            "Uri: "
                                                                                    + uri.getPath()
                                                                                    + ", "
                                                                                    + e.getMessage());
                                                        }
                                                    });
                                        } catch (Exception e2) {
                                            Log.e(GeoPackageManagerFragment.class.getSimpleName(), e2.getMessage(), e2);
                                        }
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
     * Import the GeoPackage by linking to the file if write external storage permissions are granted, otherwise request permission
     *
     * @param name
     * @param uri
     * @param path
     */
    public void importGeoPackageExternalLinkWithPermissions(final String name, final Uri uri, String path) {

        // Check for permission
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            importGeoPackageExternalLink(name, uri, path);
        } else {

            // Save off the values and ask for permission
            importExternalName = name;
            importExternalUri = uri;
            importExternalPath = path;

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(R.string.storage_access_rational_title)
                        .setMessage(R.string.storage_access_rational_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL);
            }
        }

    }

    /**
     * Import the GeoPackage by linking to the file after write external storage permission was granted
     *
     * @param granted
     */
    public void importGeoPackageExternalLinkAfterPermissionGranted(boolean granted) {
        if (granted) {
            importGeoPackageExternalLink(importExternalName, importExternalUri, importExternalPath);
        } else {
            showDisabledExternalImportPermissionsDialog();
        }
    }

    /**
     * Import the GeoPackage by linking to the file
     *
     * @param name
     * @param uri
     * @param path
     */
    private void importGeoPackageExternalLink(final String name, final Uri uri, String path) {

        // Check if a database already exists with the name
        if (manager.exists(name)) {
            // If the existing is not an external file, error
            boolean alreadyExistsError = !manager.isExternal(name);
            if (!alreadyExistsError) {
                // If the existing external file has a different file path, error
                File existingFile = manager.getFile(name);
                alreadyExistsError = !(new File(path)).equals(existingFile);
            }
            if (alreadyExistsError) {
                try {
                    getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    GeoPackageUtils.showMessage(getActivity(),
                                            "GeoPackage Exists",
                                            "A different GeoPackage already exists with the name '" + name + "'");
                                }
                            });
                } catch (Exception e) {
                    Log.e(GeoPackageManagerFragment.class.getSimpleName(), e.getMessage(), e);
                }
            }
        } else {
            // Import the GeoPackage by linking to the file
            boolean imported = manager
                    .importGeoPackageAsExternalLink(
                            path, name);

            if (imported) {
                update();
            } else {
                try {
                    getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    GeoPackageUtils.showMessage(getActivity(),
                                            "URL Import",
                                            "Failed to import Uri: "
                                                    + uri.getPath());
                                }
                            });
                } catch (Exception e) {
                    Log.e(GeoPackageManagerFragment.class.getSimpleName(), e.getMessage(), e);
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
        if (manager.exists(name)) {
            try {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                GeoPackageUtils.showMessage(getActivity(),
                                        "GeoPackage Exists",
                                        "A GeoPackage already exists with the name '" + name + "'");
                            }
                        });
            } catch (Exception e) {
                Log.e(GeoPackageManagerFragment.class.getSimpleName(), e.getMessage(), e);
            }
        } else {

            ImportTask importTask = new ImportTask(name, path, uri);
            progressDialog = createImportProgressDialog(name,
                    importTask, path, uri, null);
            progressDialog.setIndeterminate(true);
            importTask.execute();
        }
    }

    /**
     * Create a import progress dialog
     *
     * @param database
     * @param importTask
     * @param path
     * @param uri
     * @param suffix
     * @return
     */
    private ProgressDialog createImportProgressDialog(String database, final ImportTask importTask,
                                                      String path, Uri uri,
                                                      String suffix) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.geopackage_import_label) + " "
                + database + "\n\n" + (path != null ? path : uri.getPath()) + (suffix != null ? suffix : ""));
        dialog.setCancelable(false);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importTask.cancel(true);
                    }
                });
        return dialog;
    }

    /**
     * Import a GeoPackage from a stream in the background
     */
    private class ImportTask extends AsyncTask<String, Integer, String>
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
        public ImportTask(String database, String path, Uri uri) {
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
            PowerManager pm = (PowerManager) getActivity().getSystemService(
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
            update();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(String result) {
            wakeLock.release();
            progressDialog.dismiss();
            if (result != null) {
                GeoPackageUtils.showMessage(getActivity(),
                        "Import",
                        "Failed to import: "
                                + (path != null ? path : uri.getPath()));
            } else {
                update();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                final ContentResolver resolver = getActivity().getContentResolver();
                InputStream stream = resolver.openInputStream(uri);
                if (!manager.importGeoPackage(database, stream, this)) {
                    return "Failed to import GeoPackage '" + database + "'";
                }
            } catch (final Exception e) {
                return e.toString();
            }
            return null;
        }

    }


    /**
     * Get display name from the uri
     *
     * @param resolver
     * @param uri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getDisplayName(ContentResolver resolver, Uri uri) {

        String name = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Cursor nameCursor = resolver.query(uri, null, null, null, null);
            try {
                if (nameCursor.getCount() > 0) {
                    int displayNameIndex = nameCursor
                            .getColumnIndex(Document.COLUMN_DISPLAY_NAME);
                    if (displayNameIndex >= 0 && nameCursor.moveToFirst()) {
                        name = nameCursor.getString(displayNameIndex);
                    }
                }
            } finally {
                nameCursor.close();
            }
        }

        return name;
    }

    /**
     * Create a new GeoPackage
     */
    private void createGeoPackage() {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_add);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Create GeoPackage");
        // GeoPackage name
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        inputName.setHint(getString(R.string.create_geopackage_hint));
        inputName.setSingleLine(true);
        inputName.setImeOptions(EditorInfo.IME_ACTION_DONE);

        final EditText input = new EditText(getActivity());

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = inputName.getText().toString();
                                if (value != null && !value.isEmpty()) {
                                    try {
                                        if (manager.create(value)) {
                                            update();
                                        } else {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            getActivity(),
                                                            getString(R.string.geopackage_create_label),
                                                            "Failed to create GeoPackage: "
                                                                    + value);
                                        }
                                    } catch (Exception e) {
                                        GeoPackageUtils.showMessage(
                                                getActivity(), "Create "
                                                        + value, e.getMessage());
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
     * Table Link Adapter for displaying linkable tables
     */
    private class TableLinkAdapter extends ArrayAdapter<String> {

        /**
         * Set of currently linked tables
         */
        private final Set<String> linkedTables;

        /**
         * Constructor
         *
         * @param context
         * @param resource
         * @param tables       tables that can be linked
         * @param linkedTables set of currently linked tables
         */
        public TableLinkAdapter(Context context, int resource,
                                List<String> tables,
                                Set<String> linkedTables) {
            super(context, resource, tables);
            this.linkedTables = linkedTables;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                view = inflater.inflate(R.layout.table_link_row, null);
            }

            final String linkTable = getItem(position);

            CheckBox checkBox = (CheckBox) view
                    .findViewById(R.id.table_link_row_checkbox);
            TextView tableName = (TextView) view
                    .findViewById(R.id.table_link_row_name);

            // Add or remove the table from being clicked on checkbox changes
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        linkedTables.add(linkTable);
                    } else {
                        linkedTables.remove(linkTable);
                    }
                }
            });

            // Set the initial values
            checkBox.setChecked(linkedTables.contains(linkTable));
            tableName.setText(linkTable);

            return view;
        }

    }

}

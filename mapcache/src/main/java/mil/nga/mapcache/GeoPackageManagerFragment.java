package mil.nga.mapcache;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract.Document;
import android.support.v4.content.FileProvider;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.user.UserColumn;
import mil.nga.geopackage.user.UserTable;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTableType;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.filter.InputFilterMinMax;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.indexer.IndexerTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.wkb.geom.GeometryType;

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
     * Active GeoPackages
     */
    private GeoPackageDatabases active;

    /**
     * Expandable list adapter
     */
    private GeoPackageListAdapter adapter = new GeoPackageListAdapter();

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
        active = GeoPackageDatabases.getInstance(getActivity());
        this.inflater = inflater;
        manager = GeoPackageFactory.getManager(getActivity());
        View v = inflater.inflate(R.layout.fragment_manager, null);
        ExpandableListView elv = (ExpandableListView) v
                .findViewById(R.id.fragment_manager_view_ui);
        elv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int childPosition = ExpandableListView
                            .getPackedPositionChild(id);
                    int groupPosition = ExpandableListView
                            .getPackedPositionGroup(id);
                    tableOptions(databaseTables.get(groupPosition).get(
                            childPosition));
                    return true;
                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView
                            .getPackedPositionGroup(id);
                    databaseOptions(databases.get(groupPosition));
                    return true;
                }
                return false;
            }
        });
        elv.setAdapter(adapter);

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        update();
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
        databases = manager.databases();
        databaseTables.clear();
        for (String database : databases) {
            GeoPackage geoPackage = manager.open(database);
            List<GeoPackageTable> tables = new ArrayList<GeoPackageTable>();
            ContentsDao contentsDao = geoPackage.getContentsDao();
            for (String tableName : geoPackage.getFeatureTables()) {
                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                int count = featureDao.count();

                GeometryType geometryType = null;
                try {
                    Contents contents = contentsDao.queryForId(tableName);
                    GeometryColumns geometryColumns = contents
                            .getGeometryColumns();
                    geometryType = geometryColumns.getGeometryType();
                } catch (Exception e) {
                }

                GeoPackageTable table = new GeoPackageFeatureTable(database,
                        tableName, geometryType, count);
                table.setActive(active.exists(table));
                tables.add(table);
            }
            for (String tableName : geoPackage.getTileTables()) {
                TileDao tileDao = geoPackage.getTileDao(tableName);
                int count = tileDao.count();
                GeoPackageTable table = new GeoPackageTileTable(database,
                        tableName, count);
                table.setActive(active.exists(table));
                tables.add(table);
            }
            for (GeoPackageFeatureOverlayTable table : active.featureOverlays(database)) {
                FeatureDao featureDao = geoPackage.getFeatureDao(table.getFeatureTable());
                int count = featureDao.count();
                table.setCount(count);
                tables.add(table);
            }
            databaseTables.add(tables);
            geoPackage.close();
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Show options for the GeoPackage database
     *
     * @param database
     */
    private void databaseOptions(final String database) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_item);
        adapter.add(getString(R.string.geopackage_view_label));
        adapter.add(getString(R.string.geopackage_delete_label));
        adapter.add(getString(R.string.geopackage_rename_label));
        adapter.add(getString(R.string.geopackage_copy_label));
        adapter.add(getString(R.string.geopackage_export_label));
        adapter.add(getString(R.string.geopackage_share_label));
        adapter.add(getString(R.string.geopackage_create_features_label));
        adapter.add(getString(R.string.geopackage_create_tiles_label));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(database);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                if (item >= 0) {

                    switch (item) {
                        case 0:
                            viewDatabaseOption(database);
                            break;
                        case 1:
                            deleteDatabaseOption(database);
                            break;
                        case 2:
                            renameDatabaseOption(database);
                            break;
                        case 3:
                            copyDatabaseOption(database);
                            break;
                        case 4:
                            exportDatabaseOption(database);
                            break;
                        case 5:
                            shareDatabaseOption(database);
                            break;
                        case 6:
                            createFeaturesOption(database);
                            break;
                        case 7:
                            createTilesOption(database);
                            break;
                        default:
                    }
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * View database information
     *
     * @param database
     */
    private void viewDatabaseOption(final String database) {
        StringBuilder databaseInfo = new StringBuilder();
        GeoPackage geoPackage = manager.open(database);
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

        } catch (SQLException e) {
            databaseInfo.append(e.getMessage());
        } finally {
            geoPackage.close();
        }
        AlertDialog viewDialog = new AlertDialog.Builder(getActivity())
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
        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity())
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
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
    private void exportDatabaseOption(final String database) {

        final File directory = Environment.getExternalStorageDirectory();
        final EditText input = new EditText(getActivity());
        input.setText(database);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
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
                Uri databaseUri = Uri.fromFile(databaseFile);
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
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_share_label), result);
            } else {
                // Create the content Uri and add intent permissions
                Uri databaseUri = FileProvider.getUriForFile(getActivity(),
                        "mil.nga.mapcache.fileprovider",
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
     * Create features option
     *
     * @param database
     */
    private void createFeaturesOption(final String database) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createFeaturesView = inflater.inflate(R.layout.create_features,
                null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(createFeaturesView);

        final EditText nameInput = (EditText) createFeaturesView
                .findViewById(R.id.create_features_name_input);
        final EditText minLatInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) createFeaturesView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner geometryTypeSpinner = (Spinner) createFeaturesView
                .findViewById(R.id.create_features_geometry_type);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        dialog.setPositiveButton(
                getString(R.string.geopackage_create_features_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_features_name_label)
                                                + " is required");
                            }
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    maxLon, minLat, maxLat);

                            GeometryType geometryType = GeometryType
                                    .fromName(geometryTypeSpinner
                                            .getSelectedItem().toString());

                            GeometryColumns geometryColumns = new GeometryColumns();
                            geometryColumns.setId(new TableColumnKey(tableName,
                                    "geom"));
                            geometryColumns.setGeometryType(geometryType);
                            geometryColumns.setZ((byte) 0);
                            geometryColumns.setM((byte) 0);

                            GeoPackage geoPackage = manager.open(database);
                            try {
                                geoPackage.createFeatureTableWithMetadata(
                                        geometryColumns, boundingBox, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                            } finally {
                                geoPackage.close();
                            }
                            update();

                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_features_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }

    /**
     * Create tiles option
     *
     * @param database
     */
    private void createTilesOption(final String database) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createTilesView = inflater.inflate(R.layout.create_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(createTilesView);

        final EditText nameInput = (EditText) createTilesView
                .findViewById(R.id.create_tiles_name_input);
        final EditText urlInput = (EditText) createTilesView
                .findViewById(R.id.load_tiles_url_input);
        final Button preloadedUrlsButton = (Button) createTilesView
                .findViewById(R.id.load_tiles_preloaded);
        final EditText minZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) createTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) createTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) createTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) createTilesView
                .findViewById(R.id.bounding_box_preloaded);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, preloadedUrlsButton, nameInput, urlInput,
                compressFormatInput, compressQualityInput, true, maxFeaturesLabel, maxFeaturesInput, false);

        dialog.setPositiveButton(
                getString(R.string.geopackage_create_tiles_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_tiles_name_label)
                                                + " is required");
                            }
                            String tileUrl = urlInput.getText().toString();
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    maxLon, minLat, maxLat);

                            // If not importing tiles, just create the table
                            if (tileUrl == null || tileUrl.isEmpty()) {
                                ProjectionTransform wgs84ToWebMercatorTransform = ProjectionFactory
                                        .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                                        .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
                                BoundingBox webMercatorBoundingBox = wgs84ToWebMercatorTransform.transform(boundingBox);
                                GeoPackage geoPackage = manager.open(database);
                                try {
                                    // Create the web mercator srs if needed
                                    SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
                                    srsDao.getOrCreate(ProjectionConstants.EPSG_WEB_MERCATOR);
                                    // Create the tile table
                                    geoPackage.createTileTableWithMetadata(
                                            tableName, boundingBox, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM,
                                            webMercatorBoundingBox, ProjectionConstants.EPSG_WEB_MERCATOR);
                                } finally {
                                    geoPackage.close();
                                }
                                update();
                            } else {
                                // Load tiles
                                LoadTilesTask.loadTiles(getActivity(),
                                        GeoPackageManagerFragment.this, active,
                                        database, tableName, tileUrl, minZoom,
                                        maxZoom, compressFormat,
                                        compressQuality, googleTiles,
                                        boundingBox);
                            }
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

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
     * Show options for the GeoPackage table
     *
     * @param table
     */
    private void tableOptions(final GeoPackageTable table) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_item);

        adapter.add(getString(R.string.geopackage_table_view_label));
        adapter.add(getString(R.string.geopackage_table_edit_label));
        adapter.add(getString(R.string.geopackage_table_delete_label));

        switch (table.getType()) {

            case FEATURE:
                adapter.add(getString(R.string.geopackage_table_index_features_label));
                adapter.add(getString(R.string.geopackage_table_create_feature_tiles_label));
                adapter.add(getString(R.string.geopackage_table_add_feature_overlay_label));
                break;

            case TILE:
                adapter.add(getString(R.string.geopackage_table_tiles_load_label));
                break;

            case FEATURE_OVERLAY:
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(table.getDatabase() + " - " + table.getName());
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                if (item >= 0) {

                    switch (item) {
                        case 0:
                            switch (table.getType()) {
                                case FEATURE:
                                case TILE:
                                case FEATURE_OVERLAY:
                                    viewTableOption(table);
                                    break;
                            }
                            break;
                        case 1:
                            switch (table.getType()) {
                                case FEATURE:
                                case TILE:
                                    editTableOption(table);
                                    break;
                                case FEATURE_OVERLAY:
                                    editFeatureOverlayTableOption((GeoPackageFeatureOverlayTable) table);
                                    break;
                            }
                            break;
                        case 2:
                            switch (table.getType()) {
                                case FEATURE:
                                case TILE:
                                case FEATURE_OVERLAY:
                                    deleteTableOption(table);
                                    break;
                            }
                            break;
                        case 3:
                            switch (table.getType()) {
                                case FEATURE:
                                    indexFeaturesOption(table);
                                    break;
                                case TILE:
                                    loadTilesTableOption(table);
                                    break;
                            }

                            break;
                        case 4:
                            switch (table.getType()) {
                                case FEATURE:
                                    createFeatureTilesTableOption(table);
                                    break;
                            }
                            break;
                        case 5:
                            switch (table.getType()) {
                                case FEATURE:
                                    addFeatureOverlayTableOption(table);
                                    break;
                            }
                            break;

                        default:
                    }
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * View table information
     *
     * @param table
     */
    private void viewTableOption(final GeoPackageTable table) {
        StringBuilder info = new StringBuilder();
        GeoPackage geoPackage = manager.open(table.getDatabase());
        String tableName = table.getName();
        try {
            Contents contents = null;
            FeatureDao featureDao = null;
            TileDao tileDao = null;
            UserTable<? extends UserColumn> userTable = null;

            switch (table.getType()) {

                case FEATURE_OVERLAY:
                    tableName = ((GeoPackageFeatureOverlayTable) table).getFeatureTable();
                case FEATURE:
                    featureDao = geoPackage.getFeatureDao(tableName);
                    contents = featureDao.getGeometryColumns().getContents();
                    info.append("Feature Table");
                    info.append("\nFeatures: ").append(featureDao.count());
                    userTable = featureDao.getTable();
                    break;

                case TILE:
                    tileDao = geoPackage.getTileDao(tableName);
                    contents = tileDao.getTileMatrixSet().getContents();
                    info.append("Tile Table");
                    info.append("\nZoom Levels: ").append(
                            tileDao.getTileMatrices().size());
                    info.append("\nTiles: ").append(tileDao.count());
                    userTable = tileDao.getTable();
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported table type: " + table.getType());
            }

            SpatialReferenceSystem srs = contents.getSrs();

            info.append("\n\nSpatial Reference System:");
            addSrs(info, srs);

            info.append("\n\nContents:");
            info.append("\nTable Name: ").append(contents.getTableName());
            info.append("\nData Type: ").append(contents.getDataType());
            info.append("\nIdentifier: ").append(contents.getIdentifier());
            info.append("\nDescription: ").append(contents.getDescription());
            info.append("\nLast Change: ").append(contents.getLastChange());
            info.append("\nMin X: ").append(contents.getMinX());
            info.append("\nMin Y: ").append(contents.getMinY());
            info.append("\nMax X: ").append(contents.getMaxX());
            info.append("\nMax Y: ").append(contents.getMaxY());

            if (featureDao != null) {
                GeometryColumns geometryColumns = featureDao
                        .getGeometryColumns();
                info.append("\n\nGeometry Columns:");
                info.append("\nTable Name: ").append(
                        geometryColumns.getTableName());
                info.append("\nColumn Name: ").append(
                        geometryColumns.getColumnName());
                info.append("\nGeometry Type Name: ").append(
                        geometryColumns.getGeometryTypeName());
                info.append("\nZ: ").append(geometryColumns.getZ());
                info.append("\nM: ").append(geometryColumns.getM());
            }

            if (tileDao != null) {
                TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();

                SpatialReferenceSystem tileMatrixSetSrs = tileMatrixSet.getSrs();
                if (tileMatrixSetSrs.getId() != srs.getId()) {
                    info.append("\n\nTile Matrix Set Spatial Reference System:");
                    addSrs(info, tileMatrixSetSrs);
                }

                info.append("\n\nTile Matrix Set:");
                info.append("\nTable Name: ").append(
                        tileMatrixSet.getTableName());
                info.append("\nMin X: ").append(tileMatrixSet.getMinX());
                info.append("\nMin Y: ").append(tileMatrixSet.getMinY());
                info.append("\nMax X: ").append(tileMatrixSet.getMaxX());
                info.append("\nMax Y: ").append(tileMatrixSet.getMaxY());

                info.append("\n\nTile Matrices:");
                for (TileMatrix tileMatrix : tileDao.getTileMatrices()) {
                    info.append("\n\nTable Name: ").append(
                            tileMatrix.getTableName());
                    info.append("\nZoom Level: ").append(
                            tileMatrix.getZoomLevel());
                    info.append("\nTiles: ").append(
                            tileDao.count(tileMatrix.getZoomLevel()));
                    info.append("\nMatrix Width: ").append(
                            tileMatrix.getMatrixWidth());
                    info.append("\nMatrix Height: ").append(
                            tileMatrix.getMatrixHeight());
                    info.append("\nTile Width: ").append(
                            tileMatrix.getTileWidth());
                    info.append("\nTile Height: ").append(
                            tileMatrix.getTileHeight());
                    info.append("\nPixel X Size: ").append(
                            tileMatrix.getPixelXSize());
                    info.append("\nPixel Y Size: ").append(
                            tileMatrix.getPixelYSize());
                }
            }

            info.append("\n\n").append(tableName).append(" columns:");
            for (UserColumn userColumn : userTable.getColumns()) {
                info.append("\n\nIndex: ").append(userColumn.getIndex());
                info.append("\nName: ").append(userColumn.getName());
                if (userColumn.getMax() != null) {
                    info.append("\nMax: ").append(userColumn.getMax());
                }
                info.append("\nNot Null: ").append(userColumn.isNotNull());
                if (userColumn.getDefaultValue() != null) {
                    info.append("\nDefault Value: ").append(
                            userColumn.getDefaultValue());
                }
                if (userColumn.isPrimaryKey()) {
                    info.append("\nPrimary Key: ").append(
                            userColumn.isPrimaryKey());
                }
                info.append("\nType: ").append(userColumn.getTypeName());
            }

        } catch (GeoPackageException e) {
            info.append(e.getMessage());
        } finally {
            geoPackage.close();
        }
        AlertDialog viewDialog = new AlertDialog.Builder(getActivity())
                .setTitle(table.getDatabase() + " - " + tableName)
                .setPositiveButton(getString(R.string.button_ok_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage(info.toString()).create();
        viewDialog.show();
    }

    /**
     * Edit table information
     *
     * @param table
     */
    private void editTableOption(final GeoPackageTable table) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        int editTableViewId = 0;
        switch (table.getType()) {

            case FEATURE:
                editTableViewId = R.layout.edit_features;
                break;

            case TILE:
                editTableViewId = R.layout.edit_tiles;
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

        View editTableView = inflater.inflate(editTableViewId, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(editTableView);

        final EditText identifierInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_identifier_input);
        final EditText descriptionInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_description_input);
        final EditText minYInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_min_y_input);
        final EditText maxYInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_max_y_input);
        final EditText minXInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_min_x_input);
        final EditText maxXInput = (EditText) editTableView
                .findViewById(R.id.edit_contents_max_x_input);

        EditText tempMinYMatrixSetInput = null;
        EditText tempMaxYMatrixSetInput = null;
        EditText tempMinXMatrixSetInput = null;
        EditText tempMaxXMatrixSetInput = null;

        Spinner tempGeometryTypeSpinner = null;
        EditText tempZInput = null;
        EditText tempMInput = null;

        final GeoPackage geoPackage = manager.open(table.getDatabase());
        Contents tempContents = null;
        TileMatrixSet tempTileMatrixSet = null;
        GeometryColumns tempGeometryColumns = null;
        try {
            switch (table.getType()) {

                case FEATURE:
                    tempGeometryTypeSpinner = (Spinner) editTableView
                            .findViewById(R.id.edit_features_geometry_type);
                    tempZInput = (EditText) editTableView
                            .findViewById(R.id.edit_features_z_input);
                    tempMInput = (EditText) editTableView
                            .findViewById(R.id.edit_features_m_input);

                    tempZInput
                            .setFilters(new InputFilter[]{new InputFilterMinMax(
                                    0, 2)});
                    tempMInput
                            .setFilters(new InputFilter[]{new InputFilterMinMax(
                                    0, 2)});

                    GeometryColumnsDao geometryColumnsDao = geoPackage
                            .getGeometryColumnsDao();
                    tempGeometryColumns = geometryColumnsDao
                            .queryForTableName(table.getName());
                    tempContents = tempGeometryColumns.getContents();

                    tempGeometryTypeSpinner.setSelection(tempGeometryColumns
                            .getGeometryType().getCode());
                    tempZInput.setText(String.valueOf(tempGeometryColumns.getZ()));
                    tempMInput.setText(String.valueOf(tempGeometryColumns.getM()));
                    break;

                case TILE:
                    TileMatrixSetDao tileMatrixSetDao = geoPackage
                            .getTileMatrixSetDao();
                    tempTileMatrixSet = tileMatrixSetDao
                            .queryForId(table.getName());
                    tempContents = tempTileMatrixSet.getContents();

                    tempMinYMatrixSetInput = (EditText) editTableView
                            .findViewById(R.id.edit_tiles_min_y_input);
                    tempMaxYMatrixSetInput = (EditText) editTableView
                            .findViewById(R.id.edit_tiles_max_y_input);
                    tempMinXMatrixSetInput = (EditText) editTableView
                            .findViewById(R.id.edit_tiles_min_x_input);
                    tempMaxXMatrixSetInput = (EditText) editTableView
                            .findViewById(R.id.edit_tiles_max_x_input);

                    tempMinYMatrixSetInput.setText(String.valueOf(tempTileMatrixSet
                            .getMinY()));
                    tempMaxYMatrixSetInput.setText(String.valueOf(tempTileMatrixSet
                            .getMaxY()));
                    tempMinXMatrixSetInput.setText(String.valueOf(tempTileMatrixSet
                            .getMinX()));
                    tempMaxXMatrixSetInput.setText(String.valueOf(tempTileMatrixSet
                            .getMaxX()));
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported table type: " + table.getType());
            }

            identifierInput.setText(tempContents.getIdentifier());
            descriptionInput.setText(tempContents.getDescription());
            if (tempContents.getMinY() != null) {
                minYInput.setText(tempContents.getMinY().toString());
            }
            if (tempContents.getMaxY() != null) {
                maxYInput.setText(tempContents.getMaxY().toString());
            }
            if (tempContents.getMinX() != null) {
                minXInput.setText(tempContents.getMinX().toString());
            }
            if (tempContents.getMaxX() != null) {
                maxXInput.setText(tempContents.getMaxX().toString());
            }

        } catch (Exception e) {
            geoPackage.close();
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_table_edit_label),
                    e.getMessage());
            return;
        }

        final Contents contents = tempContents;
        final Spinner geometryTypeSpinner = tempGeometryTypeSpinner;
        final EditText zInput = tempZInput;
        final EditText mInput = tempMInput;
        final TileMatrixSet tileMatrixSet = tempTileMatrixSet;
        final GeometryColumns geometryColumns = tempGeometryColumns;
        final EditText minYMatrixSetInput = tempMinYMatrixSetInput;
        final EditText maxYMatrixSetInput = tempMaxYMatrixSetInput;
        final EditText minXMatrixSetInput = tempMinXMatrixSetInput;
        final EditText maxXMatrixSetInput = tempMaxXMatrixSetInput;

        dialog.setPositiveButton(getString(R.string.button_ok_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String identifier = identifierInput.getText()
                                    .toString();
                            String description = descriptionInput.getText()
                                    .toString();

                            String minYString = minYInput.getText().toString();
                            Double minY = minYString != null
                                    && !minYString.isEmpty() ? Double
                                    .valueOf(minYString) : null;

                            String maxYString = maxYInput.getText().toString();
                            Double maxY = maxYString != null
                                    && !maxYString.isEmpty() ? Double
                                    .valueOf(maxYString) : null;

                            String minXString = minXInput.getText().toString();
                            Double minX = minXString != null
                                    && !minXString.isEmpty() ? Double
                                    .valueOf(minXString) : null;

                            String maxXString = maxXInput.getText().toString();
                            Double maxX = maxXString != null
                                    && !maxXString.isEmpty() ? Double
                                    .valueOf(maxXString) : null;

                            if (minY != null && maxY != null && minY > maxY) {
                                throw new GeoPackageException(
                                        getString(R.string.edit_contents_min_y_label)
                                                + " can not be larger than "
                                                + getString(R.string.edit_contents_max_y_label));
                            }

                            if (minX != null && maxX != null && minX > maxX) {
                                throw new GeoPackageException(
                                        getString(R.string.edit_contents_min_x_label)
                                                + " can not be larger than "
                                                + getString(R.string.edit_contents_max_x_label));
                            }

                            switch (table.getType()) {

                                case FEATURE:
                                    GeometryColumnsDao geometryColumnsDao = geoPackage
                                            .getGeometryColumnsDao();

                                    geometryColumns.setGeometryType(GeometryType
                                            .fromName(geometryTypeSpinner
                                                    .getSelectedItem().toString()));
                                    geometryColumns.setZ(Byte.valueOf(zInput
                                            .getText().toString()));
                                    geometryColumns.setM(Byte.valueOf(mInput
                                            .getText().toString()));

                                    geometryColumnsDao.update(geometryColumns);
                                    break;

                                case TILE:
                                    TileMatrixSetDao tileMatrixSetDao = geoPackage
                                            .getTileMatrixSetDao();

                                    String minYMatrixSetString = minYMatrixSetInput
                                            .getText().toString();
                                    Double minMatrixSetY = minYMatrixSetString != null
                                            && !minYMatrixSetString.isEmpty() ? Double
                                            .valueOf(minYMatrixSetString) : null;

                                    String maxYMatrixSetString = maxYMatrixSetInput
                                            .getText().toString();
                                    Double maxMatrixSetY = maxYMatrixSetString != null
                                            && !maxYMatrixSetString.isEmpty() ? Double
                                            .valueOf(maxYMatrixSetString) : null;

                                    String minXMatrixSetString = minXMatrixSetInput
                                            .getText().toString();
                                    Double minMatrixSetX = minXMatrixSetString != null
                                            && !minXMatrixSetString.isEmpty() ? Double
                                            .valueOf(minXMatrixSetString) : null;

                                    String maxXMatrixSetString = maxXMatrixSetInput
                                            .getText().toString();
                                    Double maxMatrixSetX = maxXMatrixSetString != null
                                            && !maxXMatrixSetString.isEmpty() ? Double
                                            .valueOf(maxXMatrixSetString) : null;

                                    if (minMatrixSetY == null
                                            || maxMatrixSetY == null
                                            || minMatrixSetX == null
                                            || maxMatrixSetX == null) {
                                        throw new GeoPackageException(
                                                "Min and max bounds are required for Tiles");
                                    }
                                    tileMatrixSet.setMinY(minMatrixSetY);
                                    tileMatrixSet.setMaxY(maxMatrixSetY);
                                    tileMatrixSet.setMinX(minMatrixSetX);
                                    tileMatrixSet.setMaxX(maxMatrixSetX);

                                    tileMatrixSetDao.update(tileMatrixSet);
                                    break;

                                default:
                                    throw new IllegalArgumentException("Unsupported table type: " + table.getType());
                            }

                            ContentsDao contentsDao = geoPackage
                                    .getContentsDao();
                            contents.setIdentifier(identifier);
                            contents.setDescription(description);
                            contents.setMinY(minY);
                            contents.setMaxY(maxY);
                            contents.setMinX(minX);
                            contents.setMaxX(maxX);
                            contents.setLastChange(new Date());
                            contentsDao.update(contents);

                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_table_edit_label),
                                            e.getMessage());
                        } finally {
                            geoPackage.close();
                            update();
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        geoPackage.close();
                        dialog.cancel();
                    }
                });
        dialog.show();

    }

    /**
     * Delete table alert option
     *
     * @param table
     */
    private void deleteTableOption(final GeoPackageTable table) {
        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.geopackage_table_delete_label))
                .setMessage(
                        getString(R.string.geopackage_table_delete_label) + " "
                                + table.getDatabase() + " - " + table.getName()
                                + "?")
                .setPositiveButton(
                        getString(R.string.geopackage_table_delete_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                switch (table.getType()) {
                                    case FEATURE:
                                    case TILE:
                                        GeoPackage geoPackage = manager.open(table
                                                .getDatabase());
                                        try {
                                            geoPackage.deleteTable(table.getName());
                                            active.removeTable(table);
                                            update();
                                        } catch (Exception e) {
                                            GeoPackageUtils.showMessage(getActivity(),
                                                    "Delete " + table.getDatabase()
                                                            + " " + table.getName()
                                                            + " Table", e.getMessage());
                                        } finally {
                                            geoPackage.close();
                                        }
                                        break;

                                    case FEATURE_OVERLAY:
                                        active.removeTable(table);
                                        update();
                                        break;
                                }
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
     * Load tiles table alert option
     *
     * @param table
     */
    private void loadTilesTableOption(final GeoPackageTable table) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View loadTilesView = inflater.inflate(R.layout.load_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(loadTilesView);

        final EditText urlInput = (EditText) loadTilesView
                .findViewById(R.id.load_tiles_url_input);
        final Button preloadedUrlsButton = (Button) loadTilesView
                .findViewById(R.id.load_tiles_preloaded);
        final EditText minZoomInput = (EditText) loadTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) loadTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) loadTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) loadTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) loadTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) loadTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) loadTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) loadTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) loadTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) loadTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) loadTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) loadTilesView
                .findViewById(R.id.bounding_box_preloaded);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, preloadedUrlsButton, null, urlInput,
                compressFormatInput, compressQualityInput, true, maxFeaturesLabel, maxFeaturesInput, false);

        dialog.setPositiveButton(
                getString(R.string.geopackage_table_tiles_load_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tileUrl = urlInput.getText().toString();
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    maxLon, minLat, maxLat);

                            // Load tiles
                            LoadTilesTask.loadTiles(getActivity(),
                                    GeoPackageManagerFragment.this, active,
                                    table.getDatabase(), table.getName(),
                                    tileUrl, minZoom, maxZoom, compressFormat,
                                    compressQuality, googleTiles, boundingBox);
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_table_tiles_load_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }

    /**
     * Index features option
     *
     * @param table
     */
    private void indexFeaturesOption(final GeoPackageTable table) {

        GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
        GeoPackage geoPackage = manager.open(table.getDatabase());
        FeatureDao featureDao = geoPackage.getFeatureDao(table.getName());

        FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
        final boolean geoPackageIndexed = indexer.isIndexed(FeatureIndexType.GEOPACKAGE);
        final boolean metadataIndexed = indexer.isIndexed(FeatureIndexType.METADATA);
        geoPackage.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_item);

        String geoPackageIndexLabel = geoPackageIndexed ?
                getString(R.string.geopackage_table_index_features_index_delete_label) :
                getString(R.string.geopackage_table_index_features_index_create_label);
        geoPackageIndexLabel += " " + getString(R.string.geopackage_table_index_features_index_geopackage_label);
        adapter.add(geoPackageIndexLabel);

        String metadataIndexLabel = metadataIndexed ?
                getString(R.string.geopackage_table_index_features_index_delete_label) :
                getString(R.string.geopackage_table_index_features_index_create_label);
        metadataIndexLabel += " " + getString(R.string.geopackage_table_index_features_index_metadata_label);
        adapter.add(metadataIndexLabel);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(table.getDatabase() + " - " + table.getName() + " "
                + getString(R.string.geopackage_table_index_features_index_title));
        builder.setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                if (item >= 0) {

                    switch (item) {
                        case 0:
                            if (geoPackageIndexed) {
                                deleteIndexFeaturesOption(table, FeatureIndexType.GEOPACKAGE);
                            } else {
                                indexFeaturesOption(table, FeatureIndexType.GEOPACKAGE);
                            }
                            break;
                        case 1:
                            if (metadataIndexed) {
                                deleteIndexFeaturesOption(table, FeatureIndexType.METADATA);
                            } else {
                                indexFeaturesOption(table, FeatureIndexType.METADATA);
                            }
                            break;
                        default:
                    }
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Delete Index features option
     *
     * @param table
     * @param indexLocation
     */
    private void deleteIndexFeaturesOption(final GeoPackageTable table, final FeatureIndexType indexLocation) {

        String message = getString(R.string.geopackage_table_index_features_index_delete_label) + " "
                + table.getDatabase() + " - " + table.getName()
                + " " + getString(R.string.geopackage_table_index_features_index_title);
        switch (indexLocation) {
            case GEOPACKAGE:
                message += " " + getString(R.string.geopackage_table_index_features_index_delete_geopackage_label);
                break;
            case METADATA:
                message += " " + getString(R.string.geopackage_table_index_features_index_delete_metadata_label);
                break;
        }

        AlertDialog indexDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.geopackage_table_index_features_index_delete_label) + " "
                        + getString(R.string.geopackage_table_index_features_index_title))
                .setMessage(message)
                .setPositiveButton(
                        getString(R.string.button_ok_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                GeoPackage geoPackage = manager.open(table.getDatabase());
                                FeatureDao featureDao = geoPackage.getFeatureDao(table.getName());
                                FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
                                indexer.setIndexLocation(indexLocation);
                                indexer.deleteIndex();
                                geoPackage.close();
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
        indexDialog.show();
    }

    /**
     * Index features option
     *
     * @param table
     * @param indexLocation
     */
    private void indexFeaturesOption(final GeoPackageTable table, final FeatureIndexType indexLocation) {

        String message = getString(R.string.geopackage_table_index_features_index_create_label) + " "
                + table.getDatabase() + " - " + table.getName()
                + " " + getString(R.string.geopackage_table_index_features_index_title);
        switch (indexLocation) {
            case GEOPACKAGE:
                message += " " + getString(R.string.geopackage_table_index_features_index_create_geopackage_label);
                break;
            case METADATA:
                message += " " + getString(R.string.geopackage_table_index_features_index_create_metadata_label);
                break;
        }

        AlertDialog indexDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.geopackage_table_index_features_index_create_label) + " "
                        + getString(R.string.geopackage_table_index_features_index_title))
                .setMessage(message)
                .setPositiveButton(
                        getString(R.string.button_ok_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                IndexerTask.indexFeatures(getActivity(), GeoPackageManagerFragment.this, table.getDatabase(), table.getName(), indexLocation);
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
        indexDialog.show();
    }

    /**
     * Create feature tiles table option
     *
     * @param table
     */
    private void createFeatureTilesTableOption(final GeoPackageTable table) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createTilesView = inflater.inflate(R.layout.feature_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(createTilesView);

        final TextView indexWarning = (TextView) createTilesView
                .findViewById(R.id.feature_tiles_index_warning);
        final EditText nameInput = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_name_input);
        final EditText minZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) createTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) createTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) createTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) createTilesView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner pointColor = (Spinner) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_color);
        final EditText pointAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_alpha);
        final EditText pointRadius = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_radius);
        final Spinner lineColor = (Spinner) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_color);
        final EditText lineAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_alpha);
        final EditText lineStroke = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_stroke);
        final Spinner polygonColor = (Spinner) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_color);
        final EditText polygonAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_alpha);
        final EditText polygonStroke = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_stroke);
        final CheckBox polygonFill = (CheckBox) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill);
        final Spinner polygonFillColor = (Spinner) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_color);
        final EditText polygonFillAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_alpha);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        boolean setZooms = true;

        // Preset the bounding box to the feature contents
        GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
        GeoPackage geoPackage = manager.open(table.getDatabase());
        ContentsDao contentsDao = geoPackage.getContentsDao();
        try {
            Contents contents = contentsDao.queryForId(table.getName());
            if (contents != null) {
                BoundingBox boundingBox = contents.getBoundingBox();
                Projection projection = ProjectionFactory.getProjection(
                        contents.getSrs().getOrganizationCoordsysId());

                // Try to find a good zoom starting point
                ProjectionTransform webMercatorTransform = projection.getTransformation(
                        ProjectionConstants.EPSG_WEB_MERCATOR);
                BoundingBox webMercatorBoundingBox = webMercatorTransform.transform(boundingBox);
                int zoomLevel = TileBoundingBoxUtils.getZoomLevel(webMercatorBoundingBox);
                int maxZoomLevel = getActivity().getResources().getInteger(
                        R.integer.load_tiles_max_zoom_default);
                zoomLevel = Math.max(0, Math.min(zoomLevel, maxZoomLevel) - 1);
                minZoomInput.setText(String.valueOf(zoomLevel));
                maxZoomInput.setText(String.valueOf(maxZoomLevel));
                setZooms = false;

                ProjectionTransform worldGeodeticTransform = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR).getTransformation(
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                BoundingBox worldGeodeticBoundingBox = worldGeodeticTransform.transform(webMercatorBoundingBox);
                minLonInput.setText(String.valueOf(worldGeodeticBoundingBox.getMinLongitude()));
                minLatInput.setText(String.valueOf(worldGeodeticBoundingBox.getMinLatitude()));
                maxLonInput.setText(String.valueOf(worldGeodeticBoundingBox.getMaxLongitude()));
                maxLatInput.setText(String.valueOf(worldGeodeticBoundingBox.getMaxLatitude()));
            }
        } catch (Exception e) {
            // don't preset the bounding box
        }

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, null, nameInput, null,
                compressFormatInput, compressQualityInput, setZooms, maxFeaturesLabel, maxFeaturesInput, true);

        // Check if indexed
        FeatureDao featureDao = geoPackage.getFeatureDao(table.getName());
        FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
        if (indexer.isIndexed()) {
            indexWarning.setVisibility(View.GONE);
        }

        // Close the GeoPackage
        geoPackage.close();

        // Set a default name
        nameInput.setText(table.getName() + getString(R.string.feature_tiles_name_suffix));

        // Prepare the feature draw
        prepareFeatureDraw(pointAlpha, lineAlpha, polygonAlpha, polygonFillAlpha,
                pointColor, lineColor, pointRadius, lineStroke,
                polygonColor, polygonStroke, polygonFill, polygonFillColor);

        dialog.setPositiveButton(
                getString(R.string.geopackage_table_create_feature_tiles_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.feature_tiles_name_label)
                                                + " is required");
                            }
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());

                            Integer maxFeatures = null;
                            String maxFeaturesText = maxFeaturesInput.getText().toString();
                            if (maxFeaturesText != null && !maxFeaturesText.isEmpty()) {
                                maxFeatures = Integer.valueOf(maxFeaturesText);
                            }

                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    maxLon, minLat, maxLat);

                            GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
                            GeoPackage geoPackage = manager.open(table.getDatabase());
                            FeatureDao featureDao = geoPackage.getFeatureDao(table.getName());

                            // Load tiles
                            FeatureTiles featureTiles = new FeatureTiles(getActivity(), featureDao);
                            featureTiles.setMaxFeaturesPerTile(maxFeatures);
                            if (maxFeatures != null) {
                                featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(getActivity()));
                            }

                            FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
                            if (indexer.isIndexed()) {
                                featureTiles.setIndexManager(indexer);
                            }

                            Paint pointPaint = featureTiles.getPointPaint();
                            if (pointColor.getSelectedItemPosition() >= 0) {
                                pointPaint.setColor(Color.parseColor(pointColor.getSelectedItem().toString()));
                            }
                            pointPaint.setAlpha(Integer.valueOf(pointAlpha
                                    .getText().toString()));
                            featureTiles.setPointRadius(Float.valueOf(pointRadius.getText().toString()));

                            Paint linePaint = featureTiles.getLinePaint();
                            if (lineColor.getSelectedItemPosition() >= 0) {
                                linePaint.setColor(Color.parseColor(lineColor.getSelectedItem().toString()));
                            }
                            linePaint.setAlpha(Integer.valueOf(lineAlpha
                                    .getText().toString()));
                            linePaint.setStrokeWidth(Float.valueOf(lineStroke.getText().toString()));

                            Paint polygonPaint = featureTiles.getPolygonPaint();
                            if (polygonColor.getSelectedItemPosition() >= 0) {
                                polygonPaint.setColor(Color.parseColor(polygonColor.getSelectedItem().toString()));
                            }
                            polygonPaint.setAlpha(Integer.valueOf(polygonAlpha
                                    .getText().toString()));
                            polygonPaint.setStrokeWidth(Float.valueOf(polygonStroke.getText().toString()));

                            featureTiles.setFillPolygon(polygonFill.isChecked());
                            if (featureTiles.isFillPolygon()) {
                                Paint polygonFillPaint = featureTiles.getPolygonFillPaint();
                                if (polygonFillColor.getSelectedItemPosition() >= 0) {
                                    polygonFillPaint.setColor(Color.parseColor(polygonFillColor.getSelectedItem().toString()));
                                }
                                polygonFillPaint.setAlpha(Integer.valueOf(polygonFillAlpha
                                        .getText().toString()));
                            }

                            featureTiles.calculateDrawOverlap();

                            LoadTilesTask.loadTiles(getActivity(),
                                    GeoPackageManagerFragment.this, active,
                                    geoPackage, tableName, featureTiles, minZoom,
                                    maxZoom, compressFormat,
                                    compressQuality, googleTiles,
                                    boundingBox);
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

    }

    /**
     * Add feature overlay table option
     *
     * @param table
     */
    private void addFeatureOverlayTableOption(final GeoPackageTable table) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createFeatureOverlayView = inflater.inflate(R.layout.create_feature_overlay, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(createFeatureOverlayView);

        final EditText nameInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.create_feature_overlay_name_input);
        final TextView indexWarning = (TextView) createFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_index_warning);
        final EditText minZoomInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_min_zoom_input);
        final EditText maxZoomInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_max_zoom_input);
        final EditText maxFeaturesInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_max_features_input);
        final EditText minLatInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createFeatureOverlayView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) createFeatureOverlayView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner pointColor = (Spinner) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_color);
        final EditText pointAlpha = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_alpha);
        final EditText pointRadius = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_radius);
        final Spinner lineColor = (Spinner) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_color);
        final EditText lineAlpha = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_alpha);
        final EditText lineStroke = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_stroke);
        final Spinner polygonColor = (Spinner) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_color);
        final EditText polygonAlpha = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_alpha);
        final EditText polygonStroke = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_stroke);
        final CheckBox polygonFill = (CheckBox) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill);
        final Spinner polygonFillColor = (Spinner) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_color);
        final EditText polygonFillAlpha = (EditText) createFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_alpha);

        // Set a default name
        nameInput.setText(table.getName() + getString(R.string.feature_overlay_tiles_name_suffix));

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        int minZoomLevel = getActivity().getResources().getInteger(
                R.integer.load_tiles_min_zoom_default);
        int maxZoomLevel = getActivity().getResources().getInteger(
                R.integer.load_tiles_max_zoom_default);
        minZoomInput.setText(String.valueOf(minZoomLevel));
        maxZoomInput.setText(String.valueOf(maxZoomLevel));

        int maxFeatures = getActivity().getResources().getInteger(
                R.integer.feature_tiles_overlay_max_features_per_tile_default);
        maxFeaturesInput.setText(String.valueOf(maxFeatures));

        GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
        GeoPackage geoPackage = manager.open(table.getDatabase());
        FeatureDao featureDao = geoPackage.getFeatureDao(table.getName());
        Contents contents = featureDao.getGeometryColumns().getContents();

        BoundingBox boundingBox = contents.getBoundingBox();
        Projection projection = ProjectionFactory.getProjection(
                contents.getSrs().getOrganizationCoordsysId());

        ProjectionTransform webMercatorTransform = projection.getTransformation(
                ProjectionConstants.EPSG_WEB_MERCATOR);
        BoundingBox webMercatorBoundingBox = webMercatorTransform.transform(boundingBox);

        ProjectionTransform worldGeodeticTransform = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR).getTransformation(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        BoundingBox worldGeodeticBoundingBox = worldGeodeticTransform.transform(webMercatorBoundingBox);
        minLonInput.setText(String.valueOf(worldGeodeticBoundingBox.getMinLongitude()));
        minLatInput.setText(String.valueOf(worldGeodeticBoundingBox.getMinLatitude()));
        maxLonInput.setText(String.valueOf(worldGeodeticBoundingBox.getMaxLongitude()));
        maxLatInput.setText(String.valueOf(worldGeodeticBoundingBox.getMaxLatitude()));

        // Check if indexed
        FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
        if (indexer.isIndexed()) {
            indexWarning.setVisibility(View.GONE);
        }

        geoPackage.close();

        // Prepare the feature draw
        prepareFeatureDraw(pointAlpha, lineAlpha, polygonAlpha, polygonFillAlpha,
                pointColor, lineColor, pointRadius, lineStroke,
                polygonColor, polygonStroke, polygonFill, polygonFillColor);

        dialog.setPositiveButton(
                getString(R.string.geopackage_table_add_feature_overlay_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_feature_overlay_name_label)
                                                + " is required");
                            }

                            GeoPackageFeatureOverlayTable overlayTable = new GeoPackageFeatureOverlayTable(table.getDatabase(), tableName, table.getName(),
                                    null, 0);


                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());

                            Integer maxFeatures = null;
                            String maxFeaturesText = maxFeaturesInput.getText().toString();
                            if (maxFeaturesText != null && !maxFeaturesText.isEmpty()) {
                                maxFeatures = Integer.valueOf(maxFeaturesText);
                            }

                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            overlayTable.setMinZoom(minZoom);
                            overlayTable.setMaxZoom(maxZoom);
                            overlayTable.setMaxFeaturesPerTile(maxFeatures);
                            overlayTable.setMinLat(minLat);
                            overlayTable.setMaxLat(maxLat);
                            overlayTable.setMinLon(minLon);
                            overlayTable.setMaxLon(maxLon);
                            overlayTable.setPointColor(pointColor.getSelectedItem().toString());
                            overlayTable.setPointAlpha(Integer.valueOf(pointAlpha
                                    .getText().toString()));
                            overlayTable.setPointRadius(Float.valueOf(pointRadius.getText().toString()));
                            overlayTable.setLineColor(lineColor.getSelectedItem().toString());
                            overlayTable.setLineAlpha(Integer.valueOf(lineAlpha
                                    .getText().toString()));
                            overlayTable.setLineStrokeWidth(Float.valueOf(lineStroke.getText().toString()));
                            overlayTable.setPolygonColor(polygonColor.getSelectedItem().toString());
                            overlayTable.setPolygonAlpha(Integer.valueOf(polygonAlpha
                                    .getText().toString()));
                            overlayTable.setPolygonStrokeWidth(Float.valueOf(polygonStroke.getText().toString()));
                            overlayTable.setPolygonFill(polygonFill.isChecked());
                            overlayTable.setPolygonFillColor(polygonFillColor.getSelectedItem().toString());
                            overlayTable.setPolygonFillAlpha(Integer.valueOf(polygonFillAlpha
                                    .getText().toString()));

                            active.addTable(overlayTable);
                            update();

                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }


    /**
     * Add feature overlay table option
     *
     * @param table
     */
    private void editFeatureOverlayTableOption(final GeoPackageFeatureOverlayTable table) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View editFeatureOverlayView = inflater.inflate(R.layout.edit_feature_overlay, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(editFeatureOverlayView);

        final TextView indexWarning = (TextView) editFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_index_warning);
        final EditText minZoomInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_min_zoom_input);
        final EditText maxZoomInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_max_zoom_input);
        final EditText maxFeaturesInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.edit_feature_overlay_max_features_input);
        final EditText minLatInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) editFeatureOverlayView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final Button preloadedLocationsButton = (Button) editFeatureOverlayView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner pointColor = (Spinner) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_color);
        final EditText pointAlpha = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_alpha);
        final EditText pointRadius = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_point_radius);
        final Spinner lineColor = (Spinner) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_color);
        final EditText lineAlpha = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_alpha);
        final EditText lineStroke = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_line_stroke);
        final Spinner polygonColor = (Spinner) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_color);
        final EditText polygonAlpha = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_alpha);
        final EditText polygonStroke = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_stroke);
        final CheckBox polygonFill = (CheckBox) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill);
        final Spinner polygonFillColor = (Spinner) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_color);
        final EditText polygonFillAlpha = (EditText) editFeatureOverlayView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_alpha);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        minZoomInput.setText(String.valueOf(table.getMinZoom()));
        maxZoomInput.setText(String.valueOf(table.getMaxZoom()));
        if (table.getMaxFeaturesPerTile() != null) {
            maxFeaturesInput.setText(String.valueOf(table.getMaxFeaturesPerTile()));
        }
        minLonInput.setText(String.valueOf(table.getMinLon()));
        minLatInput.setText(String.valueOf(table.getMinLat()));
        maxLonInput.setText(String.valueOf(table.getMaxLon()));
        maxLatInput.setText(String.valueOf(table.getMaxLat()));

        // Check if indexed
        GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
        GeoPackage geoPackage = manager.open(table.getDatabase());
        FeatureDao featureDao = geoPackage.getFeatureDao(table.getFeatureTable());
        FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
        if (indexer.isIndexed()) {
            indexWarning.setVisibility(View.GONE);
        }
        geoPackage.close();

        // Prepare the feature draw
        prepareFeatureDraw(pointAlpha, lineAlpha, polygonAlpha, polygonFillAlpha,
                pointColor, lineColor, pointRadius, lineStroke,
                polygonColor, polygonStroke, polygonFill, polygonFillColor);

        pointColor.setSelection(((ArrayAdapter) pointColor.getAdapter()).getPosition(table.getPointColor()));
        pointAlpha.setText(String.valueOf(table.getPointAlpha()));
        pointRadius.setText(String.valueOf(table.getPointRadius()));

        lineColor.setSelection(((ArrayAdapter) lineColor.getAdapter()).getPosition(table.getLineColor()));
        lineAlpha.setText(String.valueOf(table.getLineAlpha()));
        lineStroke.setText(String.valueOf(table.getLineStrokeWidth()));

        polygonColor.setSelection(((ArrayAdapter) polygonColor.getAdapter()).getPosition(table.getPolygonColor()));
        polygonAlpha.setText(String.valueOf(table.getPolygonAlpha()));
        polygonStroke.setText(String.valueOf(table.getPolygonStrokeWidth()));

        polygonFill.setChecked(table.isPolygonFill());
        polygonFillColor.setSelection(((ArrayAdapter) polygonFillColor.getAdapter()).getPosition(table.getPolygonFillColor()));
        polygonFillAlpha.setText(String.valueOf(table.getPolygonFillAlpha()));

        dialog.setPositiveButton(
                getString(R.string.geopackage_table_edit_feature_overlay_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());
                            Integer maxFeatures = null;
                            String maxFeaturesText = maxFeaturesInput.getText().toString();
                            if (maxFeaturesText != null && !maxFeaturesText.isEmpty()) {
                                maxFeatures = Integer.valueOf(maxFeaturesText);
                            }
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            table.setMinZoom(minZoom);
                            table.setMaxZoom(maxZoom);
                            table.setMaxFeaturesPerTile(maxFeatures);
                            table.setMinLat(minLat);
                            table.setMaxLat(maxLat);
                            table.setMinLon(minLon);
                            table.setMaxLon(maxLon);
                            table.setPointColor(pointColor.getSelectedItem().toString());
                            table.setPointAlpha(Integer.valueOf(pointAlpha
                                    .getText().toString()));
                            table.setPointRadius(Float.valueOf(pointRadius.getText().toString()));
                            table.setLineColor(lineColor.getSelectedItem().toString());
                            table.setLineAlpha(Integer.valueOf(lineAlpha
                                    .getText().toString()));
                            table.setLineStrokeWidth(Float.valueOf(lineStroke.getText().toString()));
                            table.setPolygonColor(polygonColor.getSelectedItem().toString());
                            table.setPolygonAlpha(Integer.valueOf(polygonAlpha
                                    .getText().toString()));
                            table.setPolygonStrokeWidth(Float.valueOf(polygonStroke.getText().toString()));
                            table.setPolygonFill(polygonFill.isChecked());
                            table.setPolygonFillColor(polygonFillColor.getSelectedItem().toString());
                            table.setPolygonFillAlpha(Integer.valueOf(polygonFillAlpha
                                    .getText().toString()));

                            active.addTable(table);
                            update();

                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }

    /**
     * Prepare the feature draw limits and defaults
     *
     * @param pointAlpha
     * @param lineAlpha
     * @param polygonAlpha
     * @param polygonFillAlpha
     * @param pointColor
     * @param lineColor
     * @param pointRadius
     * @param lineStroke
     * @param polygonColor
     * @param polygonStroke
     * @param polygonFill
     * @param polygonFillColor
     */
    private void prepareFeatureDraw(EditText pointAlpha, EditText lineAlpha, EditText polygonAlpha, EditText polygonFillAlpha,
                                    Spinner pointColor, Spinner lineColor, EditText pointRadius, EditText lineStroke,
                                    Spinner polygonColor, EditText polygonStroke, CheckBox polygonFill, Spinner polygonFillColor) {

        // Set feature limits
        pointAlpha.setFilters(new InputFilter[]{new InputFilterMinMax(
                0, 255)});
        lineAlpha.setFilters(new InputFilter[]{new InputFilterMinMax(
                0, 255)});
        polygonAlpha.setFilters(new InputFilter[]{new InputFilterMinMax(
                0, 255)});
        polygonFillAlpha.setFilters(new InputFilter[]{new InputFilterMinMax(
                0, 255)});

        // Set default feature attributes
        FeatureTiles featureTiles = new FeatureTiles(getActivity(), null);
        String defaultColor = "black";

        Paint pointPaint = featureTiles.getPointPaint();
        pointColor.setSelection(((ArrayAdapter) pointColor.getAdapter()).getPosition(defaultColor));
        pointAlpha.setText(String.valueOf(pointPaint.getAlpha()));
        pointRadius.setText(String.valueOf(featureTiles.getPointRadius()));

        Paint linePaint = featureTiles.getLinePaint();
        lineColor.setSelection(((ArrayAdapter) lineColor.getAdapter()).getPosition(defaultColor));
        lineAlpha.setText(String.valueOf(linePaint.getAlpha()));
        lineStroke.setText(String.valueOf(linePaint.getStrokeWidth()));

        Paint polygonPaint = featureTiles.getPolygonPaint();
        polygonColor.setSelection(((ArrayAdapter) polygonColor.getAdapter()).getPosition(defaultColor));
        polygonAlpha.setText(String.valueOf(polygonPaint.getAlpha()));
        polygonStroke.setText(String.valueOf(polygonPaint.getStrokeWidth()));

        polygonFill.setChecked(featureTiles.isFillPolygon());
        Paint polygonFillPaint = featureTiles.getPolygonFillPaint();
        polygonFillColor.setSelection(((ArrayAdapter) polygonFillColor.getAdapter()).getPosition(defaultColor));
        polygonFillAlpha.setText(String.valueOf(polygonFillPaint.getAlpha()));
    }

    /**
     * Handle manager menu clicks
     *
     * @param item
     * @return
     */
    public boolean handleMenuClick(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.import_geopackage_url:
                importGeopackageFromUrl();
                break;
            case R.id.import_geopackage_file:
                importGeopackageFromFile();
                break;
            case R.id.create_geopackage:
                createGeoPackage();
                break;
            case R.id.clear_selected_tables:
                active.clearActive();
                update();
                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

    /**
     * Import a GeoPackage from a URL
     */
    private void importGeopackageFromUrl() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View importUrlView = inflater.inflate(R.layout.import_url, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(importUrlView);

        final EditText nameInput = (EditText) importUrlView
                .findViewById(R.id.import_url_name_input);
        final EditText urlInput = (EditText) importUrlView
                .findViewById(R.id.import_url_input);
        final Button button = (Button) importUrlView
                .findViewById(R.id.import_url_preloaded);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.select_dialog_item);
                adapter.addAll(getResources().getStringArray(
                        R.array.preloaded_geopackage_url_labels));
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setTitle(getString(R.string.import_url_preloaded_label));
                builder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item >= 0) {
                                    String[] urls = getResources()
                                            .getStringArray(
                                                    R.array.preloaded_geopackage_urls);
                                    String[] names = getResources()
                                            .getStringArray(
                                                    R.array.preloaded_geopackage_url_names);
                                    nameInput.setText(names[item]);
                                    urlInput.setText(urls[item]);
                                }
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        dialog.setPositiveButton(getString(R.string.geopackage_import_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String database = nameInput.getText().toString();
                        String url = urlInput.getText().toString();

                        DownloadTask downloadTask = new DownloadTask(database,
                                url);

                        progressDialog = createDownloadProgressDialog(database,
                                url, downloadTask, null);
                        progressDialog.setIndeterminate(true);

                        downloadTask.execute();
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

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
            // eat
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

        final ContentResolver resolver = getActivity().getContentResolver();

        // Try to get the file path
        final String path = FileUtils.getPath(getActivity(), uri);

        // Try to get the GeoPackage name
        String name = null;
        if (path != null) {
            name = new File(path).getName();
        } else {
            name = getDisplayName(resolver, uri);
        }

        // Remove the extension
        if (name != null) {
            int extensionIndex = name.lastIndexOf(".");
            if (extensionIndex > -1) {
                name = name.substring(0, extensionIndex);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View importFileView = inflater.inflate(R.layout.import_file, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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

                                        // Import the GeoPackage by copying the file
                                        if (copy) {
                                            ImportTask importTask = new ImportTask(value, path, uri);
                                            progressDialog = createImportProgressDialog(value,
                                                    importTask, path, uri, null);
                                            progressDialog.setIndeterminate(true);
                                            importTask.execute();
                                        } else {
                                            // Import the GeoPackage by linking
                                            // to the file
                                            boolean imported = manager
                                                    .importGeoPackageAsExternalLink(
                                                            path, value);

                                            if (imported) {
                                                update();
                                            } else {
                                                try {
                                                    getActivity().runOnUiThread(
                                                            new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    GeoPackageUtils
                                                                            .showMessage(
                                                                                    getActivity(),
                                                                                    "URL Import",
                                                                                    "Failed to import Uri: "
                                                                                            + uri.getPath());
                                                                }
                                                            });
                                                } catch (Exception e2) {
                                                    // eat
                                                }
                                            }
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
                                            // eat
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

        final EditText input = new EditText(getActivity());

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.geopackage_create_label))
                .setView(input)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
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
     * Expandable list adapter
     */
    public class GeoPackageListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return databases.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return databaseTables.get(i).size();
        }

        @Override
        public Object getGroup(int i) {
            return databases.get(i);
        }

        @Override
        public Object getChild(int i, int j) {
            return databaseTables.get(i).get(j);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int j) {
            return j;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean isExpanded, View view,
                                 ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.manager_group, null);
            }

            TextView geoPackageName = (TextView) view
                    .findViewById(R.id.manager_group_name);
            geoPackageName.setText(databases.get(i));

            return view;
        }

        @Override
        public View getChildView(int i, int j, boolean b, View view,
                                 ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.manager_child, null);
            }

            final GeoPackageTable table = databaseTables.get(i).get(j);

            CheckBox checkBox = (CheckBox) view
                    .findViewById(R.id.manager_child_checkbox);
            ImageView imageView = (ImageView) view
                    .findViewById(R.id.manager_child_image);
            TextView tableName = (TextView) view
                    .findViewById(R.id.manager_child_name);
            TextView count = (TextView) view
                    .findViewById(R.id.manager_child_count);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (table.isActive() != isChecked) {
                        table.setActive(isChecked);

                        if (table.getType() == GeoPackageTableType.FEATURE_OVERLAY) {
                            active.removeTable(table);
                            active.addTable(table);
                        } else {
                            if (isChecked) {
                                active.addTable(table);
                            } else {
                                active.removeTable(table, true);
                            }
                        }
                    }
                }
            });
            tableName.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    GeoPackageManagerFragment.this.tableOptions(table);
                    return true;
                }
            });

            checkBox.setChecked(table.isActive());

            switch (table.getType()) {

                case FEATURE:
                    GeometryType geometryType = ((GeoPackageFeatureTable) table).getGeometryType();
                    int drawableId = R.drawable.ic_geometry;
                    if (geometryType != null) {

                        switch (geometryType) {

                            case POINT:
                            case MULTIPOINT:
                                drawableId = R.drawable.ic_point;
                                break;

                            case LINESTRING:
                            case MULTILINESTRING:
                            case CURVE:
                            case COMPOUNDCURVE:
                            case CIRCULARSTRING:
                            case MULTICURVE:
                                drawableId = R.drawable.ic_linestring;
                                break;

                            case POLYGON:
                            case SURFACE:
                            case CURVEPOLYGON:
                            case TRIANGLE:
                            case POLYHEDRALSURFACE:
                            case TIN:
                            case MULTIPOLYGON:
                            case MULTISURFACE:
                                drawableId = R.drawable.ic_polygon;
                                break;

                            case GEOMETRY:
                            case GEOMETRYCOLLECTION:
                                drawableId = R.drawable.ic_geometry;
                                break;
                        }
                    }
                    imageView.setImageDrawable(getResources().getDrawable(
                            drawableId));
                    break;

                case TILE:
                    imageView.setImageDrawable(getResources().getDrawable(
                            R.drawable.ic_tiles));
                    break;

                case FEATURE_OVERLAY:
                    imageView.setImageDrawable(getResources().getDrawable(
                            R.drawable.ic_format_paint));
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported table type: " + table.getType());
            }

            tableName.setText(table.getName());
            count.setText("(" + String.valueOf(table.getCount()) + ")");

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int j) {
            return true;
        }

    }

}

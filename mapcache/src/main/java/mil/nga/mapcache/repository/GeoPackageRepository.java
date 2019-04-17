package mil.nga.mapcache.repository;

import android.app.Activity;
import androidx.lifecycle.MutableLiveData;
import androidx.appcompat.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.sf.GeometryType;
import mil.nga.sf.proj.ProjectionConstants;

/**
 *  Repository to provide access to stored GeoPackages
 */

public class GeoPackageRepository {

    private GeoPackageManager manager;
    private List<GeoPackage> geoPackages = new ArrayList<>();
    private GeoPackageDatabases active;



    public GeoPackageRepository(@NonNull Application application) {
        manager = GeoPackageFactory.getManager(application);
        active = GeoPackageDatabases.getInstance(application);
    }

    public GeoPackage getGeoPackageByName(String name) {
        try {
            GeoPackage geo = manager.open(name);
            geo.close();
            return geo;
        } catch (Exception e) {

        }
        return null;
    }

    public boolean setGeoPackageName(String oldName, String newName) {
        return manager.rename(oldName, newName);
    }

    public List<GeoPackage> getGeoPackages() {
        return geoPackages;
    }

    public List<List<GeoPackageTable>> regenerateTableList() {
        geoPackages.clear();
        List<List<GeoPackageTable>> databaseTables = new ArrayList<List<GeoPackageTable>>();
        StringBuilder errorMessage = new StringBuilder();
        Iterator<String> databasesIterator = manager.databases().iterator();
        while (databasesIterator.hasNext()) {
            boolean invalidGP = false;
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
                // This is a simple list of layer names (will be assigned to the 'allTables' var)
                List<String> tableNames = new ArrayList<>();
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
                                }

                                GeoPackageTable table = new GeoPackageFeatureTable(database,
                                        tableName, geometryType, count);
                                table.setActive(active.exists(table));
                                tables.add(table);
                                // Update simple list of layer names
                                tableNames.add(table.getName());
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
                                table.setActive(active.exists(table));
                                tables.add(table);
                                // Update simple list of layer names
                                tableNames.add(table.getName());

                            }
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

//                    for (GeoPackageFeatureOverlayTable table : active.featureOverlays(database)) {
//                        try {
//                            FeatureDao featureDao = geoPackage.getFeatureDao(table.getFeatureTable());
//                            int count = featureDao.count();
//                            table.setCount(count);
//                            tables.add(table);
//                        } catch (Exception e) {
//                            exceptions.add(e);
//                        }
//                    }

                } catch (Exception e) {
                    // If the error message contains "invalid geopackage", this GP will be labeled as invalid
                    invalidGP = e.toString().indexOf("Invalid GeoPackage") != -1 ? true : false;
                    exceptions.add(e);
                }

                if (geoPackage != null) {
                    geoPackage.close();
                    geoPackages.add(geoPackage);

                }


                // If There are no tables under the database, create a blank table so that we can at
                // least pass the database name up to the recycler view
                if (tables.isEmpty() && exceptions.isEmpty()) {
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
                        // If a geopackage is missing tables, it's invalid, don't add to the list.
                        // make sure it's deleteed
                        if (!invalidGP) {
                            databaseTables.add(tables);
//                        geoAdapter.insertToEnd(tables);
                        } else {
                            manager.delete(database);
                        }

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

        return databaseTables;
    }


    /**
     * Returns the GeoPackage's size
     */
    public String getGeoPackageSize(String geoPackageName) {
        return manager.readableSize(geoPackageName);
    }

    /**
     * Delete a geoPackage by name
     */
    public boolean deleteGeoPackage(String geoPackageName) {
        return manager.delete(geoPackageName);
    }

    /**
     * Delete a layer from a geopackage
     */
    public boolean removeLayerFromGeo(String geoPackageName, String layerName) {
        try {
            GeoPackage geo = manager.open(geoPackageName);
            if (geo != null) {
                geo.deleteTable(layerName);
                geo.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rename a layer in a geopackage
     */
    public boolean renameLayer(String tableName, String gpName, String newName) {
        // find table by name
        // change table identifier field
        return false;
    }

    /**
     * Create a geoPackage by name
     */
    public boolean createGeoPackage(String geoPackageName) {
        return manager.create(geoPackageName);
    }

    /**
     * Copy a geoPackage by name
     */
    public boolean copyGeoPackage(String geoPackageName, String newName) {
        return manager.copy(geoPackageName, newName);
    }

    /**
     * import a geopackage from url.  GeoPackageProgress should be an instance of DownloadTask
     */
    public boolean importGeoPackage(String name, URL source, GeoPackageProgress progress) {
        return manager.importGeoPackage(name, source, progress);
    }

    /**
     * Returns a database file
     */
    public File getDatabaseFile(String database) {
        return manager.getFile(database);
    }

    /**
     * Returns true if it's an external db
     */
    public boolean isExternal(String database) {
        return manager.isExternal(database);
    }

    /**
     * Returns true if exists
     */
    public boolean exists(String database) {
        return manager.exists(database);
    }

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @return true if imported successfully
     */
    public boolean importGeoPackageAsExternalLink(String path, String database) {
        return manager.importGeoPackageAsExternalLink(path, database);
    }

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @param progress progress tracker
     * @return true if loaded
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    GeoPackageProgress progress) {
        return manager.importGeoPackage(database, stream, progress);
    }

    /**
     * Returns the list of tile tables for a geopackage
     */
    public List<String> getTileTables(String database){
        GeoPackage geo = manager.open(database);
        if(geo != null) {
            List<String> tiles = geo.getTileTables();
            geo.close();
            return tiles;
        }
        return null;
    }

    /**
     *  Returns the list of feature tables for a geopackage
     */
    public List<String> getFeatureTables(String database){
        GeoPackage geo = manager.open(database);
        if(geo != null) {
            List<String> features = geo.getFeatureTables();
            geo.close();
            return features;
        }
        return null;
    }


    /**
     * Create feature table in the given geopackage
     */
    public boolean createFeatureTable(String gpName, BoundingBox boundingBox, GeometryType geometryType, String tableName){
        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey(tableName,
                "geom"));
        geometryColumns.setGeometryType(geometryType);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);

        GeoPackage geoPackage = manager.open(gpName);
        try {
            GeometryColumns created = geoPackage.createFeatureTableWithMetadata(
                    geometryColumns, boundingBox, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            if(created != null) {
                return true;
            }
        } finally {
            geoPackage.close();
        }
        return false;
    }

    /**
     * Get the given layer name
     */
    public GeoPackageTable getTableObject(String gpName, String tableName, Boolean setActive){
        GeoPackage geo = manager.open(gpName);
        if(geo != null) {
            ContentsDao contentsDao = geo.getContentsDao();
            List<String> features = geo.getFeatureTables();
            if (features.contains(tableName)) {
                FeatureDao featureDao = geo.getFeatureDao(tableName);
                int count = featureDao.count();

                GeometryType geometryType = null;
                try {
                    Contents contents = contentsDao.queryForId(tableName);
                    GeometryColumns geometryColumns = contents
                            .getGeometryColumns();
                    geometryType = geometryColumns.getGeometryType();
                } catch (Exception e) {
                }

                GeoPackageTable table = new GeoPackageFeatureTable(gpName,
                        tableName, geometryType, count);
                // If saveTable boolean is set, set the table's active status to that given value
                if(setActive != null){
                    table.setActive(setActive);
                }
                geo.close();
                return table;
            }
            List<String> tiles = geo.getTileTables();
            if (tiles.contains(tableName)) {
                List<String> tileTables = null;
                try {
                    tileTables = geo.getTileTables();
                } catch (Exception e) {
                }
                if (tileTables != null) {
                    try {
                        for (String tileTableName : tileTables) {
                            TileDao tileDao = geo.getTileDao(tileTableName);
                            int count = tileDao.count();
                            GeoPackageTable table = new GeoPackageTileTable(gpName,
                                    tileTableName, count);
                            if(setActive != null){
                                table.setActive(setActive);
                            }
                            geo.close();
                            return table;
                        }
                    } catch (Exception e) {

                    }
                }
            }
            geo.close();
            return null;
        }
        return null;
    }


    /**
     * Get table Contents object
     */
    public Contents getTableContents(String gpName, String tableName) {
        GeoPackage geo = null;
        try{
            geo = manager.open(gpName);
            if(geo != null) {
                ContentsDao contentsDao = geo.getContentsDao();
                Contents contents = contentsDao.queryForId(tableName);
                return contents;
            }

        } catch (Exception e){

        } finally {
            if(geo !=  null){
                geo.close();
            }
        }
        return null;
    }


    /**
     * Create an alert dialog with a GeoPackage's details for viewing
     *
     * @param geoPackageName
     * @param activity
     * @return
     */
    public AlertDialog getGeoPackageDetailDialog(String geoPackageName, Activity activity) {
        StringBuilder databaseInfo = new StringBuilder();
        GeoPackage geoPackage = manager.open(geoPackageName, false);
        try {
            SpatialReferenceSystemDao srsDao = geoPackage
                    .getSpatialReferenceSystemDao();

            List<SpatialReferenceSystem> srsList = srsDao.queryForAll();
            databaseInfo.append("Size: ")
                    .append(manager.readableSize(geoPackageName));
            databaseInfo.append("\n\nLocation: ").append(
                    manager.isExternal(geoPackageName) ? "External" : "Local");
            databaseInfo.append("\nPath: ").append(manager.getPath(geoPackageName));
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
        AlertDialog viewDialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setTitle(geoPackageName)
                .setPositiveButton(R.string.button_ok_label,

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage(databaseInfo.toString()).create();

        return viewDialog;

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
}
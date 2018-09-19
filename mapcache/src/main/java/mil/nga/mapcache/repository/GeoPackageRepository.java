package mil.nga.mapcache.repository;

import android.app.Application;
import android.support.annotation.NonNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.db.metadata.GeoPackageMetadata;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.sf.GeometryType;

/**
 *  Repository to provide access to stored GeoPackages
 */

public class GeoPackageRepository {

    private GeoPackageManager manager;
    private List<GeoPackage> geoPackages = new ArrayList<>();

    public GeoPackageRepository(@NonNull Application application){
        manager = GeoPackageFactory.getManager(application);
    }

    public GeoPackage getGeoPackageByName(String name){
        GeoPackage geo = manager.open(name);
        return geo;
    }

    public boolean setGeoPackageName(String oldName, String newName){
        return manager.rename(oldName, newName);
    }

    public List<GeoPackage> getGeoPackages() {
        return geoPackages;
    }

    public List<List<GeoPackageTable>> regenerateTableList(){
        geoPackages.clear();
        List<List<GeoPackageTable>> databaseTables = new ArrayList<List<GeoPackageTable>>();
        StringBuilder errorMessage = new StringBuilder();
        Iterator<String> databasesIterator = manager.databases().iterator();
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
                    geoPackages.add(geoPackage);
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
                    exceptions.add(e);
                }

                if (geoPackage != null) {
                    geoPackage.close();
                }

                // If There are no tables under the database, create a blank table so that we can at
                // least pass the database name up to the recycler view
                if (tables.isEmpty()) {
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

        return databaseTables;
    }


    /**
     *  Returns the GeoPackage's size
     */
    public String getGeoPackageSize(String geoPackageName){
        return manager.readableSize(geoPackageName);
    }

    /**
     * Delete a geoPackage by name
     */
    public boolean deleteGeoPackage(String geoPackageName){
        return manager.delete(geoPackageName);
    }

    /**
     * Create a geoPackage by name
     */
    public boolean createGeoPackage(String geoPackageName){
        return manager.create(geoPackageName);
    }

    /**
     * import a geopackage from url.  GeoPackageProgress should be an instance of DownloadTask
     */
    public boolean importGeoPackage(String name, URL source, GeoPackageProgress progress){
        return manager.importGeoPackage(name, source, progress);
    }

}

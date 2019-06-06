package mil.nga.mapcache.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Collection of active GeoPackage database tables
 *
 * @author osbornb
 */
public class GeoPackageDatabases {

    /**
     * Database preferences value
     */
    private String databasePreference = "databases";

    /**
     * Tile Tables Preference suffix
     */
    private static final String TILE_TABLES_PREFERENCE_SUFFIX = "_tile_tables";

    /**
     * Feature Tables Preference suffix
     */
    private static final String FEATURE_TABLES_PREFERENCE_SUFFIX = "_feature_tables";

    /**
     * Feature Overlay Tables Preference suffix
     */
    private static final String FEATURE_OVERLAY_TABLES_PREFERENCE_SUFFIX = "_feature_overlay_tables";

    /**
     * Initialization lock
     */
    private static final Lock initializeLock = new ReentrantLock();

    /**
     * Map of databases
     */
    private Map<String, GeoPackageDatabase> databases = new HashMap<String, GeoPackageDatabase>();

    /**
     * Context
     */
    private Context context;

    /**
     * Shared preference settings
     */
    private SharedPreferences settings;

    /**
     * Modified flag
     */
    private boolean modified = false;

    /**
     * prefix to add to the saved preference before saving
     */
    private String prefix;

    /**
     * Public constructor
     * @param context application context used for opening PreferenceManager
     */
    public GeoPackageDatabases(Context context, String prefix){
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        this.context = context;
        this.settings = preferences;
        this.prefix = prefix;
        databasePreference = prefix + "databases";
        // Load current preference data
        loadFromPreferences();
    }

    /**
     * Set the size field of a GeoPackageDatabase
     * @param database GeoPackage (database) name
     * @param size file size in string format
     */
    public void setDatabaseSize(String database, String size){
        if(databases.get(database) != null) {
            databases.get(database).setSize(size);
        }
    }

    /**
     * Check if the table exists in this collection of tables, is active
     *
     * @param table
     * @return
     */
    public boolean exists(GeoPackageTable table) {
        boolean exists = false;
        GeoPackageDatabase database = getDatabase(table);
        if (database != null) {
            exists = database.exists(table);
        }
        return exists;
    }

    /**
     * Check if the database, table, and type exists, is active
     *
     * @param database  database name
     * @param table     table name
     * @param tableType table type
     * @return true if exists
     */
    public boolean exists(String database, String table, GeoPackageTableType tableType) {
        boolean exists = false;
        GeoPackageDatabase db = getDatabase(database);
        if (db != null) {
            exists = db.exists(table, tableType);
        }
        return exists;
    }

    /**
     * Get feature overlays for the database
     *
     * @param databaseName
     * @return
     */
    public Collection<GeoPackageFeatureOverlayTable> featureOverlays(String databaseName) {

        List<GeoPackageFeatureOverlayTable> response = new ArrayList<GeoPackageFeatureOverlayTable>();

        Set<String> featureOverlays = settings.getStringSet(
                getFeatureOverlayTablesPreferenceKey(databaseName),
                new HashSet<String>());

        for (String featureOverlay : featureOverlays) {
            GeoPackageTable geoPackageTable = readTableFile(databaseName, featureOverlay);
            if (geoPackageTable != null) {
                response.add((GeoPackageFeatureOverlayTable) geoPackageTable);
            }
        }

        return response;
    }

    public GeoPackageDatabase getDatabase(GeoPackageTable table) {
        return getDatabase(table.getDatabase());
    }

    public GeoPackageDatabase getDatabase(String database) {
        return databases.get(database);
    }

    /**
     * Get the database
     *
     * @return
     */
    public Collection<GeoPackageDatabase> getDatabases() {
        return databases.values();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Add a table
     *
     * @param table
     */
    public void addTable(GeoPackageTable table) {
        addTable(table, true);
    }

    /**
     * Add a table and update the saved preferences if flag is set
     *
     * @param table
     * @param updatePreferences
     */
    public void addTable(GeoPackageTable table, boolean updatePreferences) {
        GeoPackageDatabase database = databases.get(table.getDatabase());
        if (database == null) {
            database = new GeoPackageDatabase(table.getDatabase());
            databases.put(table.getDatabase(), database);
        }
        database.add(table);
        if (updatePreferences) {
            addTableToPreferences(table);
        }
        setModified(true);
    }

    /**
     * Add the given database to the list of databases.
     * @param db GeoPackageDatabase
     */
    public void addAll(GeoPackageDatabase db){
        if(databases.containsKey(db.getDatabase())){
            databases.remove(db.getDatabase());
        }

        for(GeoPackageTable table : db.getAllTables()){
            addTable(table, true);
        }
        // Make sure to set the db size
        databases.get(db.getDatabase()).setSize(db.getSize());
        setModified(true);
    }

    /**
     * Add a GeoPackageDatabase with no tables
     * @param dbName
     */
    public void addEmptyDatabase(String dbName){
        GeoPackageDatabase database = databases.get(dbName);
        if(database == null){
            database = new GeoPackageDatabase(dbName);
            databases.put(dbName, database);
        }
    }

    /**
     * Remove a table
     *
     * @param table
     */
    public void removeTable(GeoPackageTable table) {
        removeTable(table, false);
    }

    /**
     * Sets the has active tables flag of the database
     * @param table table containing the active state
     */
    public void setTableActive(GeoPackageTable table){
        GeoPackageDatabase database = databases.get(table.getDatabase());
        if (database != null) {
            database.setActiveTables(table.isActive());
            setModified(true);
        }
    }

    /**
     * Sets all tables in the given database to the given active state
     * @param activeState active or inactve
     * @param geoPackageName name of the database to set table active state
     */
    public void setDatabaseLayersActive(boolean activeState, String geoPackageName){
        GeoPackageDatabase database = databases.get(geoPackageName);
        if (database != null) {
            database.setAllTablesActiveState(activeState);
        }
    }

    /**
     * Set all layers in every geopackage to the given active state
     * @param activeState active or inactive
     */
    public void setAllDatabaseLayersActive(boolean activeState){
        for(GeoPackageDatabase db : getDatabases()){
            setDatabaseLayersActive(activeState, db.getDatabase());
        }
        setModified(true);
    }

    /**
     * Set a layer in the given geopackage to the active state
     * @param activeState active or inactive
     */
    public void setLayerActive(boolean activeState, String geoPackageName, String layerName){
        for(GeoPackageDatabase db : getDatabases()){
            if(db.getDatabase().equalsIgnoreCase(geoPackageName)){
                db.setTableActiveState(activeState, layerName);
            }
        }
    }

    /**
     * Remove all databases from the list and preference file
     */
    public void clearAllDatabases(){
        getDatabases().clear();
        removeAllDatabasesFromPreferences();
        setModified(true);
    }

    /**
     * Remove a table
     *
     * @param table
     * @param preserveOverlays
     */
    public void removeTable(GeoPackageTable table, boolean preserveOverlays) {
        GeoPackageDatabase database = databases.get(table.getDatabase());
        if (database != null) {
            database.remove(table);
            removeTableFromPreferences(table);
            if (database.isEmpty()) {
                databases.remove(database.getDatabase());
                removeDatabaseFromPreferences(database.getDatabase(), preserveOverlays);
            }
            if (!preserveOverlays && table.getType() == GeoPackageTableType.FEATURE) {
                List<GeoPackageFeatureOverlayTable> deleteFeatureOverlays = new ArrayList<GeoPackageFeatureOverlayTable>();
                int count = 0;
                for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                    if (featureOverlay.getFeatureTable().equals(table.getName())) {
                        deleteFeatureOverlays.add(featureOverlay);
                        count++;
                    }
                }
                for (GeoPackageFeatureOverlayTable featureOverlay : deleteFeatureOverlays) {
                    removeTable(featureOverlay);
                }
            }
            setModified(true);
        }
    }

    /**
     * Remove a table by name
     *
     * @param databaseName name of the GeoPackage database to remove the table from
     * @param tableName Name of the table to remove
     */
    public void removeTable(String databaseName, String tableName) {
        GeoPackageDatabase database = databases.get(databaseName);
        if (database != null) {
            GeoPackageTable table = database.getTableByName(tableName);
            if(table != null) {
                removeTable(table);
            }
        }
    }

    public boolean isEmpty() {
        return getTableCount() == 0;
    }

    public int getTableCount() {
        int count = 0;
        for (GeoPackageDatabase database : databases.values()) {
            count += database.getTableCount();
        }
        return count;
    }

    public int getActiveTableCount() {
        int count = 0;
        for (GeoPackageDatabase database : databases.values()) {
            count += database.getActiveTableCount();
        }
        return count;
    }

    /**
     * Clear all active databases
     */
    public void clearActive() {
        Set<String> allDatabases = new HashSet<String>();
        allDatabases.addAll(databases.keySet());
        for (String database : allDatabases) {
            GeoPackageDatabase db = databases.get(database);
            for (GeoPackageTable table : db.getFeatureOverlays()) {
                if (table.isActive()) {
                    table.setActive(false);
                    addTable(table, true);
                }
            }
            removeDatabase(database, true);
        }
    }

    /**
     * Get the total number of all features in all databases
     */
    public int getAllFeaturesCount(){
        int totalFeatures = 0;
        for(GeoPackageDatabase db : getDatabases()){
            totalFeatures += db.getTotalFeatureCount();
        }
        return totalFeatures;
    }

    /**
     * Get the total number of all active features in all databases
     */
    public int getAllActiveFeaturesCount(){
        int totalFeatures = 0;
        for(GeoPackageDatabase db : getDatabases()){
            totalFeatures += db.getTotalActiveFeatureCount();
        }
        return totalFeatures;
    }

    /**
     * Remove a database
     *
     * @param database
     * @param preserveOverlays
     */
    public void removeDatabase(String database, boolean preserveOverlays) {
        databases.remove(database);
        removeDatabaseFromPreferences(database, preserveOverlays);
        setModified(true);
    }

    /**
     * Rename a database
     *
     * @param database
     * @param newDatabase
     */
    public void renameDatabase(String database, String newDatabase) {
        GeoPackageDatabase geoPackageDatabase = databases.remove(database);
        if (geoPackageDatabase != null) {
            geoPackageDatabase.setDatabase(newDatabase);
            databases.put(newDatabase, geoPackageDatabase);
            removeDatabaseFromPreferences(database, false);
            for (GeoPackageTable featureTable : geoPackageDatabase
                    .getFeatures()) {
                featureTable.setDatabase(newDatabase);
                addTableToPreferences(featureTable);
            }
            for (GeoPackageTable tileTable : geoPackageDatabase.getTiles()) {
                tileTable.setDatabase(newDatabase);
                addTableToPreferences(tileTable);
            }
            for (GeoPackageTable featureOverlayTable : geoPackageDatabase.getFeatureOverlays()) {
                featureOverlayTable.setDatabase(newDatabase);
                addTableToPreferences(featureOverlayTable);
            }
        }
        setModified(true);
    }

    /**
     * Load the GeoPackage databases from the saved preferences
     */
    public void loadFromPreferences() {
        databases.clear();
        Set<String> databases = settings.getStringSet(databasePreference,
                new HashSet<String>());
        for (String database : databases) {
            Set<String> tiles = settings
                    .getStringSet(getTileTablesPreferenceKey(database),
                            new HashSet<String>());
            Set<String> features = settings.getStringSet(
                    getFeatureTablesPreferenceKey(database),
                    new HashSet<String>());
            Set<String> featureOverlays = settings.getStringSet(
                    getFeatureOverlayTablesPreferenceKey(database),
                    new HashSet<String>());

            for (String tile : tiles) {
                addTable(new GeoPackageTileTable(database, tile, 0), false);
            }
            for (String feature : features) {
                addTable(new GeoPackageFeatureTable(database, feature, null,
                        0), false);
            }
            for (String featureOverlay : featureOverlays) {
                GeoPackageTable geoPackageTable = readTableFile(database, featureOverlay);
                if (geoPackageTable != null) {
                    addTable(geoPackageTable, false);
                }
            }
        }
    }

    /**
     * Remove the database from the saved preferences
     *
     * @param database
     * @param preserveOverlays
     */
    private void removeDatabaseFromPreferences(String database, boolean preserveOverlays) {
        Editor editor = settings.edit();

        Set<String> databases = settings.getStringSet(databasePreference,
                new HashSet<String>());
        if (databases != null && databases.contains(database)) {
            Set<String> newDatabases = new HashSet<String>();
            newDatabases.addAll(databases);
            newDatabases.remove(database);
            editor.putStringSet(databasePreference, newDatabases);
        }
        editor.remove(getTileTablesPreferenceKey(database));
        editor.remove(getFeatureTablesPreferenceKey(database));
        if (!preserveOverlays) {
            editor.remove(getFeatureOverlayTablesPreferenceKey(database));
            deleteTableFiles(database);
        }

        editor.commit();
    }

    /**
     * Remove all databases from the preferences file
     */
    private void removeAllDatabasesFromPreferences(){
        Editor editor = settings.edit();
        Set<String> emptyDatabases = new HashSet<String>();
        editor.putStringSet(databasePreference, emptyDatabases);
        editor.commit();
    }

    /**
     * Remove a table from the preferences
     *
     * @param table
     */
    private void removeTableFromPreferences(GeoPackageTable table) {
        Editor editor = settings.edit();

        switch (table.getType()) {

            case FEATURE:
                Set<String> features = settings
                        .getStringSet(getFeatureTablesPreferenceKey(table),
                                new HashSet<String>());
                if (features != null && features.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<String>();
                    newFeatures.addAll(features);
                    newFeatures.remove(table.getName());
                    editor.putStringSet(getFeatureTablesPreferenceKey(table),
                            newFeatures);
                }
                break;

            case TILE:
                Set<String> tiles = settings.getStringSet(
                        getTileTablesPreferenceKey(table), new HashSet<String>());
                if (tiles != null && tiles.contains(table.getName())) {
                    Set<String> newTiles = new HashSet<String>();
                    newTiles.addAll(tiles);
                    newTiles.remove(table.getName());
                    editor.putStringSet(getTileTablesPreferenceKey(table), newTiles);
                }
                break;

            case FEATURE_OVERLAY:
                Set<String> featureOverlays = settings
                        .getStringSet(getFeatureOverlayTablesPreferenceKey(table),
                                new HashSet<String>());
                if (featureOverlays != null && featureOverlays.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<String>();
                    newFeatures.addAll(featureOverlays);
                    newFeatures.remove(table.getName());
                    editor.putStringSet(getFeatureOverlayTablesPreferenceKey(table),
                            newFeatures);
                }
                deleteTableFile(table);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

        editor.commit();
    }

    /**
     * Add a table to the preferences, updating the saved databases and tables
     * as needed
     *
     * @param table
     */
    private void addTableToPreferences(GeoPackageTable table) {
        Editor editor = settings.edit();

        Set<String> databases = settings.getStringSet(databasePreference,
                new HashSet<String>());
        if (databases == null || !databases.contains(table.getDatabase())) {
            Set<String> newDatabases = new HashSet<String>();
            if (databases != null) {
                newDatabases.addAll(databases);
            }
            newDatabases.add(table.getDatabase());
            editor.putStringSet(databasePreference, newDatabases);
        }

        switch (table.getType()) {

            case FEATURE:
                Set<String> features = settings
                        .getStringSet(getFeatureTablesPreferenceKey(table),
                                new HashSet<String>());
                if (features == null || !features.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<String>();
                    if (features != null) {
                        newFeatures.addAll(features);
                    }
                    newFeatures.add(table.getName());
                    editor.putStringSet(getFeatureTablesPreferenceKey(table),
                            newFeatures);
                }
                break;

            case TILE:
                Set<String> tiles = settings.getStringSet(
                        getTileTablesPreferenceKey(table), new HashSet<String>());
                if (tiles == null || !tiles.contains(table.getName())) {
                    Set<String> newTiles = new HashSet<String>();
                    if (tiles != null) {
                        newTiles.addAll(tiles);
                    }
                    newTiles.add(table.getName());
                    editor.putStringSet(getTileTablesPreferenceKey(table), newTiles);
                }
                break;

            case FEATURE_OVERLAY:
                Set<String> featureOverlays = settings
                        .getStringSet(getFeatureOverlayTablesPreferenceKey(table),
                                new HashSet<String>());
                if (featureOverlays == null || !featureOverlays.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<String>();
                    if (featureOverlays != null) {
                        newFeatures.addAll(featureOverlays);
                    }
                    newFeatures.add(table.getName());
                    editor.putStringSet(getFeatureOverlayTablesPreferenceKey(table),
                            newFeatures);
                }
                writeTableFile(table);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

        editor.commit();
    }

    /**
     * Get the Tiles Table Preference Key from table
     *
     * @param table
     * @return
     */
    private String getTileTablesPreferenceKey(GeoPackageTable table) {
        return getTileTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Tiles Table Preference Key from database name
     *
     * @param database
     * @return
     */
    private String getTileTablesPreferenceKey(String database) {
        return prefix + database + TILE_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Get the Feature Table Preference Key from table
     *
     * @param table
     * @return
     */
    private String getFeatureTablesPreferenceKey(GeoPackageTable table) {
        return getFeatureTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Feature Table Preference Key from database name
     *
     * @param database
     * @return
     */
    private String getFeatureTablesPreferenceKey(String database) {
        return prefix + database + FEATURE_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Get the Feature Overlays Table Preference Key from table
     *
     * @param table
     * @return
     */
    private String getFeatureOverlayTablesPreferenceKey(GeoPackageTable table) {
        return getFeatureOverlayTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Feature Overlays Table Preference Key from database name
     *
     * @param database
     * @return
     */
    private String getFeatureOverlayTablesPreferenceKey(String database) {
        return prefix + database + FEATURE_OVERLAY_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Write a table data file
     *
     * @param table
     */
    private void writeTableFile(GeoPackageTable table) {

        String fileName = getFileName(prefix, table);

        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(table);
            fileOut.getFD().sync();
        } catch (IOException e) {
            Log.e(GeoPackageDatabases.class.getSimpleName(), "Failed to save table preference data: " + fileName, e);
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }

    /**
     * Read a table data file
     *
     * @return
     */
    private GeoPackageTable readTableFile(String database, String table) {

        String fileName = getFileName(prefix, database, table);

        ObjectInputStream objectIn = null;
        Object object = null;
        try {

            FileInputStream fileIn = context.getApplicationContext().openFileInput(fileName);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();

        } catch (Exception e) {
            Log.e(GeoPackageDatabases.class.getSimpleName(), "Failed to read table preference data: " + fileName, e);
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        GeoPackageTable geoPackageTable = null;
        if (object != null) {
            geoPackageTable = (GeoPackageTable) object;
        }

        return geoPackageTable;
    }

    /**
     * Delete a table data file
     *
     * @param table
     * @return
     */
    private boolean deleteTableFile(GeoPackageTable table) {
        return deleteTableFile(table.getDatabase(), table.getName());
    }

    /**
     * Delete a table data file
     *
     * @param database
     * @param table
     * @return
     */
    private boolean deleteTableFile(String database, String table) {
        String fileName = getFileName(prefix, database, table);
        return context.deleteFile(fileName);
    }

    /**
     * Delete table data files for a database
     *
     * @param database
     */
    private void deleteTableFiles(String database) {
        String filePrefix = getFileName(prefix, database, "");
        for (String file : context.fileList()) {
            if (file.startsWith(filePrefix)) {
                context.deleteFile(file);
            }
        }
    }

    /**
     * Get file name
     *
     * @param table
     * @return
     */
    private static String getFileName(String prefix, GeoPackageTable table) {
        return getFileName(prefix, table.getDatabase(), table.getName());
    }

    /**
     * Get file name
     *
     * @param database
     * @param table
     * @return
     */
    private static String getFileName(String prefix, String database, String table) {
        return prefix + database + "-" + table;
    }

}

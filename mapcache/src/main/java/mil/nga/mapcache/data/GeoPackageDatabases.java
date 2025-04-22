package mil.nga.mapcache.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.preference.PreferenceManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Collection of active GeoPackage database tables
 *
 * @author osbornb
 */
public class GeoPackageDatabases {

    /**
     * Database preferences value
     */
    private final String databasePreference;

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
     * Map of databases
     */
    private final ConcurrentMap<String, GeoPackageDatabase> databases = new ConcurrentHashMap<>();

    /**
     * Context
     */
    private final Context context;

    /**
     * Shared preference settings
     */
    private final SharedPreferences settings;

    /**
     * Modified flag
     */
    private boolean modified = false;

    /**
     * prefix to add to the saved preference before saving
     */
    private final String prefix;

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
        GeoPackageDatabase db = databases.get(database);
        if(db != null) {
            db.setSize(size);
        }
    }

    /**
     * Check if the GeoPackage name exists in our list (ignoring capitalization)
     */
    public boolean geoPackageNameExists(String name) {
        for(GeoPackageDatabase db : getDatabases()){
            if(db.getDatabase().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the table exists in this collection of tables, is active
     *
     * @param table The table to check.
     * @return True if it exists, false if it does not.
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
     * Searches all table types to find out if a table exists in a geoPackage
     */
    public boolean exists(String database, String table){
        return exists(database, table, GeoPackageTableType.FEATURE) ||
                exists(database, table, GeoPackageTableType.TILE) ||
                exists(database, table, GeoPackageTableType.FEATURE_OVERLAY);
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
     * @return The collection of geoPackages.
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
     * @param table The table to add.
     */
    public void addTable(GeoPackageTable table) {
        addTable(table, true);
    }

    /**
     * Add a table and update the saved preferences if flag is set
     *
     * @param table The table to add.
     * @param updatePreferences True if preferences should reflect this add.
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
     * Add the given database to the list of databases.  Only if it has tables
     * @param db GeoPackageDatabase
     */
    public void addAll(GeoPackageDatabase db){
        if(db.getTableCount() > 0) {
            databases.remove(db.getDatabase());

            for (GeoPackageTable table : db.getAllTables()) {
                addTable(table, true);
            }
            // Make sure to set the db size
            GeoPackageDatabase geo = databases.get(db.getDatabase());
            if (geo != null) {
                geo.setSize(db.getSize());
                geo.setActiveTables(true);
            }
            setModified(true);
        }
    }

    /**
     * Add a GeoPackageDatabase with no tables
     * @param dbName The name of the geoPackage.
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
     * @param table The table to remove.
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
     * @param activeState active or inactive
     * @param geoPackageName name of the database to set table active state
     */
    public void setDatabaseLayersActive(boolean activeState, String geoPackageName){
        GeoPackageDatabase database = databases.get(geoPackageName);
        if (database != null) {
            database.setAllTablesActiveState(activeState);
        }
    }

    /**
     * Set all layers in every geoPackage to the given active state
     * @param activeState active or inactive
     */
    public void setAllDatabaseLayersActive(boolean activeState){
        for(GeoPackageDatabase db : getDatabases()){
            setDatabaseLayersActive(activeState, db.getDatabase());
        }
        setModified(true);
    }

    /**
     * Set a layer in the given geoPackage to the active state
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
     * @param table The table to remove.
     * @param preserveOverlays True if overlays should stay.
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
                List<GeoPackageFeatureOverlayTable> deleteFeatureOverlays = new ArrayList<>();
                for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                    if (featureOverlay.getFeatureTable().equals(table.getName())) {
                        deleteFeatureOverlays.add(featureOverlay);
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
     * Get the total number of all tiles in all databases
     */
    public int getAllTilesCount(){
        int totalTiles = 0;
        for(GeoPackageDatabase db : getDatabases()){
            totalTiles += db.getTotalTileCount();
        }
        return totalTiles;
    }

    /**
     * Get the total number of features and tiles in all databases
     */
    public int getAllFeaturesAndTilesCount(){
        int totalActive = getAllFeaturesCount();
        totalActive += getAllTilesCount();
        return totalActive;
    }

    /**
     * Remove a database
     *
     * @param database The name of the geoPackage to remove.
     * @param preserveOverlays True if overlays should be saved.
     */
    public void removeDatabase(String database, boolean preserveOverlays) {
        databases.remove(database);
        removeDatabaseFromPreferences(database, preserveOverlays);
        setModified(true);
    }

    /**
     * Rename a database
     *
     * @param database The name of the geoPackage to rename.
     * @param newDatabase The new
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
        try {
            final Set<String> databases = settings.getStringSet(databasePreference,
                    new HashSet<>());
            for (String database : databases) {
                Set<String> tiles = settings
                        .getStringSet(getTileTablesPreferenceKey(database),
                                new HashSet<>());
                Set<String> features = settings.getStringSet(
                        getFeatureTablesPreferenceKey(database),
                        new HashSet<>());
                Set<String> featureOverlays = settings.getStringSet(
                        getFeatureOverlayTablesPreferenceKey(database),
                        new HashSet<>());

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
        } catch (Exception e){
            Log.e(GeoPackageDatabases.class.getSimpleName(),
                    "Failed to load databasePreference settings: ", e);
        }
    }

    /**
     * Remove the database from the saved preferences
     *
     * @param database The name of the geoPackage to remove.
     * @param preserveOverlays True if overlays should be saved.
     */
    private void removeDatabaseFromPreferences(String database, boolean preserveOverlays) {
        Editor editor = settings.edit();

        Set<String> databases = settings.getStringSet(databasePreference,
                new HashSet<>());
        if (databases != null && databases.contains(database)) {
            Set<String> newDatabases = new HashSet<>(databases);
            newDatabases.remove(database);
            editor.putStringSet(databasePreference, newDatabases);
        }
        editor.remove(getTileTablesPreferenceKey(database));
        editor.remove(getFeatureTablesPreferenceKey(database));
        if (!preserveOverlays) {
            editor.remove(getFeatureOverlayTablesPreferenceKey(database));
            deleteTableFiles(database);
        }

        editor.apply();
    }

    /**
     * Remove all databases from the preferences file
     */
    private void removeAllDatabasesFromPreferences(){
        Editor editor = settings.edit();
        Set<String> emptyDatabases = new HashSet<>();
        editor.putStringSet(databasePreference, emptyDatabases);
        editor.apply();
    }

    /**
     * Remove a table from the preferences
     *
     * @param table The geoPackage to remove.
     */
    private void removeTableFromPreferences(GeoPackageTable table) {
        Editor editor = settings.edit();

        switch (table.getType()) {

            case FEATURE:
                Set<String> features = settings
                        .getStringSet(getFeatureTablesPreferenceKey(table),
                                new HashSet<>());
                if (features != null && features.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<>(features);
                    newFeatures.remove(table.getName());
                    editor.putStringSet(getFeatureTablesPreferenceKey(table),
                            newFeatures);
                }
                break;

            case TILE:
                Set<String> tiles = settings.getStringSet(
                        getTileTablesPreferenceKey(table), new HashSet<>());
                if (tiles != null && tiles.contains(table.getName())) {
                    Set<String> newTiles = new HashSet<>(tiles);
                    newTiles.remove(table.getName());
                    editor.putStringSet(getTileTablesPreferenceKey(table), newTiles);
                }
                break;

            case FEATURE_OVERLAY:
                Set<String> featureOverlays = settings
                        .getStringSet(getFeatureOverlayTablesPreferenceKey(table),
                                new HashSet<>());
                if (featureOverlays != null && featureOverlays.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<>(featureOverlays);
                    newFeatures.remove(table.getName());
                    editor.putStringSet(getFeatureOverlayTablesPreferenceKey(table),
                            newFeatures);
                }
                deleteTableFile(table);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

        editor.apply();
    }

    /**
     * Add a table to the preferences, updating the saved databases and tables
     * as needed
     *
     * @param table The geoPackage to add to preferences.
     */
    private void addTableToPreferences(GeoPackageTable table) {
        Editor editor = settings.edit();

        Set<String> databases = settings.getStringSet(databasePreference,
                new HashSet<>());
        if (databases == null || !databases.contains(table.getDatabase())) {
            Set<String> newDatabases = new HashSet<>();
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
                                new HashSet<>());
                if (features == null || !features.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<>();
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
                        getTileTablesPreferenceKey(table), new HashSet<>());
                if (tiles == null || !tiles.contains(table.getName())) {
                    Set<String> newTiles = new HashSet<>();
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
                                new HashSet<>());
                if (featureOverlays == null || !featureOverlays.contains(table.getName())) {
                    Set<String> newFeatures = new HashSet<>();
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

        editor.apply();
    }

    /**
     * Get the Tiles Table Preference Key from table
     *
     * @param table The geoPackage layer to get the tiles table key for.
     * @return The tiles table key.
     */
    private String getTileTablesPreferenceKey(GeoPackageTable table) {
        return getTileTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Tiles Table Preference Key from database name
     *
     * @param database The geoPackage layer to get the tiles table key for.
     * @return The tiles table key.
     */
    private String getTileTablesPreferenceKey(String database) {
        return prefix + database + TILE_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Get the Feature Table Preference Key from table
     *
     * @param table The geoPackage layer to get the features table key for.
     * @return The features table key.
     */
    private String getFeatureTablesPreferenceKey(GeoPackageTable table) {
        return getFeatureTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Feature Table Preference Key from database name
     *
     * @param database The geoPackage layer to get the features table key for.
     * @return The features table key.
     */
    private String getFeatureTablesPreferenceKey(String database) {
        return prefix + database + FEATURE_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Get the Feature Overlays Table Preference Key from table
     *
     * @param table The geoPackage layer to get the feature overlay table key for.
     * @return The feature overlay key.
     */
    private String getFeatureOverlayTablesPreferenceKey(GeoPackageTable table) {
        return getFeatureOverlayTablesPreferenceKey(table.getDatabase());
    }

    /**
     * Get the Feature Overlays Table Preference Key from database name
     *
     * @param database The geoPackage layer to get the feature overlay key for.
     * @return The feature overlay key.
     */
    private String getFeatureOverlayTablesPreferenceKey(String database) {
        return prefix + database + FEATURE_OVERLAY_TABLES_PREFERENCE_SUFFIX;
    }

    /**
     * Write a table data file
     *
     * @param table The table to write.
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
     * @return reads the table file.
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
     * @param table The table to delete.
     */
    private void deleteTableFile(GeoPackageTable table) {
        deleteTableFile(table.getDatabase(), table.getName());
    }

    /**
     * Delete a table data file
     *
     * @param database The geoPackage the table to delete belongs to.
     * @param table The table to delete.
     */
    private void deleteTableFile(String database, String table) {
        String fileName = getFileName(prefix, database, table);
        context.deleteFile(fileName);
    }

    /**
     * Delete table data files for a database
     *
     * @param database The database to delete.
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
     * @param table The table to get the file name for.
     * @return The file name of the table.
     */
    private static String getFileName(String prefix, GeoPackageTable table) {
        return getFileName(prefix, table.getDatabase(), table.getName());
    }

    /**
     * Get file name
     *
     * @param database The geoPackage containing the table to get the file name.
     * @param table The table to get the file name for.
     * @return The file name of the table.
     */
    private static String getFileName(String prefix, String database, String table) {
        return prefix + database + "-" + table;
    }

}

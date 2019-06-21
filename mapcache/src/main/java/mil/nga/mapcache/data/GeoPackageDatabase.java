package mil.nga.mapcache.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 * Active feature and tile tables within a GeoPackage database
 *
 * @author osbornb
 */
public class GeoPackageDatabase {

    /**
     * Map of table names to feature tables
     */
    private final Map<String, GeoPackageFeatureTable> features = new HashMap<String, GeoPackageFeatureTable>();

    /**
     * Map of tables names to tile tables
     */
    private final Map<String, GeoPackageTileTable> tiles = new HashMap<String, GeoPackageTileTable>();

    /**
     * Map of table names to feature overlay tables
     */
    private final Map<String, GeoPackageFeatureOverlayTable> featureOverlays = new HashMap<String, GeoPackageFeatureOverlayTable>();

    /**
     * Database name
     */
    private String database;

    /**
     * Database size
     */
    private String size;

    /**
     * True if any of the tables in this GeoPackage are active
     */
    private boolean activeTables;

    /**
     * Constructor
     *
     * @param database
     */
    public GeoPackageDatabase(String database) {
        this.database = database;
        this.size = "0mb";
    }

    /**
     * Get the feature tables
     *
     * @return
     */
    public Collection<GeoPackageFeatureTable> getFeatures() {
        return features.values();
    }

    /**
     * Get the feature count
     *
     * @return
     */
    public int getFeatureCount() {
        return features.size();
    }

    /**
     * Get the feature tables
     *
     * @return
     */
    public Collection<GeoPackageFeatureOverlayTable> getFeatureOverlays() {
        return featureOverlays.values();
    }

    /**
     * Get the feature overlay count
     *
     * @return
     */
    public int getFeatureOverlayCount() {
        return featureOverlays.size();
    }

    /**
     * Get the feature overlay count
     *
     * @return
     */
    public int getActiveFeatureOverlayCount() {
        int count = 0;
        for (GeoPackageTable table : featureOverlays.values()) {
            if (table.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the total number of features in all feature tables
     */
    public int getTotalFeatureCount(){
        int totalCount = 0;
        for(GeoPackageFeatureTable feature : getFeatures()){
            totalCount += feature.getCount();
        }
        return totalCount;
    }

    /**
     * Get the total number of tiles in all tile tables
     */
    public int getTotalTileCount(){
        int totalCount = 0;
        for(GeoPackageTileTable tile : getTiles()){
            totalCount++;
        }
        return totalCount;
    }

    /**
     * Get the total number of active features in all feature tables
     */
    public int getTotalActiveFeatureCount(){
        int totalCount = 0;
        for(GeoPackageFeatureTable feature : getFeatures()){
            if(feature.isActive()) {
                totalCount += feature.getCount();
            }
        }
        return totalCount;
    }

    /**
     * Get the tile tables
     *
     * @return
     */
    public Collection<GeoPackageTileTable> getTiles() {
        return tiles.values();
    }

    /**
     * Get the tile count
     */
    public int getTileCount() {
        return tiles.size();
    }

    /**
     * Get the table count
     *
     * @return
     */
    public int getTableCount() {
        return getFeatureCount() + getTileCount() + getFeatureOverlayCount();
    }

    /**
     * Get the active table count
     *
     * @return
     */
    public int getActiveTableCount() {
        return getFeatureCount() + getTileCount() + getActiveFeatureOverlayCount();
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    /**
     * Get the size of the database file
     * @return
     */
    public String getSize() {
        return size;
    }

    /**
     * Set the size of the database file
     * @param size in string format
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Check if the table exists in this table, is active
     *
     * @param table table
     * @return true if exists
     */
    public boolean exists(GeoPackageTable table) {
        return exists(table.getName(), table.getType());
    }

    /**
     * Check if the table name of type exists, is active
     *
     * @param table     table name
     * @param tableType table type
     * @return true if exists
     */
    public boolean exists(String table, GeoPackageTableType tableType) {

        boolean exists = false;

        switch (tableType) {

            case FEATURE:
                exists = features.containsKey(table);
                break;

            case TILE:
                exists = tiles.containsKey(table);
                break;

            case FEATURE_OVERLAY:
                exists = featureOverlays.containsKey(table);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + tableType);
        }

        return exists;
    }

    /**
     * Add a table
     *
     * @param table
     */
    public void add(GeoPackageTable table) {
        switch (table.getType()) {

            case FEATURE:
                features.put(table.getName(), (GeoPackageFeatureTable) table);
                break;

            case TILE:
                tiles.put(table.getName(), (GeoPackageTileTable) table);
                break;

            case FEATURE_OVERLAY:
                featureOverlays.put(table.getName(), (GeoPackageFeatureOverlayTable) table);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }
    }

    /**
     * Remove a table
     *
     * @param table
     */
    public void remove(GeoPackageTable table) {

        switch (table.getType()) {

            case FEATURE:
                features.remove(table.getName());
                break;

            case TILE:
                tiles.remove(table.getName());
                break;

            case FEATURE_OVERLAY:
                featureOverlays.remove(table.getName());
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
        }

    }

    /**
     * find a table by name
     * @param table Name of the table to find
     * @return GeoPackageTable object if it's found
     */
    public GeoPackageTable getTableByName(String table) {
        if(features.containsKey(table)){
            return features.get(table);
        } else if(tiles.containsKey(table)){
            return tiles.get(table);
        } else if(featureOverlays.containsKey(table)){
            return featureOverlays.get(table);
        }
        return null;
    }

    /**
     * Get a list of all GeoPackageTables in this geopackage
     * @return List<GeoPackageTable> of all tables
     */
    public List<String> getAllTableNames(){
        List<String> names = new ArrayList<>();
        for(GeoPackageFeatureTable feature : getFeatures()){
            names.add(feature.getName());
        }
        for(GeoPackageTileTable tile : getTiles()){
            names.add(tile.getName());
        }
        return names;
    }

    /**
     * Return a list of all GeoPackage tables
     * @return list of all geopackage tables regardless of type
     */
    public List<GeoPackageTable> getAllTables(){
        List<GeoPackageTable> allTables = new ArrayList<>();
        for(GeoPackageFeatureTable feature : getFeatures()){
            allTables.add(feature);
        }
        for(GeoPackageTileTable tile : getTiles()){
            allTables.add(tile);
        }
        return allTables;
    }

    /**
     * Returns true if every table in this geopackage is set to active
     * @return
     */
    public boolean isEveryTableActive(){
        boolean allActive = true;
        for(GeoPackageTable table : getAllTables()){
            if(!table.isActive()){
                allActive = false;
            }
        }
        return allActive;
    }

    /**
     * Sets every table to the given active state
     * @param activeState boolean for active or inactive
     */
    public void setAllTablesActiveState(boolean activeState){
        for(GeoPackageFeatureTable feature : getFeatures()){
            feature.setActive(activeState);
        }
        for(GeoPackageTileTable tile : getTiles()){
            tile.setActive(activeState);
        }
        setActiveTables(activeState);
    }

    /**
     * Sets the given table to the given active state
     * @param activeState boolean for active or inactive
     */
    public void setTableActiveState(boolean activeState, String tableName){
        for(GeoPackageFeatureTable feature : getFeatures()){
            if(feature.getName().equalsIgnoreCase(tableName)) {
                feature.setActive(activeState);
            }
        }
        for(GeoPackageTileTable tile : getTiles()){
            if(tile.getName().equalsIgnoreCase(tableName)) {
                tile.setActive(activeState);
            }
        }
    }

    /**
     * Empty if no active tile or feature tables
     *
     * @return
     */
    public boolean isEmpty() {
        return getTableCount() == 0;
    }

    /**
     * Returns true if any tables in this GeoPackage are set to active
     * @return
     */
    public boolean isActiveTables() {
        return activeTables;
    }

    /**
     * Set the active state of this GeoPackage
     * @param activeTables
     */
    public void setActiveTables(boolean activeTables) {
        this.activeTables = activeTables;
    }

    /**
     * Create a list of DetailPageLayerObjects for every table in this GeoPackage
     * @return List<DetailPageLayerObject>
     */
    public List<DetailPageLayerObject> getLayerObjects(GeoPackageDatabase active){
        List<DetailPageLayerObject> list = new ArrayList<>(getFeatureCount() + getTileCount());
        for(GeoPackageTileTable tile : getTiles()){
            if(active != null) {
                tile.setActive(active.exists(tile));
            }
            list.add(new DetailPageLayerObject(tile.getName(), database, tile.isActive(), tile));
        }
        for(GeoPackageFeatureTable feature : getFeatures()){
            if(active != null) {
                feature.setActive(active.exists(feature));
            }
            list.add(new DetailPageLayerObject(feature.getName(), database, feature.isActive(), feature));
        }
        return list;
    }
}

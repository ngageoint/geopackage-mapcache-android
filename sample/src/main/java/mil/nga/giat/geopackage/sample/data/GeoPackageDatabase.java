package mil.nga.giat.geopackage.sample.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * Constructor
     *
     * @param database
     */
    public GeoPackageDatabase(String database) {
        this.database = database;
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
     * Get the feature tables
     *
     * @return
     */
    public Collection<GeoPackageFeatureOverlayTable> getFeatureOverlays() {
        return featureOverlays.values();
    }

    /**
     * Get the tile tables
     *
     * @return
     */
    public Collection<GeoPackageTileTable> getTiles() {
        return tiles.values();
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    /**
     * Check if the table exists in this table, is active
     *
     * @param table
     * @return
     */
    public boolean exists(GeoPackageTable table) {

        boolean exists = false;

        switch (table.getType()) {

            case FEATURE:
                exists = features.containsKey(table.getName());
                break;

            case TILE:
                exists = tiles.containsKey(table.getName());
                break;

            case FEATURE_OVERLAY:
                exists = featureOverlays.containsKey(table.getName());
                break;

            default:
                throw new IllegalArgumentException("Unsupported table type: " + table.getType());
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
                features.put(table.getName(), (GeoPackageFeatureTable)table);
                break;

            case TILE:
                tiles.put(table.getName(), (GeoPackageTileTable)table);
                break;

            case FEATURE_OVERLAY:
                featureOverlays.put(table.getName(), (GeoPackageFeatureOverlayTable)table);
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
     * Empty if no active tile or feature tables
     *
     * @return
     */
    public boolean isEmpty() {
        return features.isEmpty() && tiles.isEmpty() && featureOverlays.isEmpty();
    }

}

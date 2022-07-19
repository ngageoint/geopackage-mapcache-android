package mil.nga.mapcache.data;

import java.io.Serializable;

/**
 * GeoPackage table information
 *
 * @author osbornb
 */
public abstract class GeoPackageTable implements Serializable {

    /**
     * UID
     */
    private static final long serialVersionUID = 1;

    /**
     * Table name
     */
    private final String name;

    /**
     * Database name
     */
    private String database;

    /**
     * Count of features or tiles
     */
    private int count;

    /**
     * True when currently active or checked
     */
    private boolean active = false;

    /**
     * String description of the table
     */
    private String description;

    /**
     * Constructor
     *
     * @param database The geoPackage name.
     * @param name The layer name.
     * @param count The number of rows.
     */
    protected GeoPackageTable(String database, String name, int count) {
        this.database = database;
        this.name = name;
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract GeoPackageTableType getType();

    public String getName() {
        return name;
    }

    public String getDatabase() {
        return database;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isActive() {
        return active;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

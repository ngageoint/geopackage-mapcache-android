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
    public final String name;

    /**
     * Database name
     */
    public String database;

    /**
     * Count of features or tiles
     */
    public int count;

    /**
     * True when currently active or checked
     */
    public boolean active = false;

    /**
     * Constructor
     *
     * @param database
     * @param name
     * @param count
     */
    protected GeoPackageTable(String database, String name, int count) {
        this.database = database;
        this.name = name;
        this.count = count;
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

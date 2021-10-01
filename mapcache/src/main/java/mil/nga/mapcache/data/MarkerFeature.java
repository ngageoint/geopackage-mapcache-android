package mil.nga.mapcache.data;

import java.io.Serializable;

/**
 * Holds data for a marker that was selected on a map
 *  featureId, database, and table name
 */
public class MarkerFeature implements Serializable {
    long featureId;
    String database;
    String tableName;

    public MarkerFeature(long featureId, String database, String tableName) {
        this.featureId = featureId;
        this.database = database;
        this.tableName = tableName;
    }

    public long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(long featureId) {
        this.featureId = featureId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}

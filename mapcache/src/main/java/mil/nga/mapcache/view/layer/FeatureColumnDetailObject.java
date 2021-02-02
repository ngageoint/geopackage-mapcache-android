package mil.nga.mapcache.view.layer;

import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * Hold a name and data type for a Feature Column.  Used in the layer feature column recyclerview
 */
public class FeatureColumnDetailObject {
    // The name of this feature column
    private String name;
    // The data type for this feature column
    private GeoPackageDataType columnType;

    /**
     * Constructor
     */
    public FeatureColumnDetailObject(String name, GeoPackageDataType columnType){
        this.name = name;
        this.columnType = columnType;
    }

    /**
     * Getters and setters
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPackageDataType getColumnType() {
        return columnType;
    }

    public void setColumnType(GeoPackageDataType columnType) {
        this.columnType = columnType;
    }
}

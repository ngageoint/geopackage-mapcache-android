package mil.nga.mapcache.view.layer;

import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * Hold a name and data type for a Feature Column.  Used in the layer feature column recyclerview
 */
public class FeatureColumnDetailObject {

    /**
     * The name of this feature column
     */
    private String name;

    /**
     * The GeoPackage that this belongs to
     */
    private String geoPackageName;

    /**
     * The layer that this belongs to
     */
    private String layerName;

    /**
     * The data type for this feature column
     */
    private GeoPackageDataType columnType;

    /**
     * Constructor
     */
    public FeatureColumnDetailObject(String name, GeoPackageDataType columnType, String geoPackageName,
                                     String layerName){
        this.name = name;
        this.columnType = columnType;
        this.geoPackageName = geoPackageName;
        this.layerName = layerName;
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

    public String getGeoPackageName() {
        return geoPackageName;
    }

    public void setGeoPackageName(String geoPackageName) {
        this.geoPackageName = geoPackageName;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}

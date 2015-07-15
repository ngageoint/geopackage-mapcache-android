package mil.nga.giat.geopackage.sample.data;

import java.io.Serializable;

import mil.nga.giat.wkb.geom.GeometryType;

/**
 * GeoPackage Feature table information
 *
 * @author osbornb
 */
public class GeoPackageFeatureTable extends GeoPackageTable implements Serializable {

    /**
     * UID
     */
    private static final long serialVersionUID = 1;

    /**
     * Geometry Type
     */
    public GeometryType geometryType;

    /**
     * Create a new feature table
     *
     * @param database
     * @param name
     * @param geometryType
     * @param count
     * @return
     */
    public GeoPackageFeatureTable(String database, String name,
                                  GeometryType geometryType, int count) {
        super(database, name, count);
        this.geometryType = geometryType;
    }

    @Override
    public GeoPackageTableType getType() {
        return GeoPackageTableType.FEATURE;
    }

    public GeometryType getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
    }

}

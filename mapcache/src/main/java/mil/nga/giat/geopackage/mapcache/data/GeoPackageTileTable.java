package mil.nga.giat.geopackage.mapcache.data;

import java.io.Serializable;

/**
 * GeoPackage Tile table information
 *
 * @author osbornb
 */
public class GeoPackageTileTable extends GeoPackageTable implements Serializable {

    /**
     * UID
     */
    private static final long serialVersionUID = 1;

    /**
     * Create a new feature table
     *
     * @param database
     * @param name
     * @param count
     * @return
     */
    public GeoPackageTileTable(String database, String name,
                               int count) {
        super(database, name, count);
    }

    @Override
    public GeoPackageTableType getType() {
        return GeoPackageTableType.TILE;
    }

}

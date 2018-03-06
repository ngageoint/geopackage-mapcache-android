package mil.nga.mapcache.data;

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
     * Create a new tile table
     *
     * @param database database name
     * @param name     tile table name
     * @param count    tile count
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

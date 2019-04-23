package mil.nga.mapcache.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of all GeoPackageDatabase objects opened from the repository
 */
public class AllDatabases {

    /**
     * Map of databases
     */
    private Map<String, GeoPackageDatabase> databases = new HashMap<String, GeoPackageDatabase>();
}

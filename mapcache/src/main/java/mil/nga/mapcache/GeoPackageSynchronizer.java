package mil.nga.mapcache;

import java.util.HashMap;
import java.util.Map;

import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;

/**
 * Keeps some of the metadata about the tables in sync between GeoPackageDatabases.
 */
public class GeoPackageSynchronizer {

    /**
     * The instance of this class.
     */
    private static final GeoPackageSynchronizer instance = new GeoPackageSynchronizer();

    /**
     * Gets the instance of this class.
     * @return The instance of this class.
     */
    public static GeoPackageSynchronizer getInstance() {
        return instance;
    }

    /**
     * Synchronizes some of the metadata from one GeoPackageDatabases object to another.
     * @param from The GeoPackageDatabases to copy from.
     * @param to The GeoPackageDatabases to copy to.
     */
    public void synchronizeTables(GeoPackageDatabases from, GeoPackageDatabases to) {
        Map<String, Map<String, GeoPackageTable>> toSet = new HashMap<>();
        for(GeoPackageDatabase dbs : to.getDatabases()) {
            Map<String, GeoPackageTable> toTables = new HashMap<>();
            toSet.put(dbs.getDatabase(), toTables);
            for(GeoPackageTable table : dbs.getAllTables()) {
                toTables.put(table.getName(), table);
            }
        }

        for(GeoPackageDatabase dbs : from.getDatabases()) {
            if(toSet.containsKey(dbs.getDatabase())) {
                Map<String, GeoPackageTable> toTables = toSet.get(dbs.getDatabase());
                for (GeoPackageTable table : dbs.getAllTables()) {
                    if (toTables != null && toTables.containsKey(table.getName())) {
                        GeoPackageTable toTable = toTables.get(table.getName());
                        if(toTable != null) {
                            toTable.setCount(table.getCount());
                        }
                    }
                }
            }
        }
    }

    /**
     * Private constructor to enforce singleton.
     */
    private GeoPackageSynchronizer() {
    }
}

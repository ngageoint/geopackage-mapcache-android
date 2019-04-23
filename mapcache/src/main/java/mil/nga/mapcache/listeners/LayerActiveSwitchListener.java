package mil.nga.mapcache.listeners;

import mil.nga.mapcache.data.GeoPackageTable;

/**
 * Listener for changing the switch state on a layer in the GeoPackage detail view
 */
public interface LayerActiveSwitchListener {
    void onClick(boolean active, GeoPackageTable table);
}

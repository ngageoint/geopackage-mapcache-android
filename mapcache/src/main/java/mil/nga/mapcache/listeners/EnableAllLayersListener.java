package mil.nga.mapcache.listeners;

import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageTable;

/**
 * Switch listener for enabling all layers in a geopackage
 */
public interface EnableAllLayersListener {
    void onClick(boolean active, GeoPackageDatabase db);

}

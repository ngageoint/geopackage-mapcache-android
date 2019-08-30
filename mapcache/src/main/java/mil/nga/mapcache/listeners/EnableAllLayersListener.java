package mil.nga.mapcache.listeners;

import mil.nga.mapcache.data.GeoPackageDatabase;

/**
 * Switch listener for enabling all layers in a geopackage
 */
public interface EnableAllLayersListener {
    void onClick(boolean active, GeoPackageDatabase db);

}

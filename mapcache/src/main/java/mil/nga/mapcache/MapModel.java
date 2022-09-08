package mil.nga.mapcache;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.data.GeoPackageDatabases;

/**
 * Contains various states involving the map and its data.
 */
public class MapModel {
    /**
     * Bounding box around the features on the map
     */
    public BoundingBox featuresBoundingBox;

    /**
     * Bounding box around the tiles on the map
     */
    public BoundingBox tilesBoundingBox;

     /**
     * Active GeoPackages
     */
    public GeoPackageDatabases active;

    /**
     * True when a tile layer is drawn from features
     */
    public boolean featureOverlayTiles = false;
}

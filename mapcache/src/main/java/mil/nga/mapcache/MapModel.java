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
    private BoundingBox featuresBoundingBox;

    /**
     * Bounding box around the tiles on the map
     */
    private BoundingBox tilesBoundingBox;

     /**
     * Active GeoPackages
     */
     private GeoPackageDatabases active;

    /**
     * True when a tile layer is drawn from features
     */
    private boolean featureOverlayTiles = false;

    /**
     * Gets the bounding box around the features on the map
     * @return The bounding box around the features on the map
     */
    public BoundingBox getFeaturesBoundingBox() {
        return featuresBoundingBox;
    }

    /**
     * Sets the bounding box around the features on the map
     * @param featuresBoundingBox  The bounding box around the features on the map
     */
    public void setFeaturesBoundingBox(BoundingBox featuresBoundingBox) {
        this.featuresBoundingBox = featuresBoundingBox;
    }

    /**
     * Gets the bounding box around the tiles on the map
     * @return The bounding box around the tiles on the map
     */
    public BoundingBox getTilesBoundingBox() {
        return tilesBoundingBox;
    }

    /**
     * Sets the bounding box around the tiles on the map
     * @param tilesBoundingBox  The bounding box around the tiles on the map
     */
    public void setTilesBoundingBox(BoundingBox tilesBoundingBox) {
        this.tilesBoundingBox = tilesBoundingBox;
    }

    /**
     * Gets the active geoPackages.
     * @return The active geoPackages.
     */
    public GeoPackageDatabases getActive() {
        return active;
    }

    /**
     * Sets the active geoPackages.
     * @param active  The active geoPackages.
     */
    public void setActive(GeoPackageDatabases active) {
        this.active = active;
    }

    /**
     * Gets the flag indicating if a layer is drawn from features.
     * @return True when a tile layer is drawn from features.
     */
    public boolean isFeatureOverlayTiles() {
        return featureOverlayTiles;
    }

    /**
     * Sets the flag indicating if a layer is drawn from features.
     * @param featureOverlayTiles  True when a tile layer is drawn from features.
     */
    public void setFeatureOverlayTiles(boolean featureOverlayTiles) {
        this.featureOverlayTiles = featureOverlayTiles;
    }
}

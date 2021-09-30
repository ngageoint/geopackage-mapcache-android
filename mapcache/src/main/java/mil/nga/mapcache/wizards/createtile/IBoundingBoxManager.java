package mil.nga.mapcache.wizards.createtile;

import mil.nga.geopackage.BoundingBox;
import com.google.android.gms.maps.model.Polygon;

/**
 * Contains a bounding box.
 */
public interface IBoundingBoxManager {

    /**
     * Clears the bounding box and resets it.
     */
    public void clearBoundingBox();

    /**
     * Gets the bounding box.
     * @return The bounding box.
     */
    public Polygon getBoundingBox();
}

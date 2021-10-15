package mil.nga.mapcache.wizards.createtile;

import mil.nga.geopackage.BoundingBox;

import com.google.android.gms.maps.model.LatLng;
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
     *
     * @return The bounding box.
     */
    public Polygon getBoundingBox();

    /**
     * Sets the bounding box's start corner.
     * @param startCorner The top left corner of the bounding box.
     */
    public void setBoundingBoxStartCorner(LatLng startCorner);

    /**
     * Sets the bounding box's end corner.
     * @param endCorner The bottom right corner of the bounding box.
     */
    public void setBoundingBoxEndCorner(LatLng endCorner);

    /**
     * Draws the bounding box for the user to see.
     * @return True.
     */
    public boolean drawBoundingBox();
}

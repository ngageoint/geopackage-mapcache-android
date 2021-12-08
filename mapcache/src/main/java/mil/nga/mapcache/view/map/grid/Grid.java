package mil.nga.mapcache.view.map.grid;

import mil.nga.geopackage.BoundingBox;

/**
 * A class that represents a single grid.  Contains the grids bounds and any text for the grid.
 */
public class Grid {

    /**
     * The latitude and longitude bounds of the grid.
     */
    private BoundingBox bounds;

    /**
     * The text of the grid.
     */
    private String text;

    /**
     * Gets the latitude and longitude bounds of the grid.
     *
     * @return The bounds of the grid.
     */
    public BoundingBox getBounds() {
        return bounds;
    }

    /**
     * Sets the latitude and longitude bounds of the grid.
     *
     * @param bounds The new bounds of the grid.
     */
    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Gets the text of the grid.
     *
     * @return The text of the grid.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the grid.
     *
     * @param text The new text of the grid.
     */
    public void setText(String text) {
        this.text = text;
    }
}

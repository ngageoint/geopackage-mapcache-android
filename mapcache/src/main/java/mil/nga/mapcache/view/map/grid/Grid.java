package mil.nga.mapcache.view.map.grid;

import android.graphics.Color;

import org.locationtech.jts.geom.Polygon;

import mil.nga.geopackage.BoundingBox;

/**
 * A class that represents a single grid.  Contains the grids bounds and any text for the grid.
 */
public class Grid {

    /**
     * The latitude and longitude bounds of the grid.
     */
    private Polygon bounds;

    /**
     * The text of the grid.
     */
    private String text;

    /**
     * The color of the grid.
     */
    private int color = Color.BLACK;

    /**
     * Gets the latitude and longitude bounds of the grid.
     *
     * @return The bounds of the grid.
     */
    public Polygon getBounds() {
        return bounds;
    }

    /**
     * Sets the latitude and longitude bounds of the grid.
     *
     * @param bounds The new bounds of the grid.
     */
    public void setBounds(Polygon bounds) {
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

    /**
     * Gets the color of the grid.
     *
     * @return The grids color.
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the color of the grid.
     *
     * @param color The grids color.
     */
    public void setColor(int color) {
        this.color = color;
    }
}

package mil.nga.mapcache.preferences;

import java.util.Observable;

/**
 * Contains information indicating if any grids have been selected to be displayed on the map.
 */
public class GridSettingsModel extends Observable {

    /**
     * The selected grid property.
     */
    public static String SELECTED_GRID_PROPERTY = "selectedGrid";

    /**
     * String used to contain a grid overlay setting within a string.
     */
    private static String gridOverlayTag = ":GridOverlay:";

    /**
     * The selected grid.
     */
    private Grid selectedGrid;

    /**
     * Gets the currently selected grid.
     *
     * @return The selected grid.
     */
    public Grid getSelectedGrid() {
        return selectedGrid;
    }

    /**
     * Sets a new selected grid.
     *
     * @param selectedGrid The new selected grid.
     */
    public void setSelectedGrid(Grid selectedGrid) {
        this.selectedGrid = selectedGrid;
        setChanged();
        notifyObservers(SELECTED_GRID_PROPERTY);
    }

    /**
     * Saves the values of this model into a parseable string.
     *
     * @return String represention of this model.
     */
    public String toString() {
        return gridOverlayTag + selectedGrid.toString() + gridOverlayTag;
    }

    /**
     * Populates this model from a previous toString of another GridSettingsModel class.
     *
     * @param gridString The parseable grid overlay string.
     * @return A new string with all the grid overlay string removed.
     */
    public String fromString(String gridString) {
        String[] splitString = gridString.split(gridOverlayTag);
        Grid grid = Grid.valueOf(splitString[1]);
        setSelectedGrid(grid);
        return splitString[splitString.length - 1];
    }
}

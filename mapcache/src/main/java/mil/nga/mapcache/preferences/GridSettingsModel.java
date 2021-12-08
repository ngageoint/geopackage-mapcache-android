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
    private GridType selectedGridType = GridType.NONE;

    /**
     * Gets the currently selected grid.
     *
     * @return The selected grid.
     */
    public GridType getSelectedGrid() {
        return selectedGridType;
    }

    /**
     * Sets a new selected grid.
     *
     * @param selectedGridType The new selected grid.
     */
    public void setSelectedGrid(GridType selectedGridType) {
        this.selectedGridType = selectedGridType;
        setChanged();
        notifyObservers(SELECTED_GRID_PROPERTY);
    }

    /**
     * Saves the values of this model into a parseable string.
     *
     * @return String represention of this model.
     */
    public String toString() {
        return gridOverlayTag + selectedGridType.toString() + gridOverlayTag;
    }

    /**
     * Populates this model from a previous toString of another GridSettingsModel class.
     *
     * @param gridString The parseable grid overlay string.
     * @return A new string with all the grid overlay string removed.
     */
    public String fromString(String gridString) {
        String[] splitString = gridString.split(gridOverlayTag);
        String newString = gridString;
        if (splitString.length > 1) {
            GridType gridType = GridType.valueOf(splitString[1]);
            setSelectedGrid(gridType);
            newString = splitString[splitString.length - 1];
        }

        return newString;
    }
}

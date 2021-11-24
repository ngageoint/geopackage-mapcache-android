package mil.nga.mapcache.preferences;

import java.util.Observable;

/**
 * Contains information indicating if any grids have been selected to be displayed on the map.
 */
public class GridSettingsModel extends Observable {

    /**
     * The selected grid property.
     */
    private static String SELECTED_GRID_PROPERTY = "selectedGrid";

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
}

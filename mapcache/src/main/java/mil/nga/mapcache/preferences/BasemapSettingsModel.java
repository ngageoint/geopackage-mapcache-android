package mil.nga.mapcache.preferences;

import java.util.Observable;

/**
 * Contains the available basemap servers and maintains which one the user has selected.
 */
public class BasemapSettingsModel extends Observable {

    /**
     * The selected basemap property.
     */
    public static String SELECTED_BASEMAP_PROP = "selectedBasemap";

    /**
     * The available basemap servers property.
     */
    public static String AVAILABLE_SERVERS_PROP = "availableServers";

    /**
     * The selected basemap.
     */
    private BasemapServerModel selectedBasemap;

    /**
     * The available basemap servers.
     */
    private BasemapServerModel[] availableServers;

    /**
     * Gets the selected basemap.
     *
     * @return The selected basemap or null if nothing has been selected.
     */
    public BasemapServerModel getSelectedBasemap() {
        return selectedBasemap;
    }

    /**
     * Sets the selected basemap.
     *
     * @param selectedBasemap The selected basemap.
     */
    public void setSelectedBasemap(BasemapServerModel selectedBasemap) {
        this.selectedBasemap = selectedBasemap;
        setChanged();
        notifyObservers(SELECTED_BASEMAP_PROP);
    }

    /**
     * Gets the available basemap servers.
     *
     * @return The available basemap servers.
     */
    public BasemapServerModel[] getAvailableServers() {
        return availableServers;
    }

    /**
     * Sets the available servers.
     *
     * @param availableServers The available servers.
     */
    public void setAvailableServers(BasemapServerModel[] availableServers) {
        this.availableServers = availableServers;
        setChanged();
        notifyObservers(AVAILABLE_SERVERS_PROP);
    }
}

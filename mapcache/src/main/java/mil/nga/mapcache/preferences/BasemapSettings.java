package mil.nga.mapcache.preferences;

import java.util.Observable;

import mil.nga.mapcache.layersprovider.LayerModel;

/**
 * Contains the available basemap servers and maintains which one the user has selected.
 */
public class BasemapSettings extends Observable {

    /**
     * String used to seperate different servers when writing the selected servers to a string.
     */
    private static String serverTag = ":Server:";

    /**
     * The selected basemap property.
     */
    public static String SELECTED_BASEMAP_PROP = "selectedBasemap";

    /**
     * The available basemap servers property.
     */
    public static String AVAILABLE_SERVERS_PROP = "availableServers";

    /**
     * The exclusive servers property.
     */
    public static String EXCLUSIVE_SERVERS_PROP = "exclusiveServers";

    /**
     * The selected basemap.
     */
    private BasemapServerModel[] selectedBasemap;

    /**
     * The map layers where only one can be selected.
     */
    private BasemapServerModel[] exclusiveServers;

    /**
     * The available basemap servers.
     */
    private BasemapServerModel[] availableServers;

    /**
     * Contains the selected grid overlay.
     */
    private GridSettingsModel gridOverlaySettings = new GridSettingsModel();

    /**
     * Gets the selected basemap.
     *
     * @return The selected basemap or null if nothing has been selected.
     */
    public BasemapServerModel[] getSelectedBasemap() {
        return selectedBasemap;
    }

    /**
     * Sets the selected basemap.
     *
     * @param selectedBasemap The selected basemap.
     */
    public void setSelectedBasemap(BasemapServerModel[] selectedBasemap) {
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

    /**
     * Gets the map layers where only one can be selected.
     *
     * @return The layers where only one can be selected.
     */
    public BasemapServerModel[] getExclusiveServers() {
        return exclusiveServers;
    }

    /**
     * Sets the map layers where only one can be selected.
     *
     * @param exclusiveServers The layers where only one can be selected.
     */
    public void setExclusiveServers(BasemapServerModel[] exclusiveServers) {
        this.exclusiveServers = exclusiveServers;
        setChanged();
        notifyObservers(EXCLUSIVE_SERVERS_PROP);
    }

    /**
     * Gets the grid overlay settings.
     *
     * @return Contains the selected grid overlay.
     */
    public GridSettingsModel getGridOverlaySettings() {
        return gridOverlaySettings;
    }

    /**
     * Saves the selected basemaps into a string.
     *
     * @return The selected basemaps in a string.
     */
    public String toString() {
        String basemapsString = gridOverlaySettings.toString();
        for (BasemapServerModel selectedServer : selectedBasemap) {
            basemapsString += serverTag + selectedServer.getServerUrl();
            if (selectedServer.getLayers().getSelectedLayers() != null) {
                for (LayerModel layer : selectedServer.getLayers().getSelectedLayers()) {
                    basemapsString += "," + layer.getName();
                }
            }
        }

        return basemapsString;
    }

    /**
     * Sets the selected servers and layers from the passed string.
     *
     * @param modelString The string of selected servers and layers.  This string must have been
     *                    constructed from a BasemapSettingsModel.toString call.
     */
    public void fromString(String modelString) {
        String withoutGrid = gridOverlaySettings.fromString(modelString);
        String[] serverStrings = withoutGrid.split(serverTag);
        BasemapServerModel[] servers = new BasemapServerModel[serverStrings.length - 1];
        int index = 0;
        for (String serverString : serverStrings) {
            if (index > 0) {
                BasemapServerModel server = new BasemapServerModel();
                String[] props = serverString.split(",");
                server.setServerUrl(props[0]);
                LayerModel[] selectedLayers = new LayerModel[props.length - 1];
                for (int i = 1; i < props.length; i++) {
                    LayerModel layer = new LayerModel();
                    layer.setName(props[i]);
                    selectedLayers[i - 1] = layer;
                }
                server.getLayers().setSelectedLayers(selectedLayers);
                servers[index - 1] = server;
            }
            index++;
        }

        setSelectedBasemap(servers);
    }
}

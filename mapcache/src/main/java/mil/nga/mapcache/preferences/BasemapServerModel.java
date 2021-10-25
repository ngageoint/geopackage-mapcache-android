package mil.nga.mapcache.preferences;

import java.util.Observable;

import mil.nga.mapcache.layersprovider.LayersModel;

/**
 * Represents a single server that can be used for basemaps.
 */
public class BasemapServerModel extends Observable {

    /**
     * The server url property.
     */
    public String SERVER_URL_PROP = "serverUrl";

    /**
     * The server url.
     */
    private String serverUrl;

    /**
     * The layers model.
     */
    private LayersModel layers = new LayersModel();

    /**
     * Constructor.
     */
    public BasemapServerModel() {
    }

    /**
     * Gets the server url.
     *
     * @return The server url.
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Sets the server url.
     *
     * @param serverUrl The server url.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the layers.
     *
     * @return The layers available from the server, if applicable.
     */
    public LayersModel getLayers() {
        return layers;
    }
}

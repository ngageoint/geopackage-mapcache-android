package mil.nga.mapcache.preferences;

import java.util.Observable;

import mil.nga.mapcache.layersprovider.LayersModel;

/**
 * Represents a single server that can be used for basemaps.
 */
public class BasemapServerModel extends Observable {

    /**
     * The name property.
     */
    public static String NAME_PROP = "name";

    /**
     * The server url property.
     */
    public static String SERVER_URL_PROP = "serverUrl";

    /**
     * The name of the server.
     */
    private String name;

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
     * Gets the name of the server.
     *
     * @return The name of the server.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the server.
     *
     * @param name The server name.
     */
    public void setName(String name) {
        this.name = name;
        setChanged();
        notifyObservers(NAME_PROP);
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
        notifyObservers(SERVER_URL_PROP);
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

package mil.nga.mapcache.layersprovider;

import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * The model that contains all the possible layers from a given server.
 */
public class LayersModel extends Observable {

    /**
     * The layers property.
     */
    public static String LAYERS_PROP = "layers";

    /**
     * The selected layer.
     */
    public static String SELECTED_LAYERS_PROP = "selectedLayers";

    /**
     * The image formats property.
     */
    public static String IMAGE_FORMATS_PROP = "imageFormats";

    /**
     * The authorization property.
     */
    public static String REQUEST_HEADERS_PROP = "requestHeaders";

    /**
     * The title property.
     */
    public static String TITLE_PROP = "title";

    /**
     * Contains the request headers used when retrieving the layers.
     */
    private Map<String, List<String>> requestHeaders = null;

    /**
     * The available image formats for the tiles.
     */
    private String[] imageFormats;

    /**
     * The available layers from a server.
     */
    private LayerModel[] layers;

    /**
     * The selected layer.
     */
    private LayerModel[] selectedLayers;

    /**
     * The title to display for the layers view.
     */
    private String title = "Choose your layer";

    /**
     * Gets the request headers used when retrieving the layers.
     *
     * @return The request headers, or null if one was not used.
     */
    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Sets the request headers used when retrieving the layers.
     *
     * @param requestHeaders The request header.
     */
    public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
        this.requestHeaders = requestHeaders;
        setChanged();
        notifyObservers(REQUEST_HEADERS_PROP);
    }

    /**
     * Gets the available layers from a server.
     *
     * @return The available layers from a server.
     */
    public LayerModel[] getLayers() {
        return layers;
    }

    /**
     * Sets the available layers from a server.
     *
     * @param layers The available layers.
     */
    public void setLayers(LayerModel[] layers) {
        this.layers = layers;
        setChanged();
        notifyObservers(LAYERS_PROP);
    }

    /**
     * Gets the selected layer.
     *
     * @return The selected layer.
     */
    public LayerModel[] getSelectedLayers() {
        return selectedLayers;
    }

    /**
     * Sets the selected layer.
     *
     * @param selectedLayer The selected layer.
     */
    public void setSelectedLayers(LayerModel[] selectedLayer) {
        this.selectedLayers = selectedLayer;
        setChanged();
        notifyObservers(SELECTED_LAYERS_PROP);
    }

    /**
     * Gets the available image formats for the layers tiles.
     *
     * @return The available image formats.
     */
    public String[] getImageFormats() {
        return imageFormats;
    }

    /**
     * Sets the available image formats for the layers tiles.
     *
     * @param imageFormats The available image formats.
     */
    public void setImageFormats(String[] imageFormats) {
        this.imageFormats = imageFormats;
        setChanged();
        notifyObservers(IMAGE_FORMATS_PROP);
    }

    /**
     * Gets the title.
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
        setChanged();
        notifyObservers(TITLE_PROP);
    }
}

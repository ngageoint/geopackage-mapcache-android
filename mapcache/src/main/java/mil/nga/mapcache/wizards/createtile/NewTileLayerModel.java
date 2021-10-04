package mil.nga.mapcache.wizards.createtile;

import java.util.Observable;

public class NewTileLayerModel extends Observable {

    /**
     * The geopackage name property.
     */
    public static String GEOPACKAGE_NAME_PROP = "geopackageName";

    /**
     * The layer name property.
     */
    public static String LAYER_NAME_PROP = "layerName";

    /**
     * The layer name error property.
     */
    public static String LAYER_NAME_ERROR_PROP = "layerNameError";

    /**
     * The url property.
     */
    public static String URL_PROP = "url";

    /**
     * The url error property.
     */
    public static String URL_ERROR_PROP = "urlError";

    /**
     * The validation message property.
     */
    public static String VALIDATION_MESSAGE_PROP = "validationMessage";

    /**
     * The saved urls property.
     */
    public static String SAVED_URLS_PROP = "savedUrls";

    /**
     * The layer name.
     */
    private String layerName;

    /**
     * Contains error message for layer name.
     */
    private String layerNameError = null;

    /**
     * The base url to the layer.
     */
    private String url;

    /**
     * Contains error message for url.
     */
    private String urlError = null;

    /**
     * The name of the geopackage.
     */
    private String geopackageName;

    /**
     * The list of saved urls.
     */
    private String[] savedUrls;

    /**
     * If anything is wrong with the inputs, this message will be populated.
     */
    private String validationMessage = "";

    /**
     * Gets the name of the layer.
     *
     * @return The name of the layer.
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * Sets the layer name.
     *
     * @param layerName The new name of the layer.
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
        setChanged();
        notifyObservers(LAYER_NAME_PROP);
    }

    /**
     * Gets any error messages about the layer name.
     *
     * @return The error message or empty string if no errors.
     */
    public String getLayerNameError() {
        return layerNameError;
    }

    /**
     * Sets the layer name error message.
     *
     * @param layerNameError The error message or empty string if no errors.
     */
    public void setLayerNameError(String layerNameError) {
        this.layerNameError = layerNameError;
        setChanged();
        notifyObservers(LAYER_NAME_ERROR_PROP);
    }

    /**
     * Gets the base url to the tile layer.
     *
     * @return The base url for the tile layer.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the base url for the tile layer.
     *
     * @param url The base url for the tile layer.
     */
    public void setUrl(String url) {
        this.url = url;
        setChanged();
        notifyObservers(URL_PROP);
    }

    /**
     * Gets any error messages about the url.
     *
     * @return The error message or empty string if no errors.
     */
    public String getUrlError() {
        return urlError;
    }

    /**
     * Sets the error message about the url.
     *
     * @param urlError The error message or empty string if no errors.
     */
    public void setUrlError(String urlError) {
        this.urlError = urlError;
        setChanged();
        notifyObservers(URL_ERROR_PROP);
    }

    /**
     * Gets the name of the geopackage.
     *
     * @return The name of the geopackage.
     */
    public String getGeopackageName() {
        return geopackageName;
    }

    /**
     * Get the saved urls.
     *
     * @return The list of saved urls.
     */
    public String[] getSavedUrls() {
        return savedUrls;
    }

    /**
     * Sets the saved urls.
     *
     * @param savedUrls The saved urls.
     */
    public void setSavedUrls(String[] savedUrls) {
        this.savedUrls = savedUrls;
        setChanged();
        notifyObservers(SAVED_URLS_PROP);
    }

    /**
     * Sets the name of the geopackage.
     *
     * @param geopackageName The new name of the geopackage.
     */
    public void setGeopackageName(String geopackageName) {
        this.geopackageName = geopackageName;
        setChanged();
        notifyObservers(GEOPACKAGE_NAME_PROP);
    }

    /**
     * Gets the validation message, if empty everything is valid.
     *
     * @return Empty or a validation message.
     */
    public String getValidationMessage() {
        return validationMessage;
    }

    /**
     * Sets the validation message, if empty everything is valid.
     *
     * @param validationMessage The validation message.
     */
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
        setChanged();
        notifyObservers(VALIDATION_MESSAGE_PROP);
    }
}

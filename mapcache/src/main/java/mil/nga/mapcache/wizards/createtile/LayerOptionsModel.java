package mil.nga.mapcache.wizards.createtile;

import java.util.Observable;

/**
 * Contains the various input values the user can change when creating a tile layer, such as min and
 * max zoom levels.
 */
public class LayerOptionsModel extends Observable {

    /**
     * The layer name property.
     */
    public static String LAYER_NAME_PROP = "layerName";

    /**
     * The url property.
     */
    public static String URL_PROP = "url";

    /**
     * The geopackage name property.
     */
    public static String GEOPACKAGE_NAME_PROP = "geopackageName";

    /**
     * The epsg property.
     */
    public static String EPSG_PROP = "epsg";

    /**
     * The tile format property.
     */
    public static String TILE_FORMAT_PROP = "tileFormat";

    /**
     * The minimum zoom level property.
     */
    public static String MIN_ZOOM_PROP = "minZoom";

    /**
     * The maximum zoom level property.
     */
    public static String MAX_ZOOM_PROP = "maxZoom";

    /**
     * The validation message property.
     */
    public static String VALIDATION_MESSAGE_PROP = "validationMessage";

    /**
     * The layer name.
     */
    private String layerName;

    /**
     * The base url to the layer.
     */
    private String url;

    /**
     * The name of the geopackage.
     */
    private String geopackageName;

    /**
     * Either 4326 or 3857.
     */
    private long epsg;

    /**
     * Either geopackage or standard.
     */
    private String tileFormat;

    /**
     * The minimum zoom level.
     */
    private int minZoom = 0;

    /**
     * The maximum zoom level.
     */
    private int maxZoom = 10;

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
     * Gets the name of the geopackage.
     *
     * @return The name of the geopackage.
     */
    public String getGeopackageName() {
        return geopackageName;
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
     * Gets the EPSG.
     *
     * @return The EPSG.
     */
    public long getEpsg() {
        return epsg;
    }

    /**
     * Sets the EPSG.
     *
     * @param epsg The new EPSG.
     */
    public void setEpsg(long epsg) {
        this.epsg = epsg;
        setChanged();
        notifyObservers(EPSG_PROP);
    }

    /**
     * Gets the tile format.
     *
     * @return The tile format.
     */
    public String getTileFormat() {
        return tileFormat;
    }

    /**
     * Sets the tile format.
     *
     * @param tileFormat The new tile format.
     */
    public void setTileFormat(String tileFormat) {
        this.tileFormat = tileFormat;
        setChanged();
        notifyObservers(TILE_FORMAT_PROP);
    }

    /**
     * Gets the minimum zoom level.
     *
     * @return The minimum zoom level.
     */
    public int getMinZoom() {
        return minZoom;
    }

    /**
     * Sets the minimum zoom level.
     *
     * @param minZoom The minimum zoom level.
     */
    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
        setChanged();
        notifyObservers(MIN_ZOOM_PROP);
    }

    /**
     * Gets the maximum zoom level.
     *
     * @return The maximum zoom level.
     */
    public int getMaxZoom() {
        return maxZoom;
    }

    /**
     * Sets the maximum zoom level.
     *
     * @param maxZoom The maximum zoom level.
     */
    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
        setChanged();
        notifyObservers(MAX_ZOOM_PROP);
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

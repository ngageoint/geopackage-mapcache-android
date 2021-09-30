package mil.nga.mapcache.wizards.createtile;

import java.util.Observable;

/**
 * Contains the various input values the user can change when creating a tile layer, such as min and
 * max zoom levels.
 */
public class LayerOptionsModel extends NewTileLayerModel {

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
}

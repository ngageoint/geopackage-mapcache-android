package mil.nga.mapcache.ogc.wms;

/**
 * The layer object within the getCapabilities xml
 */
public class Layer {

    /**
     * The layer's title.
     */
    private String title;

    /**
     * Gets the layer's title.
     *
     * @return The layer's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the layer's title.
     *
     * @param title The layer's title.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}

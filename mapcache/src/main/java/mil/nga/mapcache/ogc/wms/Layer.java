package mil.nga.mapcache.ogc.wms;

import java.util.ArrayList;
import java.util.List;

/**
 * The layer object within the getCapabilities xml
 */
public class Layer {

    /**
     * The layer's title.
     */
    private String title;

    /**
     * The child layers.
     */
    private List<Layer> layers = new ArrayList<>();

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

    /**
     * Gets the child layers.
     *
     * @return The child layers.
     */
    public List<Layer> getLayers() {
        return layers;
    }
}

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
    private String title = "";

    /**
     * The unique id of the layer.
     */
    private String name = "";

    /**
     * The different coordinate systems that are available for this layer.
     */
    private List<String> crs = new ArrayList<>();

    /**
     * The child layers.
     */
    private List<Layer> layers = new ArrayList<>();

    /**
     * Gets the unique id of the layer.
     *
     * @return The unique id of the layer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique id of the layer.
     *
     * @param name The unique id of the layer.
     */
    public void setName(String name) {
        this.name = name;
    }

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
     * Gets the different coordinate systems that are available for this layer.
     *
     * @return The different coordinate systems available for this layer.
     */
    public List<String> getCRS() {
        return crs;
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

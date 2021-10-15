package mil.nga.mapcache.ogc.wms;

import java.util.ArrayList;
import java.util.List;

/**
 * The capability object within a WMS getCapabilities xml
 */
public class Capability {

    /**
     * The layers for the wms service.
     */
    private List<Layer> layer = new ArrayList<>();

    /**
     * The request element.
     */
    private Request request = new Request();

    /**
     * Gets the layers for the wms service.ß
     *
     * @return The layers for the wms service with child layers.
     */
    public List<Layer> getLayer() {
        return layer;
    }

    /**
     * Gets the request element.
     *
     * @return The request element.
     */
    public Request getRequest() {
        return request;
    }
}

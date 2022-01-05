package mil.nga.mapcache.ogc.wms;

/**
 * The Request element within the GetCapabilities document.
 */
public class Request {

    /**
     * The GetMap element.
     */
    private GetMap getMap = new GetMap();

    /**
     * Gets the GetMap element within the request element.
     *
     * @return The GetMap element.
     */
    public GetMap getGetMap() {
        return getMap;
    }
}

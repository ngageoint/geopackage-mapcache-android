package mil.nga.mapcache.ogc.wms;

/**
 * Provides wms url to retrieve wms tiles.
 */
public class WMSUrlProvider {

    /**
     * Instance of this class.
     */
    private static WMSUrlProvider instance = new WMSUrlProvider();

    /**
     * Gets the instance of this class.
     * @return This class instance.
     */
    public static WMSUrlProvider getInstance() {
        return instance;
    }

    /**
     * Gets a wms url in order to retrieve a tile, but without the tiles bounding box.
     * @param baseUrl The wms server's base url.
     * @param layerName
     * @param format
     * @return
     */
    public String getUrlNoBoundingBox(String baseUrl, String layerName, String format) {
        return baseUrl + "?service=WMS&request=GetMap&layers="
                + layerName
                + "&styles=&format=" + format + "&transparent=true&width=256&"
                + "height=256&version=1.3.0";
    }

    /**
     * Gets the url including the crs and a bounding box containing {minLon}, {minLat}, {maxLon},
     * {maxLat} which can be replaced with the real bounding values using string replacement.
     * @param url The url to append the CRS and bounding box to.
     * @param epsg The coordinate system to use.
     * @return The url with the CRS and bounding box appeded to it.
     */
    public String getUrlBoundBoxCRS(String url, String epsg) {
        String newUrl = url + "&crs=EPSG:" + epsg;

        if (epsg.equals("3857")) {
            newUrl += "&bbox={minLon},{minLat},{maxLon},{maxLat}";
        } else {
            newUrl += "&bbox={minLat},{minLon},{maxLat},{maxLon}";
        }

        return newUrl;
    }

    /**
     * Private constructor making it a singleton.
     */
    private WMSUrlProvider() {
    }
}

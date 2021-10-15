package mil.nga.mapcache.ogc.wms;

import java.util.ArrayList;
import java.util.List;

/**
 * The GetMap element within a GetCapabilities document.
 */
public class GetMap {

    /**
     * The different image formats the tiles can be.
     */
    private List<String> format = new ArrayList<>();

    /**
     * Gets the different image formats the tiles can be.
     *
     * @return The different image formats to request for tiles.
     */
    public List<String> getFormat() {
        return format;
    }
}

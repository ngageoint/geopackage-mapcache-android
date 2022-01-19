package mil.nga.mapcache.view.map.overlays;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.ogc.wms.WMSUrlProvider;

/**
 * The url provider for wms tiles used by google maps.
 */
public class WMSTileProvider extends UrlTileProvider {
    /**
     * The size of the map in meters.
     */
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    /**
     * The starting meters for the longitude.
     */
    private static final double TILE_ORIGIN_LON = -20037508.34789244;

    /**
     * The starting meters for the latitude.
     */
    private static final double TILE_ORIGIN_LAT = 20037508.34789244;

    /**
     * The wms url to retrieve a single tile. Needs the bounding box values though still.
     */
    private String urlNeedsBoundingBox;

    /**
     * Constructor.
     *
     * @param baseUrl   The url to a wms server.
     * @param layerName The name of the layer.
     */
    public WMSTileProvider(String baseUrl, String layerName, String format) {
        super(256, 256);
        this.urlNeedsBoundingBox = WMSUrlProvider.getInstance().getUrlNoBoundingBox(
                baseUrl, layerName, format);
        this.urlNeedsBoundingBox = WMSUrlProvider.getInstance().getUrlBoundBoxCRS(urlNeedsBoundingBox, "3857");
    }

    /**
     * Checks if it can provide tiles for the base url.
     *
     * @param baseUrl The baseUrl to the server to test.
     * @return True if this can provide tiles for the baseUrl, false if it can't.
     */
    public static boolean canProvide(String baseUrl) {
        return baseUrl.toLowerCase(Locale.ROOT).contains("/wms");
    }

    @Nullable
    @Override
    public URL getTileUrl(int x, int y, int z) {
        BoundingBox bounds = getBoundingBox(x, y, z);

        String tileUrl = urlNeedsBoundingBox.replace("{minLat}",
                String.valueOf(bounds.getMinLatitude()));
        tileUrl = tileUrl.replace("{minLon}", String.valueOf(bounds.getMinLongitude()));
        tileUrl = tileUrl.replace("{maxLat}", String.valueOf(bounds.getMaxLatitude()));
        tileUrl = tileUrl.replace("{maxLon}", String.valueOf(bounds.getMaxLongitude()));

        URL theTileUrl = null;
        try {
            theTileUrl = new URL(tileUrl);
        } catch (MalformedURLException e) {
            Log.e(WMSTileProvider.class.getSimpleName(), "Invalid url", e);
        }

        return theTileUrl;
    }

    /**
     * Given x,y,z gets the bounding box of the tile.
     *
     * @param x    The x value of the google tile.
     * @param y    The y value of the google tile.
     * @param zoom The zoom level of the google tile.
     * @return The bounding box in 3857 for the tile.
     */
    private BoundingBox getBoundingBox(int x, int y, int zoom) {
        double gridSize = MAP_SIZE / Math.pow(2, zoom);
        double minLon = TILE_ORIGIN_LON + x * gridSize;
        double minLat = TILE_ORIGIN_LAT- (y + 1) * gridSize;
        double maxLon = TILE_ORIGIN_LON + (x + 1) * gridSize;
        double maxLat = TILE_ORIGIN_LAT - y * gridSize;

        BoundingBox bbox = new BoundingBox(minLon, minLat, maxLon, maxLat);

        return bbox;
    }
}

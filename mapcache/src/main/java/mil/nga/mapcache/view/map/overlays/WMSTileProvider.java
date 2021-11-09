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
     * The starting meters for the longitude and latitude.
     */
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};

    /**
     * The index of the longitude origin.
     */
    private static final int ORIG_X = 0;

    /**
     * The index of the latitude origin.
     */
    private static final int ORIG_Y = 1;

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
        return baseUrl.toLowerCase(Locale.ROOT).contains("wmsserver");
    }

    @Nullable
    @Override
    public URL getTileUrl(int x, int y, int z) {
        double numberOfGrids = Math.pow(2, z);
        double latDelta = 180 / numberOfGrids;
        double lonDelta = 360 / numberOfGrids;

        double maxLat = 90 - y * latDelta;
        double minLat = maxLat - latDelta;
        double minLon = -180 + x * lonDelta;
        double maxLon = minLon + lonDelta;

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
        double tileSize = MAP_SIZE / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
        double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
        double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

        BoundingBox bbox = new BoundingBox(minx, miny, maxx, maxy);

        return bbox;
    }
}

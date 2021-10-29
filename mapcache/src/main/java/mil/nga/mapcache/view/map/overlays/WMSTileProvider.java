package mil.nga.mapcache.view.map.overlays;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.mapcache.ogc.wms.WMSUrlProvider;

/**
 * The url provider for wms tiles used by google maps.
 */
public class WMSTileProvider extends UrlTileProvider {

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

    @Nullable
    @Override
    public URL getTileUrl(int x, int y, int z) {
        double numberOfGrids = Math.pow(2, z);
        double latDelta = 180 / numberOfGrids;
        double lonDelta = 360 / numberOfGrids;

        double minLat = 90 - y * latDelta;
        double maxLat = minLat + latDelta;
        double minLon = -180 + x * lonDelta;
        double maxLon = minLon + lonDelta;

        String tileUrl = urlNeedsBoundingBox.replace("{minLat}", String.valueOf(minLat));
        tileUrl = tileUrl.replace("{minLon}", String.valueOf(minLon));
        tileUrl = tileUrl.replace("{maxLat}", String.valueOf(maxLat));
        tileUrl = tileUrl.replace("{maxLon}", String.valueOf(maxLon));

        URL theTileUrl = null;
        try {
            theTileUrl = new URL(tileUrl);
        } catch (MalformedURLException e) {
            Log.e(WMSTileProvider.class.getSimpleName(), "Invalid url", e);
        }

        return theTileUrl;
    }
}

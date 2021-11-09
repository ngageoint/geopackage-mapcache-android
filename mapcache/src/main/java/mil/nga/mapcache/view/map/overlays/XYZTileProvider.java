package mil.nga.mapcache.view.map.overlays;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A UrlTileProvider for x,y,z tile servers.
 */
public class XYZTileProvider extends UrlTileProvider {

    private String xyzUrl;

    /**
     * Constructor.
     *
     * @param xyzUrl The url containing the {x}, {y}, {z} string value to be replaced.
     */
    public XYZTileProvider(String xyzUrl) {
        super(256, 256);
        this.xyzUrl = xyzUrl;
    }

    @Nullable
    @Override
    public URL getTileUrl(int x, int y, int z) {
        String replacedUrl = xyzUrl.replace("{x}", String.valueOf(x));
        replacedUrl = xyzUrl.replace("{y}", String.valueOf(y));
        replacedUrl = xyzUrl.replace("{z}", String.valueOf(z));

        URL url = null;
        try {
            url = new URL(replacedUrl);
        } catch (MalformedURLException e) {
            Log.e(XYZTileProvider.class.getSimpleName(), "Invalid url", e);
        }
        return url;
    }
}

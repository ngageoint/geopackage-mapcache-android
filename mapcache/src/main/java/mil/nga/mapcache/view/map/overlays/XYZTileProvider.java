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

    /**
     * The x string value to replace.
     */
    private static String xReplace = "{x}";

    /**
     * The y string value to replace.
     */
    private static String yReplace = "{y}";

    /**
     * The z string value to replace.
     */
    private static String zReplace = "{z}";

    /**
     * The url containing the {x}, {y}, {z} string value to be replaced.
     */
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

    /**
     * Checks if it can provide tiles for the base url.
     *
     * @param baseUrl The baseUrl to the server to test.
     * @return True if this can provide tiles for the baseUrl, false if it can't.
     */
    public static boolean canProvide(String baseUrl) {
        return baseUrl.contains(xReplace) && baseUrl.contains(yReplace) && baseUrl.contains(zReplace);
    }

    @Nullable
    @Override
    public URL getTileUrl(int x, int y, int z) {
        String replacedUrl = xyzUrl.replace(xReplace, String.valueOf(x));
        replacedUrl = replacedUrl.replace(yReplace, String.valueOf(y));
        replacedUrl = replacedUrl.replace(zReplace, String.valueOf(z));

        URL url = null;
        try {
            url = new URL(replacedUrl);
        } catch (MalformedURLException e) {
            Log.e(XYZTileProvider.class.getSimpleName(), "Invalid url", e);
        }
        return url;
    }
}

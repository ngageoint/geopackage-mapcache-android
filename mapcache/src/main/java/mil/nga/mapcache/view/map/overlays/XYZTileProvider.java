package mil.nga.mapcache.view.map.overlays;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.io.network.HttpClient;
import mil.nga.mapcache.io.network.IResponseHandler;
import mil.nga.mapcache.utils.HttpUtils;

/**
 * A UrlTileProvider for x,y,z tile servers.
 */
public class XYZTileProvider extends BaseTileProvider {

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
     * The tile image bytes.
     */
    private byte[] bytes = null;

    /**
     * Constructor.
     *
     * @param xyzUrl The url containing the {x}, {y}, {z} string value to be replaced.
     */
    public XYZTileProvider(String xyzUrl, Activity activity) {
        super(activity);
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
    protected String getTileUrl(int x, int y, int z) {
        String replacedUrl = xyzUrl.replace(xReplace, String.valueOf(x));
        replacedUrl = replacedUrl.replace(yReplace, String.valueOf(y));
        replacedUrl = replacedUrl.replace(zReplace, String.valueOf(z));

        return replacedUrl;
    }


}

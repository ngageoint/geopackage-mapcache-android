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
import mil.nga.mapcache.utils.HttpUtils;

/**
 * A UrlTileProvider for x,y,z tile servers.
 */
public class XYZTileProvider implements TileProvider {

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
     * Used to get app name and version.
     */
    private Activity activity;

    /**
     * The url containing the {x}, {y}, {z} string value to be replaced.
     */
    private String xyzUrl;

    /**
     * Constructor.
     *
     * @param xyzUrl The url containing the {x}, {y}, {z} string value to be replaced.
     */
    public XYZTileProvider(String xyzUrl, Activity activity) {
        this.xyzUrl = xyzUrl;
        this.activity = activity;
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
    public Tile getTile(int x, int y, int z) {
        Tile tile = null;

        URL url = getTileUrl(x, y, z);
        if (url != null) {
            byte[] image = downloadImage(url);
            if (image != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                tile = new Tile(bitmap.getWidth(), bitmap.getHeight(), image);
            }
        }

        return tile;
    }

    /**
     * Gets the tile's image url.
     *
     * @param x The x coordinate of the tile.
     * @param y The y coordinate of the tile.
     * @param z The z coordinate of the tile.
     * @return The tile's image url, or null if it doesn't have an image.
     */
    @Nullable
    private URL getTileUrl(int x, int y, int z) {
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

    /**
     * Downloads the image at the specified url.
     *
     * @param url The location of the image.
     * @return The image data.
     */
    private byte[] downloadImage(URL url) {
        HttpURLConnection connection = null;
        byte[] bytes = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            configureRequest(connection);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                String redirect = connection.getHeaderField("Location");
                connection.disconnect();
                url = new URL(redirect);
                connection = (HttpURLConnection) url.openConnection();
                configureRequest(connection);
                connection.connect();
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(XYZTileProvider.class.getSimpleName(), "Failed to download tile. URL: " + url);
            } else {
                InputStream geoPackageStream = connection.getInputStream();
                bytes = GeoPackageIOUtils.streamBytes(geoPackageStream);
            }

        } catch (IOException e) {
            Log.e(XYZTileProvider.class.getSimpleName(), e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return bytes;
    }

    /**
     * Adds the user agent to the http header.
     *
     * @param connection The connection to add the user agent to.
     */
    private void configureRequest(HttpURLConnection connection) {
        connection.addRequestProperty(
                HttpUtils.getInstance().getUserAgentKey(),
                HttpUtils.getInstance().getUserAgentValue(activity));
    }
}

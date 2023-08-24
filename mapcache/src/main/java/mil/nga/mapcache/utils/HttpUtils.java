package mil.nga.mapcache.utils;

import android.app.Activity;
import android.os.Build;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.mapcache.R;

/**
 * Contains various utilities when dealing with http connections.
 */
public class HttpUtils {

    /**
     * The instance of this class.
     */
    private static final HttpUtils instance = new HttpUtils();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static HttpUtils getInstance() {
        return instance;
    }

    /**
     * Gets the allow headers header key.
     *
     * @return The allow headers key.
     */
    public String getAllowHeadersKey() {
        return "Access-Control-Allow-Headers";
    }

    /**
     * Gets the http User Agent key.
     *
     * @return The http user agent key.
     */
    public String getUserAgentKey() {
        return "User-Agent";
    }

    /**
     * Gets the http basic authorization header key.
     *
     * @return The http basic authorization key.
     */
    public String getBasicAuthKey() {
        return "Authorization";
    }

    /**
     * Gets the cookie key.
     *
     * @return The header key for a cookie.
     */
    public String getCookieKey() {
        return "Cookie";
    }

    /**
     * Gets the location header key.
     *
     * @return The location header key.
     */
    public String getLocationKey() {
        return "Location";
    }

    /**
     * Gets the set cookie header key.
     *
     * @return The set cookie header key.
     */
    public String getSetCookieKey() {
        return "Set-Cookie";
    }

    /**
     * Gets the content encoding header key.
     *
     * @return The content encoding header key.
     */
    public String getContentEncodingKey() {
        return "Content-Encoding";
    }

    /**
     * Gets this apps user agent value.
     *
     * @param activity Used to get the apps name and version.
     * @return This apps user agent value.
     */
    public String getUserAgentValue(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.getString(R.string.app_name)
                    + " " + activity.getString(R.string.app_version)
                    + " Android " + Build.VERSION.RELEASE_OR_CODENAME;
        } else {
            return activity.getString(R.string.app_name)
                    + " " + activity.getString(R.string.app_version)
                    + " Android " + Build.VERSION.RELEASE;
        }
    }

    /**
     * Tests a connection to the given url
     *
     * @return True if we get a 200 response
     */
    public boolean isServerAvailable(String urlString){
        boolean connected = false;
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return connected;
//            throw new GeoPackageException("bad url");
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                connected = true;
            }
        } catch (Exception e){
            String exception = e.toString();
        }
        return connected;
    }

    /**
     * Private constructor.
     */
    private HttpUtils() {

    }
}

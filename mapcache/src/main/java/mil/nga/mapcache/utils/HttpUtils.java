package mil.nga.mapcache.utils;

import android.app.Activity;
import android.os.Build;

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
     * @return The instance of this class.
     */
    public static HttpUtils getInstance() {
        return instance;
    }

    /**
     * Gets the http User Agent key.
     * @return The http user agent key.
     */
    public String getUserAgentKey() {
        return "User-Agent";
    }

    /**
     * Gets this apps user agent value.
     * @param activity Used to get the apps name and version.
     * @return This apps user agent value.
     */
    public String getUserAgentValue(Activity activity) {
        return activity.getString(R.string.app_name)
                + " " + activity.getString(R.string.app_version)
                + " Android " + Build.VERSION.RELEASE_OR_CODENAME;
    }

    /**
     * Private constructor.
     */
    private HttpUtils() {

    }
}

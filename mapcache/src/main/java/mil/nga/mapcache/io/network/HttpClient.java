package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Makes http requests asynchronously.
 */
public class HttpClient {

    /**
     * The instance of this class.
     */
    private static final HttpClient instance = new HttpClient();

    /**
     * Gets the instance of this class.
     *
     * @return This class instance.
     */
    public static HttpClient getInstance() {
        return instance;
    }

    /**
     * Sends a http get to the specified url.
     *
     * @param url     The url to send a get request to.
     * @param handler The response handler, called when request is complete.
     * @param activity Used to get the app name and version for the user agent.
     */
    public void sendGet(String url, IResponseHandler handler, Activity activity) {
        HttpGetRequest request = new HttpGetRequest(url, handler, activity);
        ThreadUtils.getInstance().runBackground(request);
    }

    /**
     * Private constructor, keep it a singleton.
     */
    private HttpClient() {
    }
}

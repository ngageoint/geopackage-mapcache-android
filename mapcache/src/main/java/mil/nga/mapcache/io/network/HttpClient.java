package mil.nga.mapcache.io.network;

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
     */
    public void sendGet(String url, IResponseHandler handler) {
        HttpGetRequest request = new HttpGetRequest(url, handler);
        ThreadUtils.getInstance().runBackground(request);
    }

    /**
     * Private constructor, keep it a singleton.
     */
    private HttpClient() {
    }
}

package mil.nga.mapcache.io.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * An http get request class that performs any specified http get.
 */
public class HttpGetRequest implements Runnable {

    /**
     * The url of the get request.
     */
    private String urlString;

    /**
     * Object that is called when request is completed.
     */
    private IResponseHandler handler;

    /**
     * Constructs a new HttpGetRequest.
     *
     * @param url     The url of the get request.
     * @param handler Object this is called when request is completed.
     */
    public HttpGetRequest(String url, IResponseHandler handler) {
        this.urlString = url;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(this.urlString);
            URLConnection connection = url.openConnection();
            this.handler.handleResponse(connection.getInputStream(), 200);
        } catch (IOException e) {
            this.handler.handleException(e);
        }
    }
}

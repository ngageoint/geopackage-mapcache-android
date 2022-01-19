package mil.nga.mapcache.io.network;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import mil.nga.mapcache.utils.HttpUtils;

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
     * Used to get the app name and version for the user agent.
     */
    private Activity activity;

    /**
     * Constructs a new HttpGetRequest.
     *
     * @param url     The url of the get request.
     * @param handler Object this is called when request is completed.
     * @param activity Used to get the app name and version for the user agent.
     */
    public HttpGetRequest(String url, IResponseHandler handler, Activity activity) {
        this.urlString = url;
        this.handler = handler;
        this.activity = activity;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            configureRequest(connection);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                addBasicAuth(connection);
            }

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
                this.handler.handleResponse(null, responseCode);
            } else {
                InputStream stream = connection.getInputStream();
                this.handler.handleResponse(stream, responseCode);
            }

        } catch (IOException e) {
            this.handler.handleException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Adds basic auth to the connection.
     * @param connection The connection to add basic auth to.
     */
    private void addBasicAuth(HttpURLConnection connection) {
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

package mil.nga.mapcache.load;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.io.network.IResponseHandler;

/**
 * Handles the response from the specified url.
 */
public class WebViewResponseHandler implements IResponseHandler {

    /**
     * The bytes to return from createTile call.
     */
    private byte[] theBytes = null;

    /**
     * If an exception occurred trying to download the tile.
     */
    private IOException exception = null;

    /**
     * The current url we are using to download a tile image.
     */
    private final String currentUrl;

    /**
     * Debug logging flag.
     */
    private final boolean isDebug = true;

    /**
     * Constructor.
     *
     * @param url The url we are awaiting the response for.
     */
    public WebViewResponseHandler(String url) {
        this.currentUrl = url;
    }

    /**
     * Gets the data returned from the get call.
     *
     * @return The bytes of the response from the get call.
     */
    public byte[] getBytes() {
        if(isDebug) {
            Log.d(
                    WebViewResponseHandler.class.getSimpleName(),
                    "Getting byte for " + this.currentUrl);
        }
        return theBytes;
    }

    /**
     * Gets the exception if any from the response.
     *
     * @return The exception from the response or null if there wasn't one.
     */
    public IOException getException() {
        return exception;
    }

    @Override
    public void handleResponse(InputStream stream, int responseCode) {
        if(isDebug) {
            Log.d(
                    WebViewResponseHandler.class.getSimpleName(),
                    "Handling response for " + this.currentUrl);
        }
        try {
            if (stream != null) {
                theBytes = GeoPackageIOUtils.streamBytes(stream);
            } else {
                Log.w(
                        WebViewResponseHandler.class.getSimpleName(),
                        "Stream is null for url " + currentUrl);
            }
        } catch (IOException e) {
            exception = e;
        }

        synchronized (this) {
            notifyAll();
        }

        if(isDebug) {
            Log.d(
                    WebViewResponseHandler.class.getSimpleName(),
                    "Notified all for " + this.currentUrl);
        }
    }

    @Override
    public void handleException(IOException exception) {
        this.exception = exception;
        synchronized (this) {
            notifyAll();
        }
    }
}

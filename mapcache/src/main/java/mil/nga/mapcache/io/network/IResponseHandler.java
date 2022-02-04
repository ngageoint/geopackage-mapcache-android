package mil.nga.mapcache.io.network;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface to an object that wants to be notified of an http response.
 */
public interface IResponseHandler {

    /**
     * Handles the stream returned from an http request.
     *
     * @param stream The response from the server, or null if bad response from server.
     * @param responseCode The http response code from the server.
     */
    public void handleResponse(InputStream stream, int responseCode);

    /**
     * Handles the exception when trying to perform an http request.
     *
     * @param exception The exception to handle.
     */
    public void handleException(IOException exception);
}

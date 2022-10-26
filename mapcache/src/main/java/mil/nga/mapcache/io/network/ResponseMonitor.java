package mil.nga.mapcache.io.network;

import java.io.IOException;
import java.io.InputStream;

import mil.nga.mapcache.io.network.slowserver.SlowServerNotifier;

/**
 * Monitors for a download response and records the time.  Routes all handler calls to the original
 * handler.
 */
public class ResponseMonitor implements IResponseHandler {

    /**
     * The host waiting for download from.
     */
    private final String host;

    /**
     * The original response handler.
     */
    private final IResponseHandler handler;

    /**
     * If the server is slow, this will notify the user of that.
     */
    private final SlowServerNotifier notifier;

    /**
     * The start time of the download.
     */
    private long startTime;

    /**
     * Constructor.
     *
     * @param host     The host we are downloading from.
     * @param handler  The original handler to route calls too.
     * @param notifier If the server is slow, this will notify the user of that.
     */
    public ResponseMonitor(String host, IResponseHandler handler, SlowServerNotifier notifier) {
        this.host = host;
        this.handler = handler;
        this.notifier = notifier;
    }

    /**
     * Records the start time of now.
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void handleResponse(InputStream stream, int responseCode) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        notifier.responseTime(host, duration);
        handler.handleResponse(stream, responseCode);
    }

    @Override
    public void handleException(IOException exception) {
        handler.handleException(exception);
    }

    @Override
    public boolean notCancelled() {
        return handler.notCancelled();
    }
}

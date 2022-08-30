package mil.nga.mapcache.view.map.overlays;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.io.network.IResponseHandler;

/**
 * Response handler used for xyz tile downloads.
 */
public class TileResponseHandler implements IResponseHandler {

    /**
     * Debug logging flag.
     */
    private static final boolean isDebug = false;

    /**
     * The tile's image bytes.
     */
    private byte[] bytes = null;

    /**
     * Gets the tile's image bytes.
     *
     * @return The tile's image.
     */
    public byte[] getBytes() {
        if(isDebug) {
            Log.d(TileResponseHandler.class.getSimpleName(), "Getting bytes");
        }
        return bytes;
    }

    @Override
    public synchronized void handleResponse(InputStream stream, int responseCode) {
        if (isDebug) {
            Log.d(TileResponseHandler.class.getSimpleName(), "Handle response");
        }
        try {
            if (stream != null) {
                if (isDebug) {
                    Log.d(TileResponseHandler.class.getSimpleName(), "Streaming bytes");
                }
                bytes = GeoPackageIOUtils.streamBytes(stream);
                if (isDebug) {
                    Log.d(TileResponseHandler.class.getSimpleName(), "Streamed bytes");
                }
            } else {
                Log.e(TileResponseHandler.class.getSimpleName(), "Stream is null, response code " + responseCode);
            }
        } catch (IOException e) {
            Log.e(TileResponseHandler.class.getSimpleName(), e.getMessage(), e);
        } finally {
            notify();
            if (isDebug) {
                Log.d(TileResponseHandler.class.getSimpleName(), "Notified");
            }
        }
    }

    @Override
    public synchronized void handleException(IOException exception) {
        Log.e(TileResponseHandler.class.getSimpleName(), exception.getMessage(), exception);
        notify();
    }

}

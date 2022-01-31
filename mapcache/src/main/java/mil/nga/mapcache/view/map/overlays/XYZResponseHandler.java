package mil.nga.mapcache.view.map.overlays;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.mapcache.io.network.IResponseHandler;

/**
 * Response handler used for xyz tile downloads.
 */
public class XYZResponseHandler implements IResponseHandler {

    /**
     * The tile's image bytes.
     */
    private byte[] bytes = null;

    /**
     * Gets the tile's image bytes.
     * @return The tile's image.
     */
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public synchronized void handleResponse(InputStream stream, int responseCode) {
        try {
            if(stream != null) {
                bytes = GeoPackageIOUtils.streamBytes(stream);
            } else {
                Log.e(XYZResponseHandler.class.getSimpleName(), "Stream is null, response code " + responseCode);
            }
        } catch (IOException e) {
            Log.e(XYZResponseHandler.class.getSimpleName(), e.getMessage(), e);
        } finally {
            notify();
        }
    }

    @Override
    public synchronized void handleException(IOException exception) {
        Log.e(XYZResponseHandler.class.getSimpleName(), exception.getMessage(), exception);
        notify();
    }

}

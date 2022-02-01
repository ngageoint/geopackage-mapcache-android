package mil.nga.mapcache.view.map.overlays;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.mapcache.io.network.HttpClient;
import mil.nga.mapcache.io.network.HttpGetRequest;

/**
 * Abstract TileProvider that handles downloading the image and creating tiles but depends on
 * subclass providing correct urls based on tile's xyz coordinate.
 */
public abstract class BaseTileProvider implements TileProvider {

    /**
     * Used to turn debug logging on.
     */
    private static boolean isDebug = false;

    /**
     * The activity used to ask username and password if necessary.
     */
    private Activity activity;

    /**
     * Constructor.
     *
     * @param activity Used to ask username and password if necessary.
     */
    public BaseTileProvider(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public Tile getTile(int x, int y, int z) {
        Tile tile = null;

        String url = getTileUrl(x, y, z);
        if (isDebug)
            Log.d(BaseTileProvider.class.getSimpleName(), "Downloading image from " + url);
        if (url != null) {
            byte[] image = downloadImage(url);
            if (image != null) {
                if (isDebug)
                    Log.d(BaseTileProvider.class.getSimpleName(), url + " image bytes length " + image.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                if (isDebug)
                    Log.d(BaseTileProvider.class.getSimpleName(), url + " bitmap height and width " + bitmap.getHeight() + " " + bitmap.getWidth());
                tile = new Tile(bitmap.getWidth(), bitmap.getHeight(), image);
            }
        }

        return tile;
    }

    /**
     * Gets the tile's image url.
     *
     * @param x The x coordinate of the tile.
     * @param y The y coordinate of the tile.
     * @param z The z coordinate of the tile.
     * @return The tile's image url, or null if it doesn't have an image.
     */
    @Nullable
    protected abstract String getTileUrl(int x, int y, int z);

    /**
     * Downloads the image at the specified url.
     *
     * @param url The location of the image.
     * @return The image data.
     */
    private synchronized byte[] downloadImage(String url) {
        TileResponseHandler handler = new TileResponseHandler();
        synchronized (handler) {
            HttpClient.getInstance().sendGet(url.toString(), handler, this.activity);

            try {
                handler.wait();
            } catch (InterruptedException e) {
                Log.d(XYZTileProvider.class.getSimpleName(), e.getMessage(), e);
            }
        }
        return handler.getBytes();
    }
}

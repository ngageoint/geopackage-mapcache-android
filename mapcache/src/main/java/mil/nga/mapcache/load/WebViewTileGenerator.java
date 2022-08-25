package mil.nga.mapcache.load;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.locationtech.proj4j.units.Units;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.mapcache.io.network.HttpClient;
import mil.nga.mapcache.io.network.IResponseHandler;
import mil.nga.proj.Projection;

public class WebViewTileGenerator extends UrlTileGenerator implements IResponseHandler {

    /**
     * Tile URL
     */
    private String tileUrl;

    /**
     * True if the URL has x, y, or z variables
     */
    private boolean urlHasXYZ;

    /**
     * True if the URL has bounding box variables
     */
    private boolean urlHasBoundingBox;

    /**
     * TMS URL flag, when true x,y,z converted to TMS when requesting the tile
     */
    private boolean tms = false;

    private byte[] theBytes = null;

    private IOException exception = null;

    public WebViewTileGenerator(Context context, GeoPackage geoPackage, String tableName, String tileUrl, int minZoom, int maxZoom, BoundingBox boundingBox, Projection projection) {
        super(context, geoPackage, tableName, tileUrl, minZoom, maxZoom, boundingBox, projection);
        initialize(tileUrl);
    }

    @Override
    public boolean isTms() {
        return this.tms;
    }

    @Override
    public void setTms(boolean tms) {
        this.tms = tms;
    }

    /**
     * Initialize the tile URL
     *
     * @param tileUrl tile URL
     */
    private void initialize(String tileUrl) {
        try {
            this.tileUrl = URLDecoder.decode(tileUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new GeoPackageException("Failed to decode tile url: "
                    + tileUrl, e);
        }

        this.urlHasXYZ = hasXYZ(tileUrl);
        this.urlHasBoundingBox = hasBoundingBox(tileUrl);

        if (!this.urlHasXYZ && !this.urlHasBoundingBox) {
            throw new GeoPackageException(
                    "URL does not contain x,y,z or bounding box variables: "
                            + tileUrl);
        }
    }


    /**
     * Determine if the url has bounding box variables
     *
     * @param url The url to check for bounding box.
     * @return True if it has bounding box false if not.
     */
    private boolean hasBoundingBox(String url) {

        String replacedUrl = replaceBoundingBox(url, boundingBox);
        return !replacedUrl.equals(url);
    }


    /**
     * Replace x, y, and z in the url
     *
     * @param url The url to replace the x y z values.
     * @param z   The z value to replace it with.
     * @param x   The x value to replace it with.
     * @param y   The y value to replace it with.
     * @return The url with x,y,z populated with values.
     */
    private String replaceXYZ(String url, int z, long x, long y) {

        url = url.replace("{z}", String.valueOf(z));
        url = url.replace("{x}", String.valueOf(x));
        url = url.replace("{y}", String.valueOf(y));
        return url;
    }

    /**
     * Determine if the url has x, y, or z variables
     *
     * @param url The url to check if it contains x,y,z values.
     * @return True if the url is xyz, false if not.
     */
    private boolean hasXYZ(String url) {
        String replacedUrl = replaceXYZ(url, 0, 0, 0);
        return !replacedUrl.equals(url);
    }


    /**
     * Replace the bounding box coordinates in the url
     *
     * @param url The url to replace.
     * @param z   The z value.
     * @param x   The x value.
     * @param y   The y value.
     * @return The url containing the x, y, z values.
     */
    private String replaceBoundingBox(String url, int z, long x, long y) {

        BoundingBox boundingBox;

        if (projection.isUnit(Units.DEGREES)) {
            boundingBox = TileBoundingBoxUtils
                    .getProjectedBoundingBoxFromWGS84(projection, x, y, z);
        } else {
            boundingBox = TileBoundingBoxUtils
                    .getProjectedBoundingBox(projection, x, y, z);
        }

        url = replaceBoundingBox(url, boundingBox);

        return url;
    }

    /**
     * Replace the url parts with the bounding box
     *
     * @param url         The url to replace.
     * @param boundingBox The bounding box values to put within the url.
     * @return The url containing the bounding box values.
     */
    private String replaceBoundingBox(String url, BoundingBox boundingBox) {

        url = url.replace("{minLat}", String.valueOf(boundingBox.getMinLatitude()));
        url = url.replace("{maxLat}", String.valueOf(boundingBox.getMaxLatitude()));
        url = url.replace("{minLon}", String.valueOf(boundingBox.getMinLongitude()));
        url = url.replace("{maxLon}", String.valueOf(boundingBox.getMaxLongitude()));

        return url;
    }

    @Override
    protected byte[] createTile(int z, long x, long y) {

        String zoomUrl = tileUrl;

        // Replace x, y, and z
        if (urlHasXYZ) {
            long yRequest = y;

            // If TMS, flip the y value
            if (tms) {
                yRequest = TileBoundingBoxUtils.getYAsOppositeTileFormat(z,
                        (int) y);
            }

            zoomUrl = replaceXYZ(zoomUrl, z, x, yRequest);
        }

        // Replace bounding box
        if (urlHasBoundingBox) {
            zoomUrl = replaceBoundingBox(zoomUrl, z, x, y);
        }

        theBytes = null;
        exception = null;
        HttpClient.getInstance().sendGet(zoomUrl, this, (Activity) context);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                Log.d(WebViewTileGenerator.class.getSimpleName(), e.getMessage(), e);
            }
        }

        if (exception != null) {
            throw new GeoPackageException("Failed to download tile. URL: "
                    + zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, exception);
        }

        return theBytes;
    }

    @Override
    public void handleResponse(InputStream stream, int responseCode) {
        try {
            theBytes = GeoPackageIOUtils.streamBytes(stream);
        } catch (IOException e) {
            exception = e;
        }

        synchronized (this) {
            notifyAll();
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

package mil.nga.mapcache.utils;

/**
 * Contains utility methods that help with latitudes and longitudes.
 */
public class LatLonUtils {

    /**
     * The instance of this class.
     */
    private static LatLonUtils instance = new LatLonUtils();

    /**
     * Gets the instance of this class.
     * @return The instance of this class.
     */
    public static LatLonUtils getInstance() {
        return instance;
    }

    /**
     * Normalizes the longitude.
     *
     * @param lng The longitude to normalize.
     * @return The normalized longitude.
     */
    public double fixLongitude(double lng) {
        while (lng > 180.0) {
            lng -= 360.0;
        }
        while (lng < -180.0) {
            lng += 360.0;
        }
        return lng;
    }

    /**
     * Private constructor helps keep it a singleton.
     */
    private LatLonUtils() {
    }
}

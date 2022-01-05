package mil.nga.mapcache.utils;

import com.google.android.gms.maps.model.LatLng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.Comparator;

import mil.nga.geopackage.BoundingBox;

/**
 * Utility class for JTS polygons.
 */
public class PolygonUtils {

    /**
     * The instance.
     */
    private static PolygonUtils instance = new PolygonUtils();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static PolygonUtils getInstance() {
        return instance;
    }

    /**
     * Gets the boundary box of the specified polygon.
     *
     * @param polygon The polygon to get bounds for.
     * @return The bounding box of this polygon, [0] == lower left, [1] == lower right,
     * [2] == upper right, [3] == upper left
     */
    public LatLng[] getBounds(Polygon polygon) {
        Coordinate[] coords = polygon.getCoordinates();
        coords = new Coordinate[]{coords[0], coords[1], coords[2], coords[3]};
        Arrays.sort(coords,(coord1, coord2) -> compare(coord1, coord2));

        LatLng[] corners = new LatLng[4];

        if(coords[0].y < coords[1].y) {
            corners[0] = new LatLng(coords[0].y, coords[0].x);
            corners[3] = new LatLng(coords[1].y, coords[1].x);
        } else {
            corners[3] = new LatLng(coords[0].y, coords[0].x);
            corners[0] = new LatLng(coords[1].y, coords[1].x);
        }

        if(coords[2].y < coords[3].y) {
            corners[1] = new LatLng(coords[2].y, coords[2].x);
            corners[2] = new LatLng(coords[3].y, coords[3].x);
        } else {
            corners[2] = new LatLng(coords[2].y, coords[2].x);
            corners[1] = new LatLng(coords[3].y, coords[3].x);
        }

        return corners;
    }

    /**
     * Private constructor ensuring this class is a singleton.
     */
    private PolygonUtils() {

    }

    /**
     * Compares the longitudes of two coordinates.
     * @param coord1 The first to compare.
     * @param coord2 The second to compare.
     * @return Double.compare of the two coords longitudes.
     */
    private int compare(Coordinate coord1, Coordinate coord2) {
        return Double.compare(coord1.x, coord2.x);
    }
}

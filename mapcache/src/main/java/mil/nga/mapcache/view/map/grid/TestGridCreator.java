package mil.nga.mapcache.view.map.grid;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import mil.nga.geopackage.BoundingBox;

/**
 * Creates a simple grid showing four quadrants on the screen.  Used for testing.
 */
public class TestGridCreator extends GridCreator {

    /**
     * Constructor
     *
     * @param model    This will contain all of the grid object to display on the map.
     * @param map      The map to add the overlays to.
     * @param activity Used to run back on the UI thread.
     */
    public TestGridCreator(GridModel model, GoogleMap map, Activity activity) {
        super(model, map, activity);
    }

    @Override
    protected Grid[] createGrid(BoundingBox bounds, int zoom) {

        double minLat = bounds.getMinLatitude();
        double minLon = bounds.getMinLongitude();
        double maxLat = bounds.getMaxLatitude();
        double maxLon = bounds.getMaxLongitude();
        double centLat = (minLat + maxLat) / 2.;
        double centLon = (minLon + maxLon) / 2.;

        Grid lowerLeft = new Grid();
        GeometryFactory factory = new GeometryFactory();
        lowerLeft.setBounds(factory.createPolygon(new Coordinate[]{new Coordinate(minLon, minLat),
                new Coordinate(centLon, minLat),
                new Coordinate(centLon, centLat),
                new Coordinate(minLon, centLat),
                new Coordinate(minLon, minLat)}));
        lowerLeft.setText("Lower Left " + zoom);

        Grid lowerRight = new Grid();
        lowerRight.setBounds(factory.createPolygon(new Coordinate[]{new Coordinate(centLon, minLat),
                new Coordinate(maxLon, minLat),
                new Coordinate(maxLon, centLat),
                new Coordinate(centLon, centLat),
                new Coordinate(centLon, minLat)}));
        lowerRight.setText("Lower Right " + zoom);

        Grid upperRight = new Grid();
        upperRight.setBounds(factory.createPolygon(new Coordinate[]{new Coordinate(centLon, centLat),
                new Coordinate(maxLon, centLat),
                new Coordinate(maxLon, maxLat),
                new Coordinate(centLon, maxLat),
                new Coordinate(centLon, centLat)}));
        upperRight.setText("Upper Right " + zoom);

        Grid upperLeft = new Grid();
        upperLeft.setBounds(factory.createPolygon(new Coordinate[]{new Coordinate(minLon, centLat),
                new Coordinate(centLon, centLat),
                new Coordinate(centLon, maxLat),
                new Coordinate(minLon, maxLat),
                new Coordinate(minLon, centLat)}));
        upperLeft.setText("Upper Left " + zoom);

        return new Grid[]{lowerLeft, lowerRight, upperRight, upperLeft};
    }
}

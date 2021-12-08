package mil.nga.mapcache.view.map.grid;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

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
        lowerLeft.setBounds(new BoundingBox(minLon, minLat, centLon, centLat));
        lowerLeft.setText("Lower Left " + zoom);

        Grid lowerRight = new Grid();
        lowerRight.setBounds(new BoundingBox(centLon, minLat, maxLon, centLat));
        lowerRight.setText("Lower Right " + zoom);

        Grid upperRight = new Grid();
        upperRight.setBounds(new BoundingBox(centLon, centLat, maxLon, maxLat));
        upperRight.setText("Upper Right " + zoom);

        Grid upperLeft = new Grid();
        upperLeft.setBounds(new BoundingBox(minLon, centLat, centLon, maxLat));
        upperLeft.setText("Upper Left " + zoom);

        return new Grid[]{lowerLeft, lowerRight, upperRight, upperLeft};
    }
}

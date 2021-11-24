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
    protected Grid[] createGrid(BoundingBox bounds) {

        double minLat = bounds.getMinLatitude();
        double minLon = bounds.getMinLongitude();
        double maxLat = bounds.getMaxLatitude();
        double maxLon = bounds.getMaxLongitude();
        double centLat = (minLat + maxLat) / 2.;
        double centLon = (minLon + maxLon) / 2.;

        Grid lowerLeft = new Grid();
        lowerLeft.setBounds(new BoundingBox(minLon, minLat, centLon, centLat));
        lowerLeft.setText("Lower Left");

        Grid lowerRight = new Grid();
        lowerLeft.setBounds(new BoundingBox(centLon, minLat, maxLon, centLon));
        lowerLeft.setText("Lower Right");

        Grid upperRight = new Grid();
        lowerLeft.setBounds(new BoundingBox(centLon, centLat, maxLon, maxLat));
        lowerLeft.setText("Upper Right");

        Grid upperLeft = new Grid();
        lowerLeft.setBounds(new BoundingBox(minLon, centLat, centLon, maxLat));
        lowerLeft.setText("Upper Left");

        return new Grid[0];
    }
}

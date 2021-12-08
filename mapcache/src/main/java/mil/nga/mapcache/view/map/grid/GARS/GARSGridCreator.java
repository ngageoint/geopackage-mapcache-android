package mil.nga.mapcache.view.map.grid.GARS;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.view.map.grid.Grid;
import mil.nga.mapcache.view.map.grid.GridCreator;
import mil.nga.mapcache.view.map.grid.GridModel;
import mil.nga.mapcache.view.map.grid.GridOptions;

/**
 * Given a bounding box this class creates a GARs grid for the visible area.
 */
public class GARSGridCreator extends GridCreator {

    /**
     * Used to calculate the GARs grid label.
     */
    private GARSCalculator labelCalculator = new GARSCalculator();

    /**
     * The grid options for a twenty degree grid size.
     */
    private GridOptions twentyDegree = new GridOptions(0, 2, true);

    /**
     * The grid options for a ten degree grid size.
     */
    private GridOptions tenDegree = new GridOptions(3, 4, true);

    /**
     * The grid options for a five degree grid size.
     */
    private GridOptions fiveDegree = new GridOptions(5, 6, true);

    /**
     * The grid options for a thirty minute grid size.
     */
    private GridOptions thirtyMinute = new GridOptions(7, 8, true);

    /**
     * The grid options for a fifteen minute grid size.
     */
    private GridOptions fifteenMinutes = new GridOptions(9, 9, true);

    /**
     * The grid options for a five minute grid size.
     */
    private GridOptions fiveMinutes = new GridOptions(10, 20, true);

    /**
     * Constructor
     *
     * @param model    This will contain all of the grid object to display on the map.
     * @param map      The map to add the overlays to.
     * @param activity Used to run back on the UI thread.
     */
    public GARSGridCreator(GridModel model, GoogleMap map, Activity activity) {
        super(model, map, activity);
    }

    @Override
    protected Grid[] createGrid(BoundingBox bounds, int zoom) {
        List<Grid> blocks = new ArrayList<>();
        if (zoom >= fiveMinutes.getMinZoom() && zoom <= fiveMinutes.getMaxZoom()) {
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 0.25 / 3.0), 0.25 / 3.0, true, 7));
        }
        if (zoom >= fifteenMinutes.getMinZoom() && zoom <= fifteenMinutes.getMaxZoom()) {
            //quadBlocks are .25 degree squares, 4 per bigBlock
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 0.25), 0.25, true, 6));
        }
        if (zoom >= thirtyMinute.getMinZoom() && zoom <= thirtyMinute.getMaxZoom()) {
            //Big blocks are 0.5x0.5 lat/lng squares
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 0.5), 0.5, true, 5));
        }

        if (zoom >= fiveDegree.getMinZoom() && zoom <= fiveDegree.getMaxZoom()) {
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 5.0), 5.0, false, -1));
        }
        if (zoom >= tenDegree.getMinZoom() && zoom <= tenDegree.getMaxZoom()) {
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 10.0), 10.0, false, -1));
        }
        if (zoom >= twentyDegree.getMinZoom() && zoom <= twentyDegree.getMaxZoom()) {
            blocks.addAll(this.calculateBlocks(roundBounds(bounds, 20.0), 20.0, false, -1));
        }

        Grid[] grids = blocks.toArray(new Grid[0]);
        return grids;
    }

    /**
     * Calculates the grids based on the increment and bounding box.
     *
     * @param bounds      The bounding box for the grids.
     * @param increment   The increment per grid.
     * @param labelGARs   True if we should use the GARs notation for grid labels otherwise it will use
     *                    a generic latitude longitude label.
     * @param labelLength The max length for the label.
     * @return The grids within the bounding box.
     */
    private List<Grid> calculateBlocks(BoundingBox bounds, double increment, boolean labelGARs, int labelLength) {
        List<Grid> grids = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        for (double bw = bounds.getMinLongitude(); bw < bounds.getMaxLongitude(); bw += increment) {
            for (double bs = bounds.getMinLatitude(); bs < bounds.getMaxLatitude(); bs += increment) {
                double be = bw + increment;
                double bn = bs + increment;
                Polygon rect = factory.createPolygon(new Coordinate[]{
                        new Coordinate(bw, bs),
                        new Coordinate(be, bs),
                        new Coordinate(be, bn),
                        new Coordinate(bw, bn),
                        new Coordinate(bw, bs)
                });

                String gridLabel = "";
                if (labelGARs) {
                    gridLabel = labelCalculator.latLng2GARS(bs + increment / 2.0, bw + increment / 2.0);
                    gridLabel = gridLabel.substring(0, labelLength);
                } else {
                    gridLabel = labelCalculator.latLng2Name(bs, bw, increment);
                }

                Grid grid = new Grid();
                grid.setBounds(rect);
                grid.setText(gridLabel);
                grids.add(grid);
            }
        }
        return grids;
    }

    /**
     * Rounds the bounds based on increment.
     *
     * @param bounds    The bounds to round.
     * @param increment The increment.
     * @return The rounded bounds.
     */
    private BoundingBox roundBounds(BoundingBox bounds, double increment) {
        double swLat = bounds.getMinLatitude();
        double swLng = bounds.getMinLongitude();
        double neLat = bounds.getMaxLatitude();
        double neLng = bounds.getMaxLongitude();
        if (swLat > 0) {
            swLat -= swLat % increment;
        } else if (swLat < 0) {
            swLat -= increment + (swLat % increment);
        }//else it's 0, good enough
        if (swLng > 0) {
            swLng -= swLng % increment;
        } else if (swLng < 0) {
            swLng -= increment + (swLng % increment);
        } //else it's 0, good enough

        if (neLat > 0) {
            neLat += increment - (neLat % increment);
        } else if (neLat < 0) {
            neLat -= neLat % increment;
        } //else it's 0, good enough
        if (neLng > 0) {
            neLng += increment - (neLng % increment);
        } else if (neLng < 0) {
            neLng -= neLng % increment;
        } //else it's 0, good enough

        swLat = Math.max(swLat, -80.0);
        neLat = Math.min(neLat, 80.0);

        return new BoundingBox(swLng, swLat, neLng, neLat);
    }
}

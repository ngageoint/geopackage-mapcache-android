package mil.nga.mapcache.view.map.grid;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.utils.PolygonUtils;

/**
 * Creates the polylines that can be added to the map.
 */
public class PolylineGridCreator {

    /**
     * The grid model to update.
     */
    private GridModel gridModel;

    /**
     * Constructor.
     *
     * @param gridModel The grid model to update.
     */
    public PolylineGridCreator(GridModel gridModel) {
        this.gridModel = gridModel;
    }

    /**
     * Creates the polylines based on the grids within the model.
     */
    public void createPolylines() {
        List<PolylineOptions> polylines = new ArrayList<>();
        //Reduce the number of polylines on map
        Map<LatLng, Set<LatLng>> existingPolylines = new HashMap<>();

        for (Grid grid : gridModel.getGrids()) {
            Polygon poly = grid.getBounds();
            LatLng[] corners = PolygonUtils.getInstance().getBounds(poly);
            LatLng lowerLeft = corners[0];
            LatLng lowerRight = corners[1];
            LatLng upperRight = corners[2];
            LatLng upperLeft = corners[3];

            PolylineOptions polyline = new PolylineOptions();

            if (!existingPolylines.containsKey(lowerLeft) || !existingPolylines.get(lowerLeft).contains(lowerRight)) {
                polyline.add(lowerLeft, lowerRight);
                polyline.width(5);
                polyline.color(grid.getColor());
                polyline.geodesic(false);
                polylines.add(polyline);

                if (!existingPolylines.containsKey(lowerLeft)) {
                    existingPolylines.put(lowerLeft, new HashSet<>());
                }

                existingPolylines.get(lowerLeft).add(lowerRight);
            }

            if (!existingPolylines.containsKey(lowerRight) || !existingPolylines.get(lowerRight).contains(upperRight)) {
                polyline = new PolylineOptions();
                polyline.add(lowerRight, upperRight);
                polyline.width(5);
                polyline.color(grid.getColor());
                polyline.geodesic(false);
                polylines.add(polyline);

                if (!existingPolylines.containsKey(lowerRight)) {
                    existingPolylines.put(lowerRight, new HashSet<>());
                }

                existingPolylines.get(lowerRight).add(lowerRight);
            }

            if (!existingPolylines.containsKey(upperLeft) || !existingPolylines.get(upperLeft).contains(upperRight)) {
                polyline = new PolylineOptions();
                polyline.add(upperRight, upperLeft);
                polyline.width(5);
                polyline.color(grid.getColor());
                polyline.geodesic(false);
                polylines.add(polyline);

                if (!existingPolylines.containsKey(upperLeft)) {
                    existingPolylines.put(upperLeft, new HashSet<>());
                }

                existingPolylines.get(upperLeft).add(upperRight);
            }

            if (!existingPolylines.containsKey(lowerLeft) || !existingPolylines.get(lowerLeft).contains(upperLeft)) {
                polyline = new PolylineOptions();
                polyline.add(upperLeft, lowerLeft);
                polyline.width(5);
                polyline.color(grid.getColor());
                polyline.geodesic(false);
                polylines.add(polyline);

                if (!existingPolylines.containsKey(lowerLeft)) {
                    existingPolylines.put(lowerLeft, new HashSet<>());
                }

                existingPolylines.get(lowerLeft).add(upperLeft);
            }
        }

        PolylineOptions[] newLines = polylines.toArray(new PolylineOptions[0]);
        this.gridModel.setPolylines(newLines);
    }
}

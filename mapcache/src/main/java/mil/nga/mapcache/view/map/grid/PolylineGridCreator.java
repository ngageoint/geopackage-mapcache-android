package mil.nga.mapcache.view.map.grid;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;

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

        for (Grid grid : gridModel.getGrids()) {
            BoundingBox box = grid.getBounds();
            LatLng lowerLeft = new LatLng(box.getMinLatitude(), box.getMinLongitude());
            LatLng lowerRight = new LatLng(box.getMinLatitude(), box.getMaxLongitude());
            LatLng upperRight = new LatLng(box.getMaxLatitude(), box.getMaxLongitude());
            LatLng upperLeft = new LatLng(box.getMaxLatitude(), box.getMinLongitude());

            PolylineOptions polyline = new PolylineOptions();
            polyline.add(lowerLeft, lowerRight, upperRight, upperLeft, lowerLeft);
            polyline.width(5);
            polyline.color(Color.RED);
            polyline.geodesic(false);
            polylines.add(polyline);
        }

        PolylineOptions[] newLines = polylines.toArray(new PolylineOptions[0]);
        this.gridModel.setPolylines(newLines);
    }
}

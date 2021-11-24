package mil.nga.mapcache.view.map.grid;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Abstract class to an object that creates grid overlays to display on top of the map.
 */
public abstract class GridCreator {

    /**
     * Contains all the grid objects to display on map.
     */
    private GridModel gridModel;

    /**
     * The map to add the overlays to.
     */
    private GoogleMap map;

    /**
     * Used to run back on the UI thread.
     */
    private Activity activity;

    /**
     * The polylines that are currently displayed on the map.
     */
    private List<Polyline> mapPolylines = new ArrayList<>();

    /**
     * Creates the polylines that can be added to the map.
     */
    private PolylineGridCreator lineCreator;

    /**
     * The observer on the model.
     */
    private Observer observer = (observer, o) -> modelUpdate(observer, o);

    /**
     * Indicates if this creator has been destroyed so no more map updates occur.
     */
    private boolean isDestroyed = false;

    /**
     * Constructor
     *
     * @param model    This will contain all of the grid object to display on the map.
     * @param map      The map to add the overlays to.
     * @param activity Used to run back on the UI thread.
     */
    public GridCreator(GridModel model, GoogleMap map, Activity activity) {
        this.gridModel = model;
        this.map = map;
        this.activity = activity;
        this.lineCreator = new PolylineGridCreator(gridModel);
        this.gridModel.addObserver(observer);
    }

    public void destroy() {
        isDestroyed = true;
        this.gridModel.deleteObserver(observer);
        removeGrids();
    }

    /**
     * Creates a grid overlay in the background and displays it on the map.
     *
     * @param bounds The bounds of the overlay to create.
     */
    public void createGridForMap(BoundingBox bounds) {
        ThreadUtils.getInstance().runBackground(() -> {
            Grid[] grids = createGrid(bounds);
            this.gridModel.setGrids(grids);

            // Now spawn the label maker and polyline creator
            ThreadUtils.getInstance().runBackground(() -> {
                createLabels();
            });
            createPolylines();
        });
    }

    /**
     * Removes the grids from the map.
     */
    private void removeGrids() {
        removePolylines();
    }

    /**
     * Removes the polylines from the map.
     */
    private void removePolylines() {
        for (Polyline polyline : mapPolylines) {
            polyline.remove();
        }
        mapPolylines.clear();
    }

    /**
     * Creates the grid object based on whatever grid this class is.
     *
     * @param bounds The bounding box of the grid.
     * @return The grids.
     */
    protected abstract Grid[] createGrid(BoundingBox bounds);

    /**
     * Creates the polylines based on what is populated for the grids within the model.
     */
    private void createPolylines() {
        lineCreator.createPolylines();
    }

    /**
     * Creates the labels based on what is populated for the grids within the model.
     */
    private void createLabels() {

    }

    /**
     * Called any time the model is updated.
     *
     * @param observer The gridModel
     * @param o        The property that changed
     */
    private void modelUpdate(Observable observer, Object o) {
        if (GridModel.POLYLINES_PROPERTY.equals(o)) {
            this.activity.runOnUiThread(() -> updatePolylines());
        } else if (GridModel.LABELS_PROPERTY.equals(o)) {
            this.activity.runOnUiThread(() -> updateLabels());
        }
    }

    /**
     * Updates the polylines on the map.
     */
    private void updatePolylines() {
        removePolylines();
        if(!isDestroyed) {
            for (PolylineOptions polylineOptions : this.gridModel.getPolylines()) {
                mapPolylines.add(map.addPolyline(polylineOptions));
            }
        }
    }

    /**
     * Updates the labels on the map.
     */
    private void updateLabels() {

    }
}

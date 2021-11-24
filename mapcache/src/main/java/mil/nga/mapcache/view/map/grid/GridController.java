package mil.nga.mapcache.view.map.grid;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.preferences.GridType;

/**
 * Manages the grids that are being displayed on the map.
 */
public class GridController {

    /**
     * The map that we display grid overlays on.
     */
    private GoogleMap map;

    /**
     * The current grid creator.
     */
    private GridCreator gridCreator = null;

    /**
     * Contains the grids to display on map.
     */
    private GridModel gridModel = new GridModel();

    /**
     * Used to run on the UI thread.
     */
    private Activity activity;

    /**
     * Constructor.
     *
     * @param map      The map that we display grid overlays on.
     * @param activity Used to run on the UI thread.
     * @param gridType The type of grid to display on map.
     */
    public GridController(GoogleMap map, Activity activity, GridType gridType) {
        this.map = map;
        this.activity = activity;
        gridChanged(gridType);
    }

    /**
     * Sets a new grid overlay on the map.
     *
     * @param gridType The new type of grid to display on map.
     */
    public void gridChanged(GridType gridType) {
        if (gridType == GridType.NONE) {
            this.map.setOnCameraIdleListener(null);
        } else {
            gridCreator = new TestGridCreator(gridModel, map, activity);
            this.map.setOnCameraIdleListener(() -> onCameraIdle());
        }
    }

    /**
     * Called when the globes camera stops moving.
     */
    private void onCameraIdle() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        gridCreator.createGridForMap(new BoundingBox(bounds.northeast.longitude, bounds.southwest.latitude,
                bounds.southwest.longitude, bounds.northeast.latitude));
    }
}

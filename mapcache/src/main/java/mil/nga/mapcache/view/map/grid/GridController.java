package mil.nga.mapcache.view.map.grid;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.preferences.GridType;
import mil.nga.mapcache.view.map.grid.GARS.GARSGridCreator;
import mil.nga.mapcache.view.map.grid.mgrs.MGRSGridCreator;

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
     * The coordinates text view.
     */
    private TextView coordsTextView;

    /**
     * Constructor.
     *
     * @param map            The map that we display grid overlays on.
     * @param activity       Used to run on the UI thread.
     * @param gridType       The type of grid to display on map.
     * @param coordsTextView The text view to display current center of screen coordinates.
     */
    public GridController(GoogleMap map, Activity activity, GridType gridType, TextView coordsTextView) {
        this.map = map;
        this.activity = activity;
        this.coordsTextView = coordsTextView;
        gridChanged(gridType);
    }

    /**
     * Sets a new grid overlay on the map.
     *
     * @param gridType The new type of grid to display on map.
     */
    public void gridChanged(GridType gridType) {
        if (gridType == GridType.NONE && gridCreator != null) {
            this.map.setOnCameraIdleListener(null);
            this.map.setOnCameraMoveListener(null);
            this.gridCreator.destroy();
            this.gridCreator = null;
            this.coordsTextView.setVisibility(View.GONE);
        } else if (gridType != GridType.NONE) {
            if (gridCreator != null) {
                gridCreator.destroy();
            }
            gridCreator = newCreator(gridType);
            this.map.setOnCameraIdleListener(() -> onCameraIdle());
            this.map.setOnCameraMoveListener(() -> onCameraMoved());
            onCameraIdle();
            onCameraMoved();
        }
    }

    /**
     * Creates a new grid creator based on the specified grid type.
     *
     * @param gridType The type of grid to create.
     * @return The grid creator.
     */
    private GridCreator newCreator(GridType gridType) {
        GridCreator gridCreator = null;
        if (gridType == GridType.GARS) {
            gridCreator = new GARSGridCreator(gridModel, map, activity);
        } else if (gridType == GridType.MGRS) {
            gridCreator = new MGRSGridCreator(gridModel, map, activity);
        }

        return gridCreator;
    }

    /**
     * Called when the globes camera stops moving.
     */
    private void onCameraIdle() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        gridCreator.createGridForMap(new BoundingBox(bounds.southwest.longitude, bounds.southwest.latitude,
                bounds.northeast.longitude, bounds.northeast.latitude));
    }

    /**
     * Called when the globes camera moves.
     */
    private void onCameraMoved() {
        LatLng center = map.getCameraPosition().target;
        String coordinate = gridCreator.coordinatesAt(center);
        if (coordinate != null) {
            coordsTextView.setVisibility(View.VISIBLE);
            coordsTextView.setText(coordinate);
        } else {
            coordsTextView.setVisibility(View.GONE);
        }
    }
}

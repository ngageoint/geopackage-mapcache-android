package mil.nga.mapcache.view.map.grid;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.preferences.GridType;
import mil.nga.mapcache.view.map.grid.GARS.GARSGridCreator;
import mil.nga.mgrs.features.Point;
import mil.nga.mgrs.tile.MGRSTileProvider;

/**
 * Manages the grids that are being displayed on the map.
 */
public class GridController {

    /**
     * The map that we display grid overlays on.
     */
    private GoogleMap map;

    /**
     * The current grid type
     */
    private GridType gridType = GridType.NONE;

    /**
     * The current grid tile overlay
     */
    private TileOverlay tileOverlay = null;

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
    private TextView coordTextView;

    /**
     * Contains the coordiantes text view.
     */
    private View coordTextCard;

    /**
     * Constructor.
     *
     * @param map           The map that we display grid overlays on.
     * @param activity      Used to run on the UI thread.
     * @param gridType      The type of grid to display on map.
     * @param coordTextView The text view to display current center of screen coordinates.
     * @param coordTextCard Contains the coordiantes text view.
     */
    public GridController(GoogleMap map, Activity activity, GridType gridType, TextView coordTextView, View coordTextCard) {
        this.map = map;
        this.activity = activity;
        this.coordTextView = coordTextView;
        this.coordTextCard = coordTextCard;
        gridChanged(gridType);
    }

    /**
     * Sets a new grid overlay on the map.
     *
     * @param gridType The new type of grid to display on map.
     */
    public void gridChanged(GridType gridType) {

        this.gridType = gridType;

        if (gridCreator != null) {
            gridCreator.destroy();
            gridCreator = null;
        }

        if (tileOverlay != null) {
            tileOverlay.remove();
            tileOverlay = null;
        }

        if (gridType == GridType.NONE) {
            this.map.setOnCameraIdleListener(null);
            this.map.setOnCameraMoveListener(null);
            this.coordTextCard.setVisibility(View.GONE);
        } else {
            switch (gridType) {
                case GARS:
                    gridCreator = new GARSGridCreator(gridModel, map, activity);
                    break;
                case MGRS:
                    tileOverlay = map.addTileOverlay(
                            new TileOverlayOptions().tileProvider(MGRSTileProvider.create(activity)));
                    break;
                default:
            }
            onCameraIdle();
            onCameraMoved();
            this.map.setOnCameraIdleListener(() -> onCameraIdle());
            this.map.setOnCameraMoveListener(() -> onCameraMoved());
        }

    }

    /**
     * Called when the globes camera stops moving.
     */
    private void onCameraIdle() {
        if (gridCreator != null) {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            gridCreator.createGridForMap(new BoundingBox(bounds.southwest.longitude, bounds.southwest.latitude,
                    bounds.northeast.longitude, bounds.northeast.latitude));
        }
    }

    /**
     * Called when the globes camera moves.
     */
    private void onCameraMoved() {
        LatLng center = map.getCameraPosition().target;
        String coordinate = null;
        switch (gridType) {
            case MGRS:
                coordinate = Point.create(center.longitude, center.latitude).toMGRS().coordinate();
                break;
            default:
        }
        if (coordinate != null) {
            coordTextCard.setVisibility(View.VISIBLE);
            coordTextView.setVisibility(View.VISIBLE);
            coordTextView.setText(coordinate);
        } else {
            coordTextCard.setVisibility(View.GONE);
        }
    }
}

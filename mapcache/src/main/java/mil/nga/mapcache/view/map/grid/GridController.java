package mil.nga.mapcache.view.map.grid;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.gars.GARS;
import mil.nga.gars.tile.GARSTileProvider;
import mil.nga.grid.features.Point;
import mil.nga.mapcache.preferences.GridType;
import mil.nga.mgrs.MGRS;
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
     * Any existing camera idle listeners.
     */
    private GoogleMap.OnCameraIdleListener idleListener;

    /**
     * Any existing camera move listeners.
     */
    private GoogleMap.OnCameraMoveListener moveListener;

    /**
     * Constructor.
     *
     * @param map           The map that we display grid overlays on.
     * @param activity      Used to run on the UI thread.
     * @param gridType      The type of grid to display on map.
     * @param coordTextView The text view to display current center of screen coordinates.
     * @param coordTextCard Contains the coordiantes text view.
     * @param idleListener  Any existing camera idle listeners.
     * @param moveListener  Any existing camera move listeners.
     */
    public GridController(GoogleMap map,
                          Activity activity,
                          GridType gridType,
                          TextView coordTextView,
                          View coordTextCard,
                          GoogleMap.OnCameraIdleListener idleListener,
                          GoogleMap.OnCameraMoveListener moveListener) {
        this.map = map;
        this.activity = activity;
        this.coordTextView = coordTextView;
        this.coordTextCard = coordTextCard;
        this.idleListener = idleListener;
        this.moveListener = moveListener;
        gridChanged(gridType);
    }

    /**
     * Sets a new grid overlay on the map.
     *
     * @param gridType The new type of grid to display on map.
     */
    public void gridChanged(GridType gridType) {

        this.gridType = gridType;

        if (tileOverlay != null) {
            tileOverlay.remove();
            tileOverlay = null;
        }

        if (gridType == GridType.NONE) {
            this.map.setOnCameraIdleListener(this.idleListener);
            this.map.setOnCameraMoveListener(this.moveListener);
            this.coordTextCard.setVisibility(View.GONE);
        } else {
            TileProvider tileProvider = null;
            switch (gridType) {
                case GARS:
                    tileProvider = GARSTileProvider.create(activity);
                    break;
                case MGRS:
                    tileProvider = MGRSTileProvider.create(activity);
                    break;
                default:
                    throw new IllegalStateException("Unsupported grid type: " + gridType);
            }
            tileOverlay = map.addTileOverlay(
                    new TileOverlayOptions().tileProvider(tileProvider));
            onCameraIdle();
            onCameraMoved();
            this.map.setOnCameraIdleListener(this::onCameraIdle);
            this.map.setOnCameraMoveListener(this::onCameraMoved);
        }

    }

    /**
     * Called when the globes camera stops moving.
     */
    private void onCameraIdle() {
        if(this.idleListener != null) {
            this.idleListener.onCameraIdle();
        }
    }

    /**
     * Called when the globes camera moves.
     */
    private void onCameraMoved() {
        if(this.moveListener != null) {
            this.moveListener.onCameraMove();
        }

        if (gridType == GridType.NONE) {
            coordTextCard.setVisibility(View.GONE);
        }else {
            LatLng center = map.getCameraPosition().target;
            Point point = Point.point(center.longitude, center.latitude);
            String coordinate = null;
            switch (gridType) {
                case GARS:
                    coordinate = GARS.from(point).coordinate();
                    break;
                case MGRS:
                    coordinate = MGRS.from(point).coordinate();
                    break;
                default:
                    throw new IllegalStateException("Unsupported grid type: " + gridType);
            }
            coordTextCard.setVisibility(View.VISIBLE);
            coordTextView.setVisibility(View.VISIBLE);
            coordTextView.setText(coordinate);
        }
    }
}

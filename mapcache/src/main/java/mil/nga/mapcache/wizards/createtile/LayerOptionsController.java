package mil.nga.mapcache.wizards.createtile;

import android.graphics.Bitmap;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;
import java.util.Observer;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.proj.ProjectionConstants;

/**
 * Performs validation and loads the tiles into the geopackage.
 */
public class LayerOptionsController implements Observer {

    /**
     * Contains the bounding box the user wants to put into the geopackage.
     */
    private IBoundingBoxManager boxManager;

    /**
     * The model shared between the UI and controller.
     */
    private LayerOptionsModel model;

    /**
     * Callback for load tiles task.
     */
    private ILoadTilesTask callback;

    /**
     * The active geopackages.
     */
    private GeoPackageDatabases active;

    /**
     * The activity to pass to the load tile task.
     */
    private FragmentActivity activity;

    /**
     * Constructor.
     *
     * @param boxManager Contains the bounding box the user wants to put into the geopackage.
     * @param callback   Callback for load tiles task.
     * @param active     The active geopackages.
     * @param activity   The activity to pass to the load tile task.
     * @param model      The model shared between the UI and controller.
     */
    public LayerOptionsController(IBoundingBoxManager boxManager, ILoadTilesTask callback,
                                  GeoPackageDatabases active, FragmentActivity activity,
                                  LayerOptionsModel model) {
        this.boxManager = boxManager;
        this.callback = callback;
        this.active = active;
        this.activity = activity;
        this.model = model;
        this.model.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (LayerOptionsModel.MAX_ZOOM_PROP.equals(o) || LayerOptionsModel.MIN_ZOOM_PROP.equals(o)) {
            if (model.getMinZoom() > model.getMaxZoom()) {
                model.setValidationMessage("Min zoom can't be more than max zoom");
            } else {
                model.setValidationMessage("");
            }
        }
    }

    /**
     * Loads the tiles as specified by the user into the active geopackages.
     */
    public void loadTiles() {
        boolean xyzTiles = false;
        if (model.getTileFormat().equalsIgnoreCase("google")) {
            xyzTiles = true;
        }

        Bitmap.CompressFormat compressFormat = null;
        Integer compressQuality = 100;
        TileScaling scaling = null;
        double minLat = 90.0;
        double minLon = 180.0;
        double maxLat = -90.0;
        double maxLon = -180.0;
        for (LatLng point : boxManager.getBoundingBox().getPoints()) {
            minLat = Math.min(minLat, point.latitude);
            minLon = Math.min(minLon, point.longitude);
            maxLat = Math.max(maxLat, point.latitude);
            maxLon = Math.max(maxLon, point.longitude);
        }
        BoundingBox boundingBox = new BoundingBox(minLon,
                minLat, maxLon, maxLat);

        // Load tiles
        LoadTilesTask.loadTiles(activity,
                callback, active,
                model.getGeopackageName(), model.getLayerName(), model.getUrl(), model.getMinZoom(),
                model.getMaxZoom(), compressFormat,
                compressQuality, xyzTiles,
                boundingBox, scaling,
                ProjectionConstants.AUTHORITY_EPSG, String.valueOf(model.getEpsg()));
    }
}

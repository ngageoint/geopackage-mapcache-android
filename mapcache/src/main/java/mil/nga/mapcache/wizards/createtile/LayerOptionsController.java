package mil.nga.mapcache.wizards.createtile;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;
import java.util.Observer;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.layersprovider.LayerModel;
import mil.nga.mapcache.layersprovider.LayersModel;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.mapcache.ogc.wms.WMSUrlProvider;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.proj.ProjectionConstants;

/**
 * Performs validation and loads the tiles into the geoPackage.
 */
public class LayerOptionsController implements Observer {

    /**
     * Contains the bounding box the user wants to put into the geoPackage.
     */
    private final IBoundingBoxManager boxManager;

    /**
     * The model shared between the UI and controller.
     */
    private final  LayerOptionsModel model;

    /**
     * Callback for load tiles task.
     */
    private final ILoadTilesTask callback;

    /**
     * Used to get the geoPackage.
     */
    private final GeoPackageViewModel viewModel;

    /**
     * The activity to pass to the load tile task.
     */
    private final FragmentActivity activity;

    /**
     * The model containing the selected layer.
     */
    private final LayersModel layers;

    /**
     * Constructor.
     *
     * @param boxManager Contains the bounding box the user wants to put into the geoPackage.
     * @param callback   Callback for load tiles task.
     * @param viewModel  Used to get the geoPackage.
     * @param activity   The activity to pass to the load tile task.
     * @param model      The model shared between the UI and controller.
     * @param layers     The model containing the selected layer.
     */
    public LayerOptionsController(IBoundingBoxManager boxManager, ILoadTilesTask callback,
                                  GeoPackageViewModel viewModel, FragmentActivity activity,
                                  LayerOptionsModel model, LayersModel layers) {
        this.boxManager = boxManager;
        this.callback = callback;
        this.viewModel = viewModel;
        this.activity = activity;
        this.model = model;
        this.layers = layers;

        LayerModel[] selectedLayers = this.layers.getSelectedLayers();
        if (selectedLayers != null && selectedLayers[0].getEpsgs() != null && selectedLayers[0].getEpsgs().length > 0) {
            boolean contained = false;
            for (long epsg : selectedLayers[0].getEpsgs()) {
                if (epsg == model.getEpsg()) {
                    contained = true;
                    break;
                }
            }

            if (!contained) {
                this.model.setEpsg(4326);
            }
        }

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
     * Loads the tiles as specified by the user into the active geoPackages.
     */
    public void loadTiles() {
        boolean xyzTiles = model.getTileFormat().equalsIgnoreCase("google");

        Integer compressQuality = 100;
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

        String url = model.getUrl();
        if (layers.getSelectedLayers() != null && layers.getSelectedLayers()[0].getEpsgs() != null
                && layers.getSelectedLayers()[0].getEpsgs().length > 0) {
            url = WMSUrlProvider.getInstance().getUrlBoundBoxCRS(url, String.valueOf(model.getEpsg()));
        }


        // Load tiles
        LoadTilesTask.loadTiles(activity,
                callback, viewModel,
                model.getGeopackageName(), model.getLayerName(), url, model.getMinZoom(),
                model.getMaxZoom(), null,
                compressQuality, xyzTiles,
                boundingBox, null,
                ProjectionConstants.AUTHORITY_EPSG, String.valueOf(model.getEpsg()),
                layers.getRequestHeaders());
    }
}

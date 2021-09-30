package mil.nga.mapcache.wizards.createtile;

import android.webkit.URLUtil;

import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * Controller class for the first step in creating a new tile layer within a geopackage.
 */
public class NewTileLayerController implements Observer {

    /**
     * The model.
     */
    private NewTileLayerModel model;

    /**
     * Used to validate the layer name.
     */
    private GeoPackageViewModel geoPackageViewModel;

    /**
     * Constructor.
     *
     * @param model               The model.
     * @param geoPackageViewModel Used to ensure unique layer names.
     */
    public NewTileLayerController(NewTileLayerModel model, GeoPackageViewModel geoPackageViewModel) {
        this.model = model;
        this.geoPackageViewModel = geoPackageViewModel;
        this.model.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (NewTileLayerModel.LAYER_NAME_PROP.equals(o)) {
            String givenName = model.getLayerName();

            if (givenName.isEmpty() || givenName.trim().isEmpty()) {
                model.setLayerNameError("Name is required");
            } else if (geoPackageViewModel.tableExistsInGeoPackage(model.getGeopackageName(), model.getLayerName())) {
                model.setLayerNameError("Layer name already exists");
            } else {
                boolean allowed = Pattern.matches("[a-zA-Z_0-9]+", givenName);
                if (!allowed) {
                    model.setLayerNameError("Names must be alphanumeric only");
                } else {
                    model.setLayerNameError(null);
                }
            }
        } else if (NewTileLayerModel.URL_PROP.equals(o)) {
            String givenUrl = model.getUrl();
            if (givenUrl.isEmpty() || givenUrl.trim().isEmpty()) {
                model.setUrlError("URL is required");
            } else if (!URLUtil.isValidUrl(model.getUrl())) {
                model.setUrlError("URL is not valid");
            } else {
                model.setUrlError(null);
            }
        }
    }
}

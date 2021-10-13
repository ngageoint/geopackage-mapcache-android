package mil.nga.mapcache.wizards.createtile;

import android.content.SharedPreferences;
import android.webkit.URLUtil;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Pattern;

import mil.nga.mapcache.R;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * Controller class for the first step in creating a new tile layer within a geopackage.
 */
public class NewTileLayerController implements Observer, Comparator<String> {

    private static String[] preferredImageFormats = {"png", "jpeg", "tiff", "gif"};

    /**
     * The model.
     */
    private NewTileLayerModel model;

    /**
     * Used to validate the layer name.
     */
    private GeoPackageViewModel geoPackageViewModel;

    /**
     * Used to get string constants.
     */
    private Fragment fragment;

    /**
     * Used to get saved urls.
     */
    private SharedPreferences settings;

    /**
     * Constructor.
     *
     * @param model               The model.
     * @param geoPackageViewModel Used to ensure unique layer names.
     * @param fragment            Used to get string constants.
     * @param settings            Used to get saved urls.
     */
    public NewTileLayerController(NewTileLayerModel model, GeoPackageViewModel geoPackageViewModel,
                                  Fragment fragment, SharedPreferences settings) {
        this.model = model;
        this.geoPackageViewModel = geoPackageViewModel;
        this.fragment = fragment;
        this.settings = settings;
        this.model.addObserver(this);
    }

    /**
     * Loads the saved urls into the model.
     */
    public void loadSavedUrls() {
        Set<String> existing = settings.getStringSet(fragment.getString(R.string.geopackage_create_tiles_label), new HashSet<String>());
        String[] urlChoices = existing.toArray(new String[existing.size()]);
        model.setSavedUrls(urlChoices);
    }

    /**
     * Sets the proper url in the model based on what the user has selected.
     *
     * @param layersModel The model containing user input.
     */
    public void setUrl(LayersModel layersModel) {
        String format = getFormat(layersModel);
        model.setUrl(model.getUrl() + "?service=WMS&request=GetMap&layers="
                + layersModel.getSelectedLayer().getName()
                + "&styles=&format=" + format + "&transparent=true&version=1.3.0&width=256&"
                + "height=256&crs=EPSG:3857&bbox={minLon},{minLat},{maxLon},{maxLat}");
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

    @Override
    public int compare(String s, String t1) {
        int sIndex = formatIndex(s);
        int t1Index = formatIndex(t1);

        return Integer.compare(sIndex, t1Index);
    }

    /**
     * Based on the available formats gets the most preferred format out of all of them.
     *
     * @param layersModel Contains the available formats.
     * @return The best format to use for tile downloads.
     */
    private String getFormat(LayersModel layersModel) {
        List<String> formats = new ArrayList<String>(Arrays.asList(layersModel.getImageFormats()));
        Collections.sort(formats, this);

        return formats.get(0);
    }

    /**
     * Finds the index of the format within the preferred format array.
     *
     * @param format The format to check.
     * @return The index of the format within the preferred format array, or an index outside
     * the bounds of the preferred format array if its an unknown format.
     */
    private int formatIndex(String format) {
        int index = 0;
        for (String prefFormat : preferredImageFormats) {
            if (format.endsWith(prefFormat)) {
                break;
            }
            index++;
        }

        return index;
    }
}

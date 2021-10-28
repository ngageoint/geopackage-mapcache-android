package mil.nga.mapcache.preferences;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import mil.nga.mapcache.R;
import mil.nga.mapcache.layersprovider.LayersModel;
import mil.nga.mapcache.layersprovider.LayersProvider;
import mil.nga.mapcache.ogc.wms.Layer;

/**
 * The controller for the basemap settings.
 */
public class BasemapSettingsController implements Observer {

    /**
     * Used to get all the saved urls and selected base maps.
     */
    private SharedPreferences prefs;

    /**
     * The model containing the available layers.
     */
    private BasemapSettingsModel model;

    /**
     * The activity.
     */
    private Activity activity;

    /**
     * Constructor.
     *
     * @param activity The activity.
     * @param prefs    The preferences.
     * @param model    The model containing the available layers.
     */
    public BasemapSettingsController(Activity activity, SharedPreferences prefs, BasemapSettingsModel model) {
        this.activity = activity;
        this.prefs = prefs;
        this.model = model;
        loadModel();
    }

    /**
     * Loads the model with the available layers and what basemaps have been selected.
     */
    private void loadModel() {
        String basemapsString = prefs.getString(activity.getString(R.string.selectedBasemaps), "");
        model.fromString(basemapsString);

        // Default map
        BasemapServerModel defaultMap = new BasemapServerModel();
        defaultMap.setServerUrl(String.valueOf(GoogleMap.MAP_TYPE_NORMAL));
        defaultMap.setName("Map");

        // Satellite map
        BasemapServerModel satellite = new BasemapServerModel();
        satellite.setServerUrl(String.valueOf(GoogleMap.MAP_TYPE_SATELLITE));
        satellite.setName("Satellite");

        // Terrain map
        BasemapServerModel terrain = new BasemapServerModel();
        terrain.setServerUrl(String.valueOf(GoogleMap.MAP_TYPE_TERRAIN));
        terrain.setName("Terrain");

        BasemapServerModel[] exclusives = {defaultMap, satellite, terrain};
        model.setExclusiveServers(exclusives);

        Set<String> savedUrls = new HashSet<>();
        savedUrls = prefs.getStringSet(activity.getString(R.string.geopackage_create_tiles_label),
                savedUrls);
        BasemapServerModel[] servers = new BasemapServerModel[savedUrls.size()];
        int index = 0;
        for (String savedUrl : savedUrls) {
            BasemapServerModel server = new BasemapServerModel();
            server.setServerUrl(savedUrl);
            LayersProvider provider = new LayersProvider(activity, server.getLayers());
            provider.retrieveLayers(server.getServerUrl());
            servers[index] = server;
            index++;
        }

        model.setAvailableServers(servers);
        model.addObserver(this);
        for (BasemapServerModel selectedServers : model.getSelectedBasemap()) {
            selectedServers.getLayers().addObserver(this);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (BasemapSettingsModel.SELECTED_BASEMAP_PROP.equals(o)
                || LayersModel.SELECTED_LAYERS_PROP.equals(o)) {
            if(BasemapSettingsModel.SELECTED_BASEMAP_PROP.equals(o)) {
                for (BasemapServerModel selectedServers : model.getSelectedBasemap()) {
                    selectedServers.getLayers().deleteObserver(this);
                    selectedServers.getLayers().addObserver(this);
                }
            }

            String selectedBasemapString = model.toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(activity.getString(R.string.selectedBasemaps), selectedBasemapString);
            editor.commit();
        }
    }
}

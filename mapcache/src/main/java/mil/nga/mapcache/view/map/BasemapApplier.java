package mil.nga.mapcache.view.map;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mil.nga.mapcache.layersprovider.LayerModel;
import mil.nga.mapcache.preferences.BasemapServerModel;
import mil.nga.mapcache.preferences.BasemapSettings;
import mil.nga.mapcache.preferences.BasemapSettingsLoader;
import mil.nga.mapcache.view.map.grid.GridController;
import mil.nga.mapcache.view.map.overlays.WMSTileProvider;
import mil.nga.mapcache.view.map.overlays.XYZTileProvider;

/**
 * Based on the settings applies various base maps to the map if the user has selected any custom
 * ones.
 */
public class BasemapApplier {

    /**
     * Used to help get preferences.
     */
    private Activity activity;

    /**
     * Contains the selected basemap settings.
     */
    private SharedPreferences prefs;

    /**
     * The current base map tile overlays displaying on the map.
     */
    private Map<String, Map<String, TileOverlay>> currentProviders = new HashMap<>();

    /**
     * The current zIndex.
     */
    private int zIndex = -2;

    /**
     * Draws the selected grid overlay on the map, if any are selected.
     */
    private GridController grid;

    /**
     * Constructor.
     *
     * @param activity The current activity.
     * @param prefs    Access to the preferences.
     */
    public BasemapApplier(Activity activity, SharedPreferences prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    /**
     * Adds or removes any custom basemaps to the map based on settings selected by user.
     *
     * @param map The map to add basemaps too.
     */
    public void applyBasemaps(GoogleMap map) {
        applyGoogleBasemap(map);

        BasemapSettings settings = BasemapSettingsLoader.getInstance().loadSettings(activity, prefs);
        applyUserBasemap(map, settings);
        applyGridOverlay(map, settings);
    }

    /**
     * Applies the google map type setting to the map.
     *
     * @param map The map to apply basemap to.
     */
    private void applyGoogleBasemap(GoogleMap map) {
        int mapType = prefs.getInt(BasemapSettingsLoader.MAP_TYPE_KEY, 1);
        map.setMapType(mapType);
    }

    /**
     * Applies a user specified tile server as a base map to the map.
     *
     * @param map      The map to apply basemap to.
     * @param settings The basemap settings configured by user.
     */
    private void applyUserBasemap(GoogleMap map, BasemapSettings settings) {
        Map<String, Set<String>> allProviders = new HashMap<>();
        for (BasemapServerModel server : settings.getSelectedBasemap()) {
            allProviders.put(server.getServerUrl(), new HashSet<>());
            if (server.getLayers().getSelectedLayers() != null
                    && server.getLayers().getSelectedLayers().length > 0) {

                if (!currentProviders.containsKey(server.getServerUrl())) {
                    currentProviders.put(server.getServerUrl(), new HashMap<>());
                }
                Map<String, TileOverlay> serversProviders = currentProviders.get(server.getServerUrl());


                for (LayerModel layer : server.getLayers().getSelectedLayers()) {
                    allProviders.get(server.getServerUrl()).add(layer.getName());
                    if (!serversProviders.containsKey(layer.getName())) {
                        addLayer(server.getServerUrl(), layer.getName(), map);
                    }
                }
            } else {
                Set<String> layers = new HashSet<>();
                layers.add("");
                allProviders.put(server.getServerUrl(), layers);
                if (!currentProviders.containsKey(server.getServerUrl())) {
                    addLayer(server.getServerUrl(), "", map);
                }
            }
        }

        List<String> serversToRemove = new ArrayList<>();
        Map<String, List<String>> layersToRemove = new HashMap<>();
        for (Map.Entry<String, Map<String, TileOverlay>> entry : currentProviders.entrySet()) {
            if (!allProviders.containsKey(entry.getKey())) {
                serversToRemove.add(entry.getKey());
            } else {
                List<String> layers = new ArrayList<>();
                layersToRemove.put(entry.getKey(), layers);
                Set<String> allLayers = allProviders.get(entry.getKey());
                for (Map.Entry<String, TileOverlay> layerEntry : entry.getValue().entrySet()) {
                    if (!allLayers.contains(layerEntry.getKey())) {
                        layers.add(layerEntry.getKey());
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : layersToRemove.entrySet()) {
            Map<String, TileOverlay> providers = currentProviders.get(entry.getKey());
            for (String layerName : entry.getValue()) {
                removeLayer(entry.getKey(), layerName);
            }
        }

        for (String serverName : serversToRemove) {
            Map<String, TileOverlay> providers = currentProviders.get(serverName);
            for (Map.Entry<String, TileOverlay> entry : providers.entrySet()) {
                removeLayer(serverName, entry.getKey());
            }
        }
    }

    /**
     * Applies a grid overlay basemap if selected by the user.
     *
     * @param map      The map to apply the overlay to.
     * @param settings Contains the user selected grid overlay settings.
     */
    private void applyGridOverlay(GoogleMap map, BasemapSettings settings) {
        if (grid == null) {
            grid = new GridController(map, this.activity, settings.getGridOverlaySettings().getSelectedGrid());
        } else {
            grid.gridChanged(settings.getGridOverlaySettings().getSelectedGrid());
        }
    }

    /**
     * Adds the specified tile layer to the map.
     *
     * @param baseUrl   The base url to the tile server.
     * @param layerName The name of the layer or empty string if the url already points to a layer.
     * @param map       The map to add the layer too.
     */
    public void addLayer(String baseUrl, String layerName, GoogleMap map) {
        TileProvider provider = createProvider(baseUrl, layerName);
        if (provider != null) {
            TileOverlayOptions options = new TileOverlayOptions();
            options.tileProvider(provider);
            options.zIndex(zIndex++);
            Map<String, TileOverlay> serversProviders = currentProviders.get(baseUrl);
            if (serversProviders == null) {
                serversProviders = new HashMap<>();
                currentProviders.put(baseUrl, serversProviders);
            }
            serversProviders.put(layerName, map.addTileOverlay(options));
        }
    }

    /**
     * Removes the layer from the map.
     *
     * @param baseUrl   The url to the tile server.
     * @param layerName The name of the layer or empty string if the url already points to a layer.
     */
    public void removeLayer(String baseUrl, String layerName) {
        Map<String, TileOverlay> providers = currentProviders.get(baseUrl);
        if (providers != null) {
            TileOverlay overlay = providers.remove(layerName);
            overlay.setVisible(false);
            overlay.remove();
            zIndex--;
            if (providers.isEmpty()) {
                currentProviders.remove(baseUrl);
            }
        }
    }

    /**
     * Clears out any
     */
    public void clear() {
        currentProviders.clear();
    }

    /**
     * Sets the map type.
     *
     * @param map     The google map to set the map type.
     * @param mapType The base map type.
     */
    public void setMapType(GoogleMap map, int mapType) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(BasemapSettingsLoader.MAP_TYPE_KEY, mapType);
        editor.commit();
        if (map != null) {
            map.setMapType(mapType);
        }
    }

    /**
     * Creates the TileProvider for the specific server based on the url format.
     *
     * @param baseUrl   The server's base url.
     * @param layerName The name of the layer or empty string if the server is a one layer server.
     * @return The tile provider or null.
     */
    private TileProvider createProvider(String baseUrl, String layerName) {
        TileProvider tileProvider = null;

        if (WMSTileProvider.canProvide(baseUrl)) {
            tileProvider = new WMSTileProvider(baseUrl, layerName, "image/png");
        } else if (XYZTileProvider.canProvide(baseUrl)) {
            tileProvider = new XYZTileProvider(baseUrl);
        }

        return tileProvider;
    }
}

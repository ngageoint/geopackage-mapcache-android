package mil.nga.mapcache.preferences;

import android.app.Activity;
import android.content.SharedPreferences;

import mil.nga.mapcache.R;

/**
 * Loads the selected basemap settings.
 */
public class BasemapSettingsLoader {

    /**
     * The instance of the loader.
     */
    private static BasemapSettingsLoader instance = new BasemapSettingsLoader();

    /**
     * Map type key for saving to preferences
     */
    public static final String MAP_TYPE_KEY = "map_type_key";

    /**
     * Gets the instance of this loader.
     *
     * @return
     */
    public static BasemapSettingsLoader getInstance() {
        return instance;
    }

    /**
     * Loads the selected basemap settings.
     *
     * @param activity Used to load the settings.
     * @param prefs    Contains the settings.
     * @return The selected basemap settings.
     */
    public BasemapSettings loadSettings(Activity activity, SharedPreferences prefs) {
        BasemapSettings model = new BasemapSettings();

        loadSettings(activity, prefs, model);

        return model;
    }

    /**
     * Loads the selected basemap settings.
     *
     * @param activity Used to load the settings.
     * @param prefs    Contains the settings.
     * @param settings An existing settings object that will be populated with the selected basemaps
     *                 within prefs.
     */
    public void loadSettings(Activity activity, SharedPreferences prefs, BasemapSettings settings) {
        String basemapsString = prefs.getString(activity.getString(R.string.selectedBasemaps), "");
        settings.fromString(basemapsString);
        int mapType = prefs.getInt(BasemapSettingsLoader.MAP_TYPE_KEY, 1);
        settings.getSelectedBasemap()[0].setServerUrl(String.valueOf(mapType));
    }

    /**
     * Private constructor helps keep it a singleton.
     */
    private BasemapSettingsLoader() {

    }
}

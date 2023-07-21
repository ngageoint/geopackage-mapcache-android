package mil.nga.mapcache.preferences;

import android.app.Activity;
import android.content.SharedPreferences;

import mil.nga.mapcache.R;

/**
 * Loads the selected basemap settings.
 */
public class BasemapSettingsIO {

    /**
     * The instance of the loader.
     */
    private static BasemapSettingsIO instance = new BasemapSettingsIO();

    /**
     * Map type key for saving to preferences
     */
    public static final String MAP_TYPE_KEY = "map_type_key";

    /**
     * Gets the instance of this loader.
     *
     * @return
     */
    public static BasemapSettingsIO getInstance() {
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
        int mapType = prefs.getInt(BasemapSettingsIO.MAP_TYPE_KEY, 1);
        if (settings.getSelectedBasemap() == null || settings.getSelectedBasemap().length == 0) {
            BasemapServerModel[] selected = new BasemapServerModel[1];
            selected[0] = new BasemapServerModel();
            settings.setSelectedBasemap(selected);
        }
        settings.getSelectedBasemap()[0].setServerUrl(String.valueOf(mapType));
    }

    /**
     * Saves the basemap settings to the preferences.
     *
     * @param activity Used to get string constants.
     * @param prefs    The preferences to save to.
     * @param settings The new settings to save.
     */
    public void saveSettings(Activity activity, SharedPreferences prefs, BasemapSettings settings) {
        String selectedBasemapString = settings.toString();
        SharedPreferences.Editor editor = prefs.edit();

        // Set the map type for google map selection
        if(settings.getSelectedBasemap().length > 0) {
            BasemapServerModel model = settings.getSelectedBasemap()[0];
            String serverUrl = model.getServerUrl();
            if(serverUrl == null || serverUrl.isEmpty()) return;
            int mapType = Integer.parseInt(serverUrl);
            editor.putInt(BasemapSettingsIO.MAP_TYPE_KEY, mapType);

            editor.putString(activity.getString(R.string.selectedBasemaps), selectedBasemapString);
            editor.commit();
        }
    }

    /**
     * Private constructor helps keep it a singleton.
     */
    private BasemapSettingsIO() {

    }
}

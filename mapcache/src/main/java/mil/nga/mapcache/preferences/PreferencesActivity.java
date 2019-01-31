package mil.nga.mapcache.preferences;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import mil.nga.mapcache.LayerDetailFragment;
import mil.nga.mapcache.R;

/**
 * Activity for the Preferences menu
 */
public class PreferencesActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    /**
     * Create the activity and set to content_frame
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getSupportActionBar().setTitle("Settings");
        // Adds back arrow button to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferencesFragment()).commit();
    }

    /**
     * Add back arrow button listener
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
        return false;
    }





    /**
     *  -------------------------------------------------------
     * Fragment to hold the preferences from xml/preferences.xml
     */
    public static class PreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        // Preference for setting the map to dark mode
        private Preference darkMap;
        // Preference for making the zoom icons visible
        private Preference zoomIcons;

        /**
         * Build from a preferences file instead of a layout
         * @param bundle
         * @param s
         */
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
            LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
            return super.onCreateView(localInflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            return false;
        }

    }


}

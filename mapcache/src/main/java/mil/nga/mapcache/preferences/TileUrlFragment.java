package mil.nga.mapcache.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import mil.nga.mapcache.R;

public class TileUrlFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    View urlView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        urlView = inflater.inflate(R.layout.fragment_saved_tile_urls, container, false);
        return urlView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}

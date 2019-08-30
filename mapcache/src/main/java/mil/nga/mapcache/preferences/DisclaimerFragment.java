package mil.nga.mapcache.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceFragmentCompat;

import mil.nga.mapcache.R;

public class DisclaimerFragment extends PreferenceFragmentCompat {

    View disclaimerView;

    public DisclaimerFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        disclaimerView = inflater.inflate(R.layout.fragment_disclaimer, container, false);
        return disclaimerView;
    }
}
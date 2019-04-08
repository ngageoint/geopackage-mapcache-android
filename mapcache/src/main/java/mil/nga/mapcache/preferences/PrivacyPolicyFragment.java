package mil.nga.mapcache.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mil.nga.mapcache.R;

/**
 * Privacy Policy page in settings
 */
public class PrivacyPolicyFragment extends PreferenceFragmentCompat {

    View privacyView;

    public PrivacyPolicyFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        privacyView = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        return privacyView;
    }
}

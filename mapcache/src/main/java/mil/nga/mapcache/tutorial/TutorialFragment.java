package mil.nga.mapcache.tutorial;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mil.nga.mapcache.R;

/**
 * Tutorial fragment
 */
public class TutorialFragment extends PreferenceFragmentCompat {
    View tutorialView;

    public TutorialFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tutorialView = inflater.inflate(R.layout.fragment_tutorial, container, false);
        return tutorialView;
    }
}

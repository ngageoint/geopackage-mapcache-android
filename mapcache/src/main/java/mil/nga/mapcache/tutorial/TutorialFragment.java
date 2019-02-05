package mil.nga.mapcache.tutorial;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
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

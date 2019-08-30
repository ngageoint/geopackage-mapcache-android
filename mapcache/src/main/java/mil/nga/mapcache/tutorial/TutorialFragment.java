package mil.nga.mapcache.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

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
        ViewPager2 viewPager = tutorialView.findViewById(R.id.view_pager);
        List<String> pages = new ArrayList<>();
        pages.add("first");
        pages.add("Second");
        pages.add("third");
        TutorialAdapter tutorialAdapter = new TutorialAdapter(getContext(), pages);
        viewPager.setAdapter(tutorialAdapter);
        return tutorialView;
    }
}

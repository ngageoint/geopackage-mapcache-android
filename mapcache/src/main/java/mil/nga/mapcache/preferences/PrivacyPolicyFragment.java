package mil.nga.mapcache.preferences;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

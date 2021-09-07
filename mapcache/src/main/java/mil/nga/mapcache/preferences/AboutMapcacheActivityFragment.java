package mil.nga.mapcache.preferences;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import mil.nga.mapcache.R;

/**
 * About MapCache page in settings
 */
public class AboutMapcacheActivityFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    View aboutView;

    public AboutMapcacheActivityFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        aboutView = inflater.inflate(R.layout.fragment_about_mapcache, container, false);
        createUrls();
        setVersion();
        return aboutView;
    }


    /**
     * Create the urls out of the string resources
     */
    private void createUrls(){
        // Mapcache github
        Spanned githubUrl = Html.fromHtml(getString(R.string.mapcache_github_url));
        final TextView mapcacheGithubText = (TextView) aboutView.findViewById(R.id.github_url);
        mapcacheGithubText.setText(githubUrl);
        mapcacheGithubText.setMovementMethod(LinkMovementMethod.getInstance());


        // GeoPackage Android github
        Spanned geopackageGithubUrl = Html.fromHtml(getString(R.string.geopackage_android_url));
        final TextView geoPackageAndroidText = (TextView) aboutView.findViewById(R.id.geopackage_github_url);
        geoPackageAndroidText.setText(geopackageGithubUrl);
        geoPackageAndroidText.setMovementMethod(LinkMovementMethod.getInstance());

        // OGC
        Spanned ogcUrl = Html.fromHtml(getString(R.string.ogc_url));
        final TextView ogcText = (TextView) aboutView.findViewById(R.id.ogc_url);
        ogcText.setText(ogcUrl);
        ogcText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Set app version info from gradle file
     */
    private void setVersion(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("About MapCache: ", e.toString());
        }

        String version = String.valueOf(R.string.app_version);
        if (packageInfo != null) {
            version = packageInfo.versionName;
        }
        TextView versionName = aboutView.findViewById(R.id.mapcache_version);
        versionName.setText(version);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }
}

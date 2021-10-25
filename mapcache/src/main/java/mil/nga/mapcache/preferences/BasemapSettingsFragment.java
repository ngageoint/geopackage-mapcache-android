package mil.nga.mapcache.preferences;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.ViewAnimation;

/**
 *  Fragment giving the user a way to modify a saved list of URLs, which will be used in the create
 *  tile layer wizard
 */
public class BasemapSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener, Observer {

    /**
     * The model.
     */
    private BasemapSettingsModel model = new BasemapSettingsModel();

    /**
     * The controller.
     */
    private BasemapSettingsController controller;

    /**
     * The list of available basemaps.
     */
    private LinearLayout listView;

    /**
     * Used to create each row view.
     */
    private LayoutInflater inflater;

    /**
     * The activity.
     */
    private Activity activity;

    /**
     * The rows of saved urls.
     */
    private List<BasemapSettingsRowView> rows = new ArrayList<>();

    /**
     * Constructor.
     * @param activity The activity.
     */
    public BasemapSettingsFragment(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    /**
     * Create the parent view and set up listeners
     * @param inflater Layout inflator
     * @param container Main container
     * @param savedInstanceState Saved instance state
     * @return Parent view for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View basemapView = inflater.inflate(R.layout.base_map_settings, container, false);
        listView = basemapView.findViewById(R.id.item_list_layout);
        this.controller = new BasemapSettingsController(
                activity,
                getPreferenceManager().getSharedPreferences(),
                model);
        model.addObserver(this);
        refreshListView();
        return basemapView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(BasemapSettingsModel.AVAILABLE_SERVERS_PROP.equals(o)) {
            refreshListView();
        }
    }

    /**
     * Creates a row for each saved url.
     */
    private void refreshListView() {
        rows.clear();
        listView.removeAllViews();
        for(BasemapServerModel server : model.getAvailableServers()) {
            BasemapSettingsRowView row = new BasemapSettingsRowView(inflater, server);
            rows.add(row);
            listView.addView(row.getView());
        }
    }
}

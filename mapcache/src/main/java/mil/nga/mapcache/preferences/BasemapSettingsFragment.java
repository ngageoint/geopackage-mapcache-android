package mil.nga.mapcache.preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RadioButton;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

import mil.nga.mapcache.R;

/**
 * Fragment giving the user a way to modify a saved list of URLs, which will be used in the create
 * tile layer wizard
 */
public class BasemapSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    /**
     * The model.
     */
    private BasemapSettings model = new BasemapSettings();

    /**
     * The controller.
     */
    private BasemapSettingsController controller;

    /**
     * The list of available basemaps.
     */
    private ExpandableListView listView;

    /**
     * Used to create each row view.
     */
    private LayoutInflater inflater;

    /**
     * The activity.
     */
    private Activity activity;

    /**
     * List of the default map layers.
     */
    private List<Button> exclusiveButtons = new ArrayList<>();

    /**
     * List of the grid types
     */
    private List<Button> gridButtons = new ArrayList<>();

    /**
     * The no grid radio button.
     */
    private RadioButton gridNone;

    /**
     * The GARS grid radio button.
     */
    private RadioButton gridGARS;

    /**
     * The MGRS grid radio button.
     */
    private RadioButton gridMGRS;

    /**
     * Constructor.
     *
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
     *
     * @param inflater           Layout inflator
     * @param container          Main container
     * @param savedInstanceState Saved instance state
     * @return Parent view for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View basemapView = inflater.inflate(R.layout.base_map_settings, container, false);
        listView = basemapView.findViewById(R.id.expandableListView);
        this.controller = new BasemapSettingsController(
                activity,
                getPreferenceManager().getSharedPreferences(),
                model);
        listView.setAdapter(new BasemapExpandableListAdapter(inflater, model));

        Button mapButton = basemapView.findViewById(R.id.mapButton);
        mapButton.setOnClickListener(view -> exclusiveMapClicked(view));
        mapButton.setOnTouchListener((view, motionEvent) -> keepPressed(view, motionEvent));
        exclusiveButtons.add(mapButton);

        Button satButton = basemapView.findViewById(R.id.satButton);
        satButton.setOnClickListener(view -> exclusiveMapClicked(view));
        satButton.setOnTouchListener((view, motionEvent) -> keepPressed(view, motionEvent));
        exclusiveButtons.add(satButton);

        Button terrainButton = basemapView.findViewById(R.id.terrainButton);
        terrainButton.setOnClickListener(view -> exclusiveMapClicked(view));
        terrainButton.setOnTouchListener((view, motionEvent) -> keepPressed(view, motionEvent));
        exclusiveButtons.add(terrainButton);

        Button gridNoneButton = basemapView.findViewById(R.id.gridNoneButton);
        gridNoneButton.setOnClickListener(view -> {
            this.model.getGridOverlaySettings().setSelectedGrid(GridType.NONE);
        });
        gridButtons.add(gridNoneButton);
        this.gridNone = basemapView.findViewById(R.id.gridNone);
        this.gridNone.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                this.model.getGridOverlaySettings().setSelectedGrid(GridType.NONE);
            }
        });

        Button gridGarsButton = basemapView.findViewById(R.id.gridGarsButton);
        gridGarsButton.setOnClickListener(view -> {
            this.model.getGridOverlaySettings().setSelectedGrid(GridType.GARS);
        });
        gridButtons.add(gridGarsButton);
        this.gridGARS = basemapView.findViewById(R.id.gridGars);
        this.gridGARS.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                this.model.getGridOverlaySettings().setSelectedGrid(GridType.GARS);
            }
        });

        Button gridMgrsButton = basemapView.findViewById(R.id.gridMgrsButton);
        gridMgrsButton.setOnClickListener(view -> {
            this.model.getGridOverlaySettings().setSelectedGrid(GridType.MGRS);
        });
        gridButtons.add(gridMgrsButton);
        this.gridMGRS = basemapView.findViewById(R.id.gridMGRS);
        this.gridMGRS.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                this.model.getGridOverlaySettings().setSelectedGrid(GridType.MGRS);
            }
        });

        model.addObserver((observable, o) -> modelUpdate(observable, o));
        model.getGridOverlaySettings().addObserver((observable, o) -> modelUpdate(observable, o));
        updateButtonColors();
        updateGridChecked();

        Button newServerButton = basemapView.findViewById(R.id.button);
        newServerButton.setOnClickListener(view -> launchSavedUrls());

        return basemapView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.controller.loadModel();
    }

    /**
     * Called when any of the exclusive basemap buttons are clicked.
     *
     * @param view The button.
     */
    private void exclusiveMapClicked(View view) {
        Button button = (Button) view;
        BasemapServerModel mapModel = getExclusiveModel(button.getText().toString());
        if (mapModel != null) {
            addExclusiveToSelected(mapModel);
        }
    }

    /**
     * Gets the exclusive server model based on its name.
     *
     * @param name The name of the server to get.
     * @return The exclusive server model or null.
     */
    private BasemapServerModel getExclusiveModel(String name) {
        BasemapServerModel theOne = null;
        for (BasemapServerModel server : model.getExclusiveServers()) {
            if (server.getName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
                theOne = server;
                break;
            }
        }

        return theOne;
    }

    /**
     * Adds the base map to the list of selected basemaps.
     *
     * @param exclusive The base map to add to selection and replace existing exclusive base map.
     */
    private void addExclusiveToSelected(BasemapServerModel exclusive) {
        BasemapServerModel[] selected = model.getSelectedBasemap();
        if (selected == null || selected.length == 0) {
            selected = new BasemapServerModel[1];
        }

        selected[0] = exclusive;
        model.setSelectedBasemap(selected);
    }

    /**
     * Called whenever the model changes.
     *
     * @param observable The model.
     * @param o          The property that changed.
     */
    private void modelUpdate(Observable observable, Object o) {
        if (BasemapSettings.SELECTED_BASEMAP_PROP.equals(o)
                || BasemapSettings.EXCLUSIVE_SERVERS_PROP.equals(o)) {
            updateButtonColors();
        } else if (GridSettingsModel.SELECTED_GRID_PROPERTY.equals(o)) {
            updateGridChecked();
        }
    }

    /**
     * Keeps the buttons in a pressed state.
     *
     * @param view        The button.
     * @param motionEvent The event.
     * @return True.
     */
    private boolean keepPressed(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Button button = (Button) view;
            exclusiveMapClicked(view);
        }

        return true;
    }

    /**
     * Launches the saved urls settings page to allow user to add more tile servers.
     */
    private void launchSavedUrls() {
        Intent myIntent = new Intent(this.activity, TileUrlActivity.class);
        this.activity.startActivity(myIntent);
    }

    /**
     * Updates the button colors to reflect which base map is currently the defaulted one.
     */
    private void updateButtonColors() {
        if (model.getSelectedBasemap() != null && model.getSelectedBasemap().length > 0
                && model.getExclusiveServers() != null) {
            BasemapServerModel exclusiveLayer = model.getSelectedBasemap()[0];
            String name = "";
            for (BasemapServerModel anExclusive : model.getExclusiveServers()) {
                if (anExclusive.getServerUrl().equals(exclusiveLayer.getServerUrl())) {
                    name = anExclusive.getName();
                    break;
                }
            }

            for (Button exclusiveButton : exclusiveButtons) {
                if (exclusiveButton.getText().toString().equalsIgnoreCase(name)) {
                    exclusiveButton.setTextColor(getResources().getColor(R.color.textLinkColor, getContext().getTheme()));
                    exclusiveButton.setTextScaleX(1.2f);
                    exclusiveButton.getCompoundDrawables()[1].setAlpha(255);
                } else {
                    exclusiveButton.setPressed(false);
                    exclusiveButton.setTextColor(getResources().getColor(R.color.textPrimaryColor, getContext().getTheme()));
                    exclusiveButton.setTextScaleX(1);
                    exclusiveButton.getCompoundDrawables()[1].setAlpha(200);
                }
            }
        }
    }

    /**
     * Updates which grid is selected.
     */
    private void updateGridChecked() {

        GridType selectedGridType = model.getGridOverlaySettings().getSelectedGrid();
        for(Button gridButton : gridButtons){
            if(gridButton.getText().toString().equalsIgnoreCase(selectedGridType.toString())){
                gridButton.setTextColor(getResources().getColor(R.color.textLinkColor, getContext().getTheme()));
                gridButton.setTextScaleX(1.2f);
            } else {
                gridButton.setTextColor(getResources().getColor(R.color.textPrimaryColor, getContext().getTheme()));
                gridButton.setTextScaleX(1);
            }
        }

        if (model.getGridOverlaySettings().getSelectedGrid() == GridType.NONE) {
            this.gridNone.setChecked(true);
        } else if (model.getGridOverlaySettings().getSelectedGrid() == GridType.GARS) {
            this.gridGARS.setChecked(true);
        } else if (model.getGridOverlaySettings().getSelectedGrid() == GridType.MGRS) {
            this.gridMGRS.setChecked(true);
        }
    }
}

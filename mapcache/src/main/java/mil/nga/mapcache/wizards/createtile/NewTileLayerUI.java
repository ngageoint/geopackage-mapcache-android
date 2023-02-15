package mil.nga.mapcache.wizards.createtile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Observable;

import mil.nga.mapcache.R;
import mil.nga.mapcache.layersprovider.LayersModel;
import mil.nga.mapcache.layersprovider.LayersProvider;
import mil.nga.mapcache.layersprovider.LayersView;
import mil.nga.mapcache.layersprovider.LayersViewDialog;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.utils.SampleDownloader;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

import java.util.Observer;

/**
 * The first step when creating a new tile layer within a geoPackage.
 */
public class NewTileLayerUI implements Observer {

    /**
     * RecyclerView that will hold our GeoPackages.
     */
    private final RecyclerView geoPackageRecycler;

    /**
     * The map.
     */
    private final IMapView mapView;

    /**
     * Used to get the layout.
     */
    private final FragmentActivity activity;

    /**
     * The app context.
     */
    private final Context context;

    /**
     * The fragment this UI is apart of, used to get resource strings.
     */
    private final Fragment fragment;

    /**
     * Used to get the geoPackage.
     */
    private final GeoPackageViewModel viewModel;

    /**
     * The callback to pass to LoadTilesTask.
     */
    private final ILoadTilesTask callback;

    /**
     * Contains a bounding box that is displayed to the user.
     */
    private final IBoundingBoxManager boxManager;

    /**
     * The model for the UI.
     */
    private final NewTileLayerModel model = new NewTileLayerModel();

    /**
     * The button that takes us to the next step.
     */
    private MaterialButton drawButton;

    /**
     * The text input for the new layer name.
     */
    private TextInputEditText inputName;

    /**
     * The text input for the new url.
     */
    private TextInputEditText inputUrl;

    /**
     * The controller.
     */
    private final NewTileLayerController controller;

    /**
     * Used to show a spinning progress dialog.
     */
    private ProgressDialog progressDialog;

    /**
     * Retrieves all the layers from a given server.
     */
    private LayersProvider provider;

    /**
     * Constructor
     *
     * @param geoPackageRecycler RecyclerView that will hold our GeoPackages.
     * @param mapView            The map.
     * @param boxManager         The bounding box manager.
     * @param activity           Use The app context.
     * @param context            The app context.
     * @param fragment           The fragment this UI is apart of, used to get resource strings.
     * @param viewModel          Used to get the geoPackage.
     * @param callback           The callback to pass to LoadTilesTask.
     * @param geoPackageName     The name of the geoPackage.
     */
    public NewTileLayerUI(RecyclerView geoPackageRecycler, IMapView mapView,
                          IBoundingBoxManager boxManager,
                          FragmentActivity activity, Context context, Fragment fragment,
                          GeoPackageViewModel viewModel,
                          ILoadTilesTask callback, String geoPackageName) {
        this.geoPackageRecycler = geoPackageRecycler;
        this.mapView = mapView;
        this.boxManager = boxManager;
        this.activity = activity;
        this.context = context;
        this.fragment = fragment;
        this.viewModel = viewModel;
        this.callback = callback;
        model.setGeopackageName(geoPackageName);
        model.addObserver(this);
        this.controller = new NewTileLayerController(model, viewModel, fragment,
                PreferenceManager.getDefaultSharedPreferences(activity));
    }

    /**
     * Show the UI for the first step in creating a new tile layer within a geoPackage.
     */
    public void show() {
        BottomSheetBehavior<RecyclerView> behavior = BottomSheetBehavior.from(geoPackageRecycler);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.new_tile_layer_wizard, null);
        // Logo and title
        ImageView closeLogo = (ImageView) alertView.findViewById(R.id.new_layer_close_logo);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        TextView titleText = (TextView) alertView.findViewById(R.id.new_layer_title);
        titleText.setText(context.getResources().getString(R.string.create_tile_layer));
        drawButton = (MaterialButton) alertView.findViewById(R.id.draw_tile_box_button);

        // Validate name to have only alphanumeric chars because of sqlite errors
        inputName = (TextInputEditText) alertView.findViewById(R.id.new_tile_name_text);
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputName.getText() != null) {
                    String givenName = inputName.getText().toString();
                    model.setLayerName(givenName);
                }
            }
        });

        inputUrl = (TextInputEditText) alertView.findViewById(R.id.new_tile_url);
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(activity);
        String defaultTileUrl = settings.getString("default_tile_url", context.getResources().getString(R.string.default_tile_url));
        inputUrl.setText(defaultTileUrl);

        inputUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputUrl.getText() != null) {
                    String givenUrl = inputUrl.getText().toString();
                    model.setUrl(givenUrl);
                }
            }
        });

        // Show a menu to choose from saved urls
        TextView defaultText = alertView.findViewById(R.id.default_url);
        defaultText.setOnClickListener((View view) -> controller.loadSavedUrls());

        // Example URLs from github
        TextView exampleUrlText = alertView.findViewById(R.id.example_urls);
        exampleUrlText.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(fragment.getString(R.string.example_url_header));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context, android.R.layout.select_dialog_item);

            SampleDownloader sampleDownloader = new SampleDownloader(fragment.getActivity(), adapter);
            adapter.add(activity.getString(R.string.fetching_examples));
            sampleDownloader.getExampleData(activity.getString(R.string.sample_tile_urls));
            builder.setAdapter(adapter,
                    (DialogInterface d, int item) -> {
                        if (item >= 0) {
                            String name = adapter.getItem(item);
                            if(!activity.getString(R.string.examples_unavailable).equalsIgnoreCase(name)) {
                                inputName.setText(name);
                                inputUrl.setText(sampleDownloader.getSampleList().get(name));
                            }
                        }
                    });
            builder.show();
        });

        // URL help menu
        TextView urlHelpText = (TextView) alertView.findViewById(R.id.url_help);
        urlHelpText.setOnClickListener((View view) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(fragment.getString(R.string.map_tile_url_header));
            builder.setMessage(fragment.getString(R.string.url_template_message));
            final AlertDialog urlDialog = builder.create();
            builder.setPositiveButton(R.string.button_ok_label,
                    (DialogInterface dialogInterface, int i) -> urlDialog.dismiss());
            builder.show();
        });


        // Open the dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // Click listener for close button
        closeLogo.setOnClickListener((View v) -> alertDialog.dismiss());

        // Listener for the draw button
        drawButton.setOnClickListener((View v) -> {
            if (inputName.getText() != null) {
                model.setLayerName(inputName.getText().toString());
            }
            if (inputUrl.getText() != null) {
                model.setUrl(inputUrl.getText().toString());
            }
            model.setBaseUrl(model.getUrl());
            if ((model.getLayerNameError() == null || model.getLayerNameError().isEmpty())
                    && (model.getUrlError() == null || model.getUrlError().isEmpty())) {
                alertDialog.dismiss();
                LayersModel layers = new LayersModel();
                layers.addObserver(NewTileLayerUI.this);
                showSpinningDialog();
                provider = new LayersProvider(fragment.getActivity(), layers);
                provider.retrieveLayers(model.getUrl());
            }
        });

        alertDialog.show();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (NewTileLayerModel.LAYER_NAME_PROP.equals(o)) {
            inputName.setError(model.getLayerNameError());
            drawButton.setEnabled(model.getLayerNameError() == null || model.getLayerNameError().isEmpty());
        } else if (NewTileLayerModel.URL_ERROR_PROP.equals(o)) {
            inputUrl.setError(model.getUrlError());
            drawButton.setEnabled(model.getUrlError() == null || model.getUrlError().isEmpty());
        } else if (NewTileLayerModel.SAVED_URLS_PROP.equals(o)) {
            showSavedUrls();
        } else if (LayersModel.LAYERS_PROP.equals(o)) {
            hideSpinningDialog();
            LayersModel layers = (LayersModel) observable;
            if (layers.getLayers() == null || layers.getLayers().length == 0 ||
                    (layers.getSelectedLayers() != null && layers.getLayers() != null
                            && layers.getLayers().length > 0)) {
                drawTileBoundingBox(layers);
            } else {
                LayersView layersView = new LayersViewDialog(context, layers);
                layersView.show();
            }
        } else if (LayersModel.SELECTED_LAYERS_PROP.equals(o)) {
            LayersModel layers = (LayersModel) observable;
            controller.setUrl(layers);
            drawTileBoundingBox(layers);
        }
    }

    /**
     * Show a message for the user to draw a bounding box on the map.  use results to create a tile layer
     */
    private void drawTileBoundingBox(LayersModel layers) {
        TileBoundingBoxUI tileBoundsUI = new TileBoundingBoxUI(geoPackageRecycler, mapView,
                boxManager, layers);
        tileBoundsUI.show(activity, context, fragment, viewModel, callback, model);
    }

    /**
     * Shows the saved urls the user can choose from, or a message stating they have none.
     */
    private void showSavedUrls() {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_saved_url_list, null);
        ListView listView = view.findViewById(R.id.list_view);
        ArrayList<SavedUrl> urlList = model.getSavedUrlObjectList();
        SavedUrlAdapter adapter = new SavedUrlAdapter(context, urlList);
        listView.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Saved Tile URLs");
        builder.setView(view);
        AlertDialog ad = builder.show();
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            inputUrl.setText(model.getUrlAtPosition(i));
            inputUrl.setError(null);
            ViewAnimation.setBounceAnimatiom(inputUrl, 200);
            ad.dismiss();
        });
        adapter.updateConnections(urlList);
    }

    /**
     * Shows a spinning dialog while we retrieve layers from server.
     */
    private void showSpinningDialog() {
        this.progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Retrieving Layers");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                (dialog, which) -> this.cancelRetrieveLayers());
        progressDialog.show();
    }

    /**
     * Cancels Retrieving layers.
     */
    private void cancelRetrieveLayers() {
        if (provider != null) {
            this.provider.cancel();
        }
        if (progressDialog != null) {
            this.progressDialog.dismiss();
        }
    }

    /**
     * Hides the spinning dialog.
     */
    private void hideSpinningDialog() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
    }
}

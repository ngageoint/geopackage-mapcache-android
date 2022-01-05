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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Observable;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.layersprovider.LayersModel;
import mil.nga.mapcache.layersprovider.LayersProvider;
import mil.nga.mapcache.layersprovider.LayersView;
import mil.nga.mapcache.layersprovider.LayersViewDialog;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

import java.util.Observer;

/**
 * The first step when creating a new tile layer within a geopackage.
 */
public class NewTileLayerUI implements Observer {

    /**
     * RecyclerView that will hold our GeoPackages.
     */
    private RecyclerView geoPackageRecycler;

    /**
     * The map.
     */
    private IMapView mapView;

    /**
     * Used to get the layout.
     */
    private FragmentActivity activity;

    /**
     * The app context.
     */
    private Context context;

    /**
     * The fragment this UI is apart of, used to get resource strings.
     */
    private Fragment fragment;

    /**
     * Active GeoPackages
     */
    private GeoPackageDatabases active;

    /**
     * The callback to pass to LoadTilesTask.
     */
    private ILoadTilesTask callback;

    /**
     * Contains a bounding box that is displayed to the user.
     */
    private IBoundingBoxManager boxManager;

    /**
     * The model for the UI.
     */
    private NewTileLayerModel model = new NewTileLayerModel();

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
    private NewTileLayerController controller;

    /**
     * If the url given has extra layers with it, this will populate a list of available layers
     * for the user to choose from.
     */
    private LayersProvider provider;

    /**
     * Used to show a spinning progress dialog.
     */
    private ProgressDialog progressDialog;

    /**
     * Constructor
     *
     * @param geoPackageRecycler RecyclerView that will hold our GeoPackages.
     * @param mapView            The map.
     * @param boxManager         The bounding box manager.
     * @param activity           Use The app context.
     * @param context            The app context.
     * @param fragment           The fragment this UI is apart of, used to get resource strings.
     * @param active             The active GeoPackages
     * @param callback           The callback to pass to LoadTilesTask.
     * @param geoPackageName     The name of the geopackage.
     */
    public NewTileLayerUI(RecyclerView geoPackageRecycler, IMapView mapView,
                          IBoundingBoxManager boxManager,
                          FragmentActivity activity, Context context, Fragment fragment,
                          GeoPackageDatabases active, GeoPackageViewModel geoPackageViewModel,
                          ILoadTilesTask callback, String geoPackageName) {
        this.geoPackageRecycler = geoPackageRecycler;
        this.mapView = mapView;
        this.boxManager = boxManager;
        this.activity = activity;
        this.context = context;
        this.fragment = fragment;
        this.active = active;
        this.callback = callback;
        model.setGeopackageName(geoPackageName);
        model.addObserver(this);
        this.controller = new NewTileLayerController(model, geoPackageViewModel, fragment,
                PreferenceManager.getDefaultSharedPreferences(activity));
    }

    /**
     * Show the UI for the first step in creating a new tile layer within a geopackage.
     *
     * @param geoPackageViewModel Used to validate unique layer names.
     */
    public void show(GeoPackageViewModel geoPackageViewModel) {
        BottomSheetBehavior behavior = BottomSheetBehavior.from(geoPackageRecycler);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.new_tile_layer_wizard, null);
        // Logo and title
        ImageView closeLogo = (ImageView) alertView.findViewById(R.id.new_layer_close_logo);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        TextView titleText = (TextView) alertView.findViewById(R.id.new_layer_title);
        titleText.setText("Create Tile Layer");
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
                String givenName = inputName.getText().toString();
                model.setLayerName(givenName);
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
                String givenUrl = inputUrl.getText().toString();
                model.setUrl(givenUrl);
            }
        });

        // Show a menu to choose from saved urls
        TextView defaultText = (TextView) alertView.findViewById(R.id.default_url);
        defaultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.loadSavedUrls();
            }
        });

        // URL help menu
        TextView urlHelpText = (TextView) alertView.findViewById(R.id.url_help);
        urlHelpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(fragment.getString(R.string.map_tile_url_header));
                builder.setMessage(fragment.getString(R.string.url_template_message));
                final AlertDialog urlDialog = builder.create();
                builder.setPositiveButton(R.string.button_ok_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        urlDialog.dismiss();
                    }
                });
                builder.show();
            }
        });


        // Open the dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // Click listener for close button
        closeLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // Listener for the draw button
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setLayerName(inputName.getText().toString());
                model.setUrl(inputUrl.getText().toString());
                model.setBaseUrl(model.getUrl());
                if ((model.getLayerNameError() == null || model.getLayerNameError().isEmpty())
                        && (model.getUrlError() == null || model.getUrlError().isEmpty())) {
                    alertDialog.dismiss();
                    LayersModel layers = new LayersModel();
                    layers.addObserver(NewTileLayerUI.this);
                    showSpinningDialog();
                    LayersProvider provider = new LayersProvider(fragment.getActivity(), layers);
                    provider.retrieveLayers(model.getUrl());
                }
            }
        });

        alertDialog.show();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (NewTileLayerModel.LAYER_NAME_PROP.equals(o)) {
            inputName.setError(model.getLayerNameError());
            if (model.getLayerNameError() == null || model.getLayerNameError().isEmpty()) {
                drawButton.setEnabled(true);
            } else {
                drawButton.setEnabled(false);
            }
        } else if (NewTileLayerModel.URL_ERROR_PROP.equals(o)) {
            inputUrl.setError(model.getUrlError());
            if (model.getUrlError() == null || model.getUrlError().isEmpty()) {
                drawButton.setEnabled(true);
            } else {
                drawButton.setEnabled(false);
            }
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
        tileBoundsUI.show(activity, context, fragment, active, callback, model);
    }

    /**
     * Shows the saved urls the user can choose from, or a message stating they have none.
     */
    private void showSavedUrls() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Saved Tile URLs");
        if (model.getSavedUrls().length > 0) {
            builder.setItems(model.getSavedUrls(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    inputUrl.setText(model.getSavedUrls()[which]);
                    inputUrl.setError(null);
                    ViewAnimation.setBounceAnimatiom(inputUrl, 200);
                }
            });
        } else {
            builder.setMessage(fragment.getString(R.string.no_saved_urls_message));
        }
        builder.show();
    }

    /**
     * Shows a spinning dialog while we retrieve layers from server.
     */
    private void showSpinningDialog() {
        this.progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Retrieving Layers");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
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

package mil.nga.mapcache.wizards.createtile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

public class NewTileLayerUI {

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

    private NewTileLayerModel model = new NewTileLayerModel();

    public NewTileLayerUI(RecyclerView geoPackageRecycler, IMapView mapView,
                          IBoundingBoxManager boxManager,
                          FragmentActivity activity, Context context, Fragment fragment,
                          GeoPackageDatabases active, ILoadTilesTask callback,
                          String geoPackageName) {
        this.geoPackageRecycler = geoPackageRecycler;
        this.mapView = mapView;
        this.boxManager = boxManager;
        this.activity = activity;
        this.context = context;
        this.fragment = fragment;
        this.active = active;
        this.callback = callback;
        model.setGeopackageName(geoPackageName);
    }

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
        final MaterialButton drawButton = (MaterialButton) alertView.findViewById(R.id.draw_tile_box_button);

        // Validate name to have only alphanumeric chars because of sqlite errors
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.new_tile_name_text);
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
                drawButton.setEnabled(true);

                if (givenName.isEmpty()) {
                    inputName.setError("Name is required");
                    drawButton.setEnabled(false);
                } else {
                    boolean allowed = Pattern.matches("[a-zA-Z_0-9]+", givenName);
                    if (!allowed) {
                        inputName.setError("Names must be alphanumeric only");
                        drawButton.setEnabled(false);
                    }
                }
            }
        });

        final TextInputEditText inputUrl = (TextInputEditText) alertView.findViewById(R.id.new_tile_url);
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
                drawButton.setEnabled(true);

                if (givenUrl.isEmpty()) {
                    inputUrl.setError("URL is required");
                    drawButton.setEnabled(false);
                }
            }
        });

        // Show a menu to choose from saved urls
        TextView defaultText = (TextView) alertView.findViewById(R.id.default_url);
        defaultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> existing = settings.getStringSet(fragment.getString(R.string.geopackage_create_tiles_label), new HashSet<String>());
                String[] urlChoices = existing.toArray(new String[existing.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Saved Tile URLs");
                if (urlChoices.length > 0) {
                    builder.setItems(urlChoices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            inputUrl.setText(urlChoices[which]);
                            inputUrl.setError(null);
                            ViewAnimation.setBounceAnimatiom(inputUrl, 200);
                        }
                    });
                } else {
                    builder.setMessage(fragment.getString(R.string.no_saved_urls_message));
                }
                builder.show();
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
                if (model.getLayerName().isEmpty() || model.getLayerName().trim().length() == 0) {
                    inputName.setError("Layer name must not be blank");
                    drawButton.setEnabled(false);
                } else if (model.getUrl().isEmpty() || model.getUrl().trim().length() == 0) {
                    inputUrl.setError("URL must not be blank");
                    drawButton.setEnabled(false);
                } else if (geoPackageViewModel.tableExistsInGeoPackage(model.getGeopackageName(), model.getLayerName())) {
                    inputName.setError("Layer name already exists");
                    drawButton.setEnabled(false);
                } else if (!URLUtil.isValidUrl(model.getUrl())) {
                    inputUrl.setError("URL is not valid");
                    drawButton.setEnabled(false);
                } else {
                    alertDialog.dismiss();
                    drawTileBoundingBox();
                }
            }
        });

        alertDialog.show();
    }

    /**
     * Show a message for the user to draw a bounding box on the map.  use results to create a tile layer
     */
    private void drawTileBoundingBox() {
        TileBoundingBoxUI tileBoundsUI = new TileBoundingBoxUI(geoPackageRecycler, mapView, boxManager);
        tileBoundsUI.show(activity, context, fragment, active, callback,
                model.getGeopackageName(), model.getLayerName(), model.getUrl());
    }
}

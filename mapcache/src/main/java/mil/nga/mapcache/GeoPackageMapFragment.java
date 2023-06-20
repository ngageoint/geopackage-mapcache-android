package mil.nga.mapcache;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.locationtech.proj4j.units.Units;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumns;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexListResults;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.index.MultipleFeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.map.MapUtils;
import mil.nga.geopackage.map.features.FeatureInfoBuilder;
import mil.nga.geopackage.map.features.StyleCache;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeConverter;
import mil.nga.geopackage.map.geom.GoogleMapShapeMarkers;
import mil.nga.geopackage.map.geom.GoogleMapShapeType;
import mil.nga.geopackage.map.geom.PolygonHoleMarkers;
import mil.nga.geopackage.map.geom.ShapeMarkers;
import mil.nga.geopackage.map.geom.ShapeWithChildrenMarkers;
import mil.nga.geopackage.map.tiles.overlay.FeatureOverlayQuery;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTableType;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.listeners.DetailLayerClickListener;
import mil.nga.mapcache.listeners.EnableAllLayersListener;
import mil.nga.mapcache.listeners.FeatureColumnListener;
import mil.nga.mapcache.listeners.GeoPackageClickListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.listeners.OnDialogButtonClickListener;
import mil.nga.mapcache.listeners.SensorCallback;
import mil.nga.mapcache.load.DownloadTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.ImportTask;
import mil.nga.mapcache.load.ShareTask;
import mil.nga.mapcache.preferences.GridType;
import mil.nga.mapcache.preferences.PreferencesActivity;
import mil.nga.mapcache.repository.GeoPackageModifier;
import mil.nga.mapcache.sensors.SensorHandler;
import mil.nga.mapcache.utils.ProjUtils;
import mil.nga.mapcache.utils.SampleDownloader;
import mil.nga.mapcache.utils.SwipeController;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.view.GeoPackageAdapter;
import mil.nga.mapcache.view.detail.DetailActionUtil;
import mil.nga.mapcache.view.detail.DetailPageAdapter;
import mil.nga.mapcache.view.detail.DetailPageHeaderObject;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;
import mil.nga.mapcache.view.layer.FeatureColumnDetailObject;
import mil.nga.mapcache.view.layer.FeatureColumnUtil;
import mil.nga.mapcache.view.layer.LayerPageAdapter;
import mil.nga.mapcache.view.map.BasemapApplier;
import mil.nga.mapcache.view.map.feature.FeatureViewActivity;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.mapcache.wizards.createtile.IBoundingBoxManager;
import mil.nga.mapcache.wizards.createtile.IMapView;
import mil.nga.mapcache.wizards.createtile.NewTileLayerUI;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.util.GeometryEnvelopeBuilder;
import mil.nga.sf.util.GeometryPrinter;

/**
 * GeoPackage Map Fragment
 *
 * @author osbornb
 */
public class GeoPackageMapFragment extends Fragment implements
        OnMapReadyCallback, OnMapLongClickListener, OnMapClickListener, OnMarkerClickListener,
        OnMarkerDragListener, ILoadTilesTask, IIndexerTask, OnCameraIdleListener, OnDialogButtonClickListener,
        GeoPackageModifier, IBoundingBoxManager, IMapView {

    /**
     * Max features key for saving to preferences
     */
    private static final String MAX_FEATURES_KEY = "max_features_key";

    /**
     * Key for using dark mode from preferences
     */
    private static final String SETTINGS_DARK_KEY = "dark_map";

    /**
     * Key for using app dark mode from preferences
     */
    private static final String SETTINGS_APP_DARK_KEY = "dark_app";

    /**
     * Key for zoom icons being visible from shared preferences
     */
    private static final String SETTINGS_ZOOM_KEY = "zoom_icons";

    /**
     * Key for the current zoom level being visible
     */
    private static final String SETTINGS_ZOOM_LEVEL_KEY = "zoom_level";

    /**
     * Key for max features warning message
     */
    private static final String MAX_FEATURES_MESSAGE_KEY = "max_features_warning";

    /**
     * Google map
     */
    private GoogleMap map;

    /**
     * Map loaded flag
     */
    private boolean mapLoaded = false;

    /**
     * View
     */
    private View view;

    /**
     * Edit features view
     */
    private View editFeaturesView;

    /**
     * Edit features polygon hole view
     */
    private View editFeaturesPolygonHoleView;

    /**
     * True when the map is visible
     */
    private static boolean visible = true;

    /**
     * True when the location is shown on the map.
     */
    private static boolean locationVisible = false;

    /**
     * True when we are showing bearing on the map
     */
    private static boolean bearingVisible = false;


    /**
     * Tracks the last calculated bearing from the sensors
     */
    float mCompassLastMeasuredBearing = 0.0f;

    /**
     * Last location saved from location services
     */
    Location mLastLocation;

    /**
     * Handles sensor listeners for location updates
     */
    private SensorHandler sensorHandler;

    /**
     * Callback for location updates
     */
    private LocationCallback locationCallback;

    /**
     * Update task
     */
    private MapUpdateTask updateTask;

    /**
     * Update features task
     */
    private MapFeaturesUpdateTask updateFeaturesTask;

    /**
     * Update lock for creating and cancelling update tasks
     */
    private final Lock updateLock = new ReentrantLock();

    /**
     * Vibrator
     */
    private Vibrator vibrator;

    /**
     * Touchable map layout
     */
    private TouchableMap touch;

    /**
     * Bounding box mode
     */
    private boolean boundingBoxMode = false;

    /**
     * Bounding box starting corner
     */
    private LatLng boundingBoxStartCorner = null;

    /**
     * Bounding box ending corner
     */
    private LatLng boundingBoxEndCorner = null;

    /**
     * Bounding box polygon
     */
    private Polygon boundingBox = null;

    /**
     * True when drawing a shape
     */
    private boolean drawing = false;

    /**
     * Bounding Box menu item
     */
    private MenuItem boundingBoxMenuItem;

    /**
     * Edit Features menu item
     */
    private MenuItem editFeaturesMenuItem;

    /**
     * Current zoom level
     */
    private int currentZoom = -1;

    /**
     * Edit points type
     */
    private EditType editFeatureType = null;

    /**
     * Edit type enumeration
     */
    private enum EditType {

        POINT, LINESTRING, POLYGON, POLYGON_HOLE, EDIT_FEATURE

    }

    /**
     * Map of edit point marker ids and markers
     */
    private final Map<String, Marker> editPoints = new LinkedHashMap<>();

    /**
     * Map of edit point hole marker ids and markers
     */
    private final Map<String, Marker> editHolePoints = new LinkedHashMap<>();

    /**
     * Edit feature marker
     */
    private Marker editFeatureMarker;

    /**
     * Temp Edit feature marker before validation
     */
    private Marker tempEditFeatureMarker;

    /**
     * Edit feature shape
     */
    private GoogleMapShapeMarkers editFeatureShape;

    /**
     * Edit feature shape markers for adding new points
     */
    private ShapeMarkers editFeatureShapeMarkers;

    /**
     * Edit linestring
     */
    private Polyline editLinestring;

    /**
     * Edit polygon
     */
    private Polygon editPolygon;

    /**
     * Edit hole polygon
     */
    private Polygon editHolePolygon;

    /**
     * List of hold polygons
     */
    private final List<List<LatLng>> holePolygons = new ArrayList<>();

    /**
     * Edit point button
     */
    private ImageButton editPointButton;

    /**
     * Edit linestring button
     */
    private ImageButton editLinestringButton;

    /**
     * Edit polygon button
     */
    private ImageButton editPolygonButton;

    /**
     * Edit accept button
     */
    private ImageButton editAcceptButton;

    /**
     * Edit clear button
     */
    private ImageButton editClearButton;

    /**
     * Edit polygon holes button
     */
    private ImageButton editPolygonHolesButton;

    /**
     * Edit accept button
     */
    private ImageButton editAcceptPolygonHolesButton;

    /**
     * Edit clear button
     */
    private ImageButton editClearPolygonHolesButton;

    /**
     * RecyclerView that will hold our GeoPackages
     */
    private RecyclerView geoPackageRecycler;

    /**
     * Adapter for the main RecyclerView of GeoPackages
     */
    private GeoPackageAdapter geoPackageRecyclerAdapter;

    /**
     * Adapter for showing a GeoPackage detail page
     */
    private DetailPageAdapter detailPageAdapter;

    /**
     * Util class for opening dialogs to respond to the GeoPackage detail view buttons
     */
    private DetailActionUtil detailButtonUtil;

    /**
     * Util class for opening dialogs to respond to the feature column buttons on the layer detail page
     */
    private FeatureColumnUtil featureColumnUtil;

    /**
     * ViewModel for accessing data from the repository
     */
    private GeoPackageViewModel geoPackageViewModel;

    /**
     * Button for selecting map type
     */
    private ImageButton mapSelectButton;

    /**
     * Button for editing features
     */
    private ImageButton editFeaturesButton;

    /**
     * Button for opening the settings view
     */
    private ImageButton settingsIcon;

    /**
     * Zoom in button
     */
    private ImageButton zoomInButton;

    /**
     * Zoom out button
     */
    private ImageButton zoomOutButton;

    /**
     * Zoom level label
     */
    private TextView zoomLevelText;

    /**
     * Shows the coordinates at the center of the screen.
     */
    private TextView coordText;

    /**
     * Contains the coordinates text view.
     */
    private View coordTextCard;

    /**
     * Floating Action Button for creating geoPackages
     */
    private FloatingActionButton fab;

    /**
     * Floating Action Button for new layers
     */
    private FloatingActionButton layerFab;

    /**
     * Task for importing a geoPackage
     */
    private ImportTask importTask;

    /**
     * Adapter for the LayerDetailView in the Recycler
     */
    private LayerPageAdapter layerAdapter;

    /**
     * Boolean for if we should show the max feature warning
     */
    private boolean displayMaxFeatureWarning = true;

    /**
     * Location provider for getting current location
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * A view that acts as a transparent box.  Used for laying on top of a map for the user to
     * draw a bounding box
     */
    private View transBox;

    /**
     * ShareTask object handles sharing GeoPackage files to other apps or saving to disk
     */
    private ShareTask shareTask;

    /**
     * Controls user selected base maps.
     */
    private BasemapApplier basemapApplier;

    /**
     * Used to zoom the maps position to various spots.
     */
    private Zoomer zoomer;

    /**
     * Model that contains various states involving the map.
     */
    private final MapModel model = new MapModel();

    /**
     * Activity launchers
     */
    ActivityResultLauncher<Intent> importGeoPackageActivityResultLauncher;
    ActivityResultLauncher<Intent> preferencePageActivityResultLauncher;


    /**
     * The camera move listener.
     */
    private final GoogleMap.OnCameraMoveListener moveListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            if (zoomLevelText.getVisibility() == View.VISIBLE && map != null) {
                zoomLevelText.setText(getResources().getString(
                        R.string.zoom_level,
                        map.getCameraPosition().zoom));
            }
        }
    };

    /**
     * Constructor
     */
    public GeoPackageMapFragment() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() != null) {
            geoPackageViewModel = new ViewModelProvider(getActivity()).get(GeoPackageViewModel.class);
            geoPackageViewModel.init();
            model.setActive(new GeoPackageDatabases(
                    getActivity().getApplicationContext(),
                    "active"));
            vibrator = (Vibrator) getActivity().getSystemService(
                    Context.VIBRATOR_SERVICE);
        }

        if (geoPackageViewModel != null && geoPackageViewModel.getGeos() != null) {
            GeoPackageSynchronizer.getInstance().synchronizeTables(
                    geoPackageViewModel.getGeos().getValue(),
                    model.getActive());
        }


        view = inflater.inflate(R.layout.fragment_map, container, false);
        getMapFragment().getMapAsync(this);

        touch = new TouchableMap(getActivity());
        touch.addView(view);

        // Set listeners for icons on map
        setIconListeners();

        // Set up location provider
        if (getContext() != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        }

        // Util class for launching dialogs when clicking buttons on GeoPackage detail page
        detailButtonUtil = new DetailActionUtil();

        // Util class for launching dialogs when creating/deleting feature columns on the layer detail page
        featureColumnUtil = new FeatureColumnUtil(getActivity());

        // Floating action button
        layerFab = view.findViewById(R.id.layer_fab);
        fab = view.findViewById(R.id.bottom_sheet_fab);
        setFloatingActionButton();
        setNewLayerFab();

        // Create the GeoPackage recycler view
        createGeoPackageRecycler();
        subscribeGeoPackageRecycler();

        // Show disclaimer
        showDisclaimer();

        // Draw a transparent box.  used for downloading a new tile layer
        // NOTE: This view is invisible by default
        transBox = getLayoutInflater().inflate(R.layout.transparent_box_view, null);

        // Create a ShareTask to handle sharing to other apps or saving to disk
        shareTask = new ShareTask(getActivity());

        // Set up activity launchers registered for results
        setupLaunchers();

        return touch;
    }


    /**
     * Launch the preferences activity
     */
    public void launchPreferences() {
        try {
            Intent intent = new Intent(getContext(), PreferencesActivity.class);
            preferencePageActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Set up activity launchers for results
     * (replaces startActivityForResult)
     */
    private void setupLaunchers() {
        // Import a geoPackage from file
        importGeoPackageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // Import geoPackage from file
                            ImportTask task = new ImportTask(getActivity(), data);
                            task.importFile();
                        }
                    }
                });
        // Launch preference page
        preferencePageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    settingsUpdate();
                }
        );
    }

    /**
     * Update after the settings activity is closed
     * <p>
     * Note: instead of being called in the initial onCreateView, it gets called in onMapReady, because
     * we need the map to be initialized before we can set it to dark mode
     */
    private void settingsUpdate() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        boolean darkMode = settings.getBoolean(SETTINGS_DARK_KEY, false);
        boolean appDarkMode = settings.getBoolean(SETTINGS_APP_DARK_KEY, false);
        boolean zoomIconsVisible = settings.getBoolean(SETTINGS_ZOOM_KEY, false);
        boolean zoomLevelVisible = settings.getBoolean(SETTINGS_ZOOM_LEVEL_KEY, false);
        displayMaxFeatureWarning = settings.getBoolean(MAX_FEATURES_MESSAGE_KEY, false);

        setMapDarkMode(darkMode);
        setAppDarkMode(appDarkMode);
        setZoomIconsVisible(zoomIconsVisible);
        setZoomLevelVisible(zoomLevelVisible);
        if (basemapApplier != null) {
            basemapApplier.applyBasemaps(map);
        }
    }

    /**
     * Get the boolean value for the zoom level indicator setting
     */
    public boolean isZoomLevelVisible() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        return settings.getBoolean(SETTINGS_ZOOM_LEVEL_KEY, false);

    }

    /**
     * Sets the main RecyclerView to show the list of GeoPackages by setting the adapter
     */
    private void populateRecyclerWithGeoPackages() {
        layerFab.hide();
        fab.show();
        geoPackageRecycler.setAdapter(geoPackageRecyclerAdapter);
    }

    /**
     * Sets the main RecyclerView to show the details for a selected GeoPackage
     */
    private void populateRecyclerWithDetail() {
        layerFab.show();
        fab.hide();
        if (detailPageAdapter != null) {
            geoPackageRecycler.setAdapter(detailPageAdapter);
        }
    }

    /**
     * Sets the main RecyclerView to show the details for a selected layer from the GeoPackage
     * detail page
     *
     * @param layerAdapter - A pre-populated adapter to populate with a layer's detail
     */
    private void populateRecyclerWithLayerDetail(LayerPageAdapter layerAdapter) {
        layerFab.hide();
        fab.hide();
        if (layerAdapter != null) {
            geoPackageRecycler.setAdapter(layerAdapter);
        }
    }

    /**
     * Populate the top level GeoPackage recyclerview with GeoPackage names
     */
    private void createGeoPackageRecycler() {
        geoPackageRecycler = view.findViewById(R.id.recycler_geopackages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        geoPackageRecycler.setLayoutManager(layoutManager);

        GeoPackageClickListener geoClickListener = (View view, int position, GeoPackageDatabase db) ->
                createGeoPackageDetailAdapter(db);
        // Create the adapter and set it for the recyclerview
        geoPackageRecyclerAdapter = new GeoPackageAdapter(geoClickListener);
        populateRecyclerWithGeoPackages();


        // Listener for swiping a geoPackage to the right to enable/disable all layers
        EnableAllLayersListener gpSwipeListener = (boolean active, GeoPackageDatabase db) ->
                geoPackageViewModel.setAllLayersActive(active, db);

        if (getContext() != null) {
            SwipeController controller = new SwipeController(getContext(), gpSwipeListener);
            controller.getTouchHelper().attachToRecyclerView(geoPackageRecycler);
        }
    }

    /**
     * Subscribe to populate the list of GeoPackages for the recyclerview.
     * Gets a list of GeoPackageTables and sends them to the adapter.
     * Also subscribes to the list of active tables.  When that is updated, the adapter will set
     * the active status for all GeoPackages in the RecyclerView
     */
    private void subscribeGeoPackageRecycler() {
        // Observe list of GeoPackages
        geoPackageViewModel.getGeos().observe(getViewLifecycleOwner(), newGeos -> {
            // Set the visibility of the 'no geoPackages found' message
            setListVisibility(newGeos.getDatabases().isEmpty());
            // If not empty, repopulate the list
            geoPackageRecyclerAdapter.clear();
            geoPackageRecyclerAdapter.insertDefaultHeader();
            for (GeoPackageDatabase db : newGeos.getDatabases()) {
                geoPackageRecyclerAdapter.insertToEnd(db);
            }
            geoPackageRecyclerAdapter.insertDefaultFooter();
            geoPackageRecyclerAdapter.notifyDataSetChanged();

            // Make sure the detail page is repopulated in case a new layer is added
            if (detailPageAdapter != null) {
                detailPageAdapter.updateAllTables(newGeos, model.getActive());
            }
        });

        // Observe Active Tables - used to determine which layers are enabled.  Update main list
        // of geoPackages when a change is made in order to change the active state
        geoPackageViewModel.getActive().observe(getViewLifecycleOwner(), newTables -> {
            GeoPackageSynchronizer.getInstance().synchronizeTables(model.getActive(), newTables);
            model.setActive(newTables);
            geoPackageRecyclerAdapter.updateActiveTables(newTables.getDatabases());
            geoPackageRecyclerAdapter.notifyDataSetChanged();

            // Get the total number of active features and the max features setting
            int totalFeatures = model.getActive().getAllFeaturesCount();
            int maxFeatureSetting = getMaxFeatures();
            if (totalFeatures > maxFeatureSetting) {
                showMaxFeaturesExceeded();
            }

            // if the detail page has been used, send the updated active list for it to update itself
            if (detailPageAdapter != null) {
                detailPageAdapter.updateActiveTables(model.getActive());
            }

            // if the layer detail page has been created, send the updated active list for it to update itself
            if (layerAdapter != null) {
                layerAdapter.updateActiveTables(model.getActive());
            }

            // Update the map
            if (map != null) {
                if (newTables.isEmpty()) {
                    map.clear();
                }
                updateInBackground(true);
            }
        });
    }


    /**
     * Populate the RecyclerView with details about a single GeoPackage, and generate click listeners
     * for the detail view
     *
     * @param db - GeoPackageDatabase object of the GP that we're going to create the view for
     */
    private void createGeoPackageDetailAdapter(GeoPackageDatabase db) {
        if (db != null) {
            // Listener for clicking on Layer
            DetailLayerClickListener layerListener = this::createGeoPackageLayerDetailAdapter;

            // Listener for clicking on Layer's active switch.  Sends the table and active state to the
            // repository to be stored in the active tables list
            LayerActiveSwitchListener activeLayerListener = (boolean active, GeoPackageTable table) ->
                    geoPackageViewModel.setLayerActive(table);

            // Listener for clicking the enable all switch for enabling all layers
            EnableAllLayersListener enableAllListener = (boolean active, GeoPackageDatabase geopackage) ->
                    geoPackageViewModel.setAllLayersActive(active, geopackage);

            // Listener to forward a button click on the detail header to the appropriate dialog function
            // Note: Layer name will be empty string for the GeoPackage detail page
            DetailActionListener detailActionListener = (View view, int actionType, String gpName, String layerName) ->
                    openActionDialog(gpName, layerName, actionType);

            // Click listener for the back arrow on the detail header.  Resets the RecyclerView to
            // show GeoPackages
            View.OnClickListener detailBackListener = (View view) -> populateRecyclerWithGeoPackages();

            // Generate a list to pass to the adapter.  Should contain:
            // - A header: DetailPageHeaderObject
            // - N number of DetailPageLayerObject objects generated from the GeoPackageDatabase object
            DetailPageHeaderObject detailHeader = new DetailPageHeaderObject(db);
            List<Object> detailList = new ArrayList<>();
            detailList.add(detailHeader);
            detailList.addAll(db.getLayerObjects(model.getActive().getDatabase(db.getDatabase())));

            detailPageAdapter = new DetailPageAdapter(detailList, layerListener,
                    detailBackListener, detailActionListener, activeLayerListener, enableAllListener, db);
            populateRecyclerWithDetail();
        }
    }


    /**
     * Create a view adapter to populate the RecyclerView with a Layer detail view (used when
     * clicking a Layer row from the GP Detail page)
     */
    private void createGeoPackageLayerDetailAdapter(DetailPageLayerObject layerObject) {

        // Click listener for the back arrow on the layer page.  Resets the RecyclerView to
        // show the previous GeoPackage Detail view
        View.OnClickListener detailBackListener = (View view) -> populateRecyclerWithDetail();

        // Listener for clicking on Layer's active switch.  Sends the table and active state to the
        // repository to be stored in the active tables list
        LayerActiveSwitchListener activeLayerListener = (boolean active, GeoPackageTable table) ->
                geoPackageViewModel.setLayerActive(table);

        // (Delete) Listener to forward a button click layer detail page to the appropriate dialog function
        DetailActionListener detailActionListener = (View view, int actionType, String gpName, String layerName) ->
                openActionDialog(gpName, layerName, actionType);

        // Listener for editing feature columns on the layer detail page
        FeatureColumnListener featureColumnListener = (View view, int actionType, FeatureColumnDetailObject columnDetailObject) ->
                openFeatureColumnDialog(columnDetailObject, actionType);

        List<Object> layerDetailObjects = new ArrayList<>();
        layerDetailObjects.add(layerObject);
        for (FeatureColumn fc : layerObject.getFeatureColumns()) {
            // Default values of 'id' and 'geom' shouldn't be passed along
            if (!fc.getName().equalsIgnoreCase("id") &&
                    !fc.getName().equalsIgnoreCase("geom")) {
                FeatureColumnDetailObject fcDetailObject = new FeatureColumnDetailObject(fc.getName(),
                        fc.getDataType(), layerObject.getGeoPackageName(), layerObject.getName());
                layerDetailObjects.add(fcDetailObject);
            }
        }
        layerAdapter = new LayerPageAdapter(layerDetailObjects, detailBackListener,
                activeLayerListener, detailActionListener, featureColumnListener);
        populateRecyclerWithLayerDetail(layerAdapter);
    }


    /**
     * Ask the DetailButtonUtil to open a dialog to complete the action related to the button
     * that was clicked
     *
     * @param gpName     GeoPackage name
     * @param layerName  Name of the layer to delete (will be empty string for anything but
     *                   DELETE_LAYER action
     * @param actionType ActionType enum
     */
    private void openActionDialog(String gpName, String layerName, int actionType) {
        if (actionType == DetailActionListener.DETAIL_GP) {
            detailButtonUtil.openDetailDialog(gpName, this);
        } else if (actionType == DetailActionListener.RENAME_GP) {
            detailButtonUtil.openRenameDialog(getActivity(), gpName, this);
        } else if (actionType == DetailActionListener.SHARE_GP) {
            detailButtonUtil.openShareDialog(gpName, this);
        } else if (actionType == DetailActionListener.COPY_GP) {
            detailButtonUtil.openCopyDialog(getActivity(), gpName, this);
        } else if (actionType == DetailActionListener.DELETE_GP) {
            detailButtonUtil.openDeleteDialog(getActivity(), gpName, this);
        } else if (actionType == DetailActionListener.DELETE_LAYER) {
            detailButtonUtil.openDeleteLayerDialog(getActivity(), gpName, layerName, this);
        } else if (actionType == DetailActionListener.RENAME_LAYER) {
            detailButtonUtil.openRenameLayerDialog(getActivity(), gpName, layerName, this);
        } else if (actionType == DetailActionListener.COPY_LAYER) {
            detailButtonUtil.openCopyLayerDialog(getActivity(), gpName, layerName, this);
        } else if (actionType == DetailActionListener.ADD_FEATURE_COLUMN) {
            detailButtonUtil.openAddFieldDialog(getActivity(), gpName, layerName, this);
        } else if (actionType == DetailActionListener.EDIT_FEATURES) {
            // Open edit features mode with the geopackage and layer already selected
            openEditFeatures(gpName, layerName);
        }
    }

    /**
     * Ask the FeatureColumnUtil to open a dialog to complete the action related to the button
     * that was clicked
     *
     * @param columnDetailObject object containing feature column details
     * @param actionType         ActionType enum
     */
    private void openFeatureColumnDialog(FeatureColumnDetailObject columnDetailObject,
                                         int actionType) {
        if (actionType == FeatureColumnListener.DELETE_FEATURE_COLUMN) {
            featureColumnUtil.openDeleteDialog(getActivity(), columnDetailObject, this);
        }
    }

    /**
     * Implement OnDialogButtonClickListener Detail button confirm click
     * Open a dialog with the GeoPackages advanced details
     *
     * @param gpName - GeoPackage name
     */
    @Override
    public void onDetailGP(String gpName) {
        AlertDialog viewDialog = geoPackageViewModel.getGeoPackageDetailDialog(gpName, getActivity());
        viewDialog.show();
    }

    /**
     * Implement OnDialogButtonClickListener Rename button confirm click
     * Rename a GeoPackage and recreate the detail view adapter to make it refresh
     *
     * @param oldName - GeoPackage original name
     * @param newName - New GeoPackage name
     */
    @Override
    public void onRenameGP(String oldName, String newName) {
        Log.i("click", "Rename GeoPackage from: " + oldName + " to: " + newName);
        try {
            // If the new name already exists, make sure the names match, meaning that
            // this is just renaming the same geopackage
            if (geoPackageViewModel.geoPackageNameExists(newName) &&
                    !oldName.equalsIgnoreCase(newName)) {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_rename_label),
                        "GeoPackage name \"" + newName
                                + "\" is already taken");
            } else {
                if (geoPackageViewModel.setGeoPackageName(oldName, newName)) {
                    // recreate the adapter and repopulate the recyclerview
                    createGeoPackageDetailAdapter(geoPackageViewModel.getGeoByName(newName));
                } else {
                    GeoPackageUtils.showMessage(getActivity(),
                            getString(R.string.geopackage_rename_label),
                            "Rename from " + oldName + " to " + newName
                                    + " was not successful");
                }
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.geopackage_rename_label),
                    e.getMessage());
        }
    }

    /**
     * Implement OnDialogButtonClickListener Share button confirm click
     * Kick off a share task with this GeoPackage
     * Menu to either share externally or save the file
     *
     * @param gpName - GeoPackage name to be saved
     */
    @Override
    public void onShareGP(String gpName) {
        // Set the geopackage name before we ask permissions and get routed back through MainActivity
        // to exportGeoPackageToExternal()
        shareTask.setGeoPackageName(gpName);
        getImportPermissions(MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE);
    }

    /**
     * Implement OnDialogButtonClickListener Copy button confirm click
     * Copy a GeoPackage in the repository and replace the recyclerview with the geoPackages list
     *
     * @param gpName - GeoPackage name
     */
    @Override
    public void onCopyGP(String gpName, String newName) {
        Log.i("click", "Copy Geopackage");
        try {
            if (geoPackageViewModel.copyGeoPackage(gpName, newName)) {
                populateRecyclerWithGeoPackages();
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_copy_label), "Copy from "
                                + gpName + " to " + newName + " was not successful");
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.geopackage_copy_label),
                    e.getMessage());
        }
    }

    /**
     * Implement OnDialogButtonClickListener Delete button confirm click
     * Delete GeoPackage from the repository which should trigger an update to our views
     *
     * @param gpName - GeoPackage name
     */
    @Override
    public void onDeleteGP(String gpName) {
        // remove any active layers drawn on map
        geoPackageViewModel.removeActiveTableLayers(gpName);
        // Delete the geopackage and take us back to the GeoPackage list
        if (geoPackageViewModel.deleteGeoPackage(gpName)) {
            populateRecyclerWithGeoPackages();
        }
    }

    /**
     * Ask the viewModel to remove the given layer name from the given GeoPackage name
     *
     * @param gpName    GeoPackage name
     * @param layerName Layer name to delete
     */
    @Override
    public void onDeleteLayer(String gpName, String layerName) {
        // First remove it from the active layers
        geoPackageViewModel.removeActiveLayer(gpName, layerName);
        // Ask the repository to delete the layer
        geoPackageViewModel.removeLayerFromGeo(gpName, layerName,
                GeoPackageMapFragment.this);
    }

    /**
     * Callback after onDeleteLayer asks the viewModel to delete the layer
     *
     * @param geoPackageName The name of the changed geoPackage.
     */
    @Override
    public void onLayerDeleted(String geoPackageName) {
        GeoPackageDatabase newDb = geoPackageViewModel.getGeoByName(geoPackageName);
        createGeoPackageDetailAdapter(newDb);
    }

    /**
     * Ask the view model to rename a layer in the given geopackage
     */
    public void onRenameLayer(String gpName, String layerName, String newLayerName) {
        // First remove it from the active layers
        geoPackageViewModel.removeActiveLayer(gpName, layerName);
        GeoPackageDatabase db = geoPackageViewModel.renameLayer(gpName, layerName, newLayerName);
        if (db != null) {

            //TODO: Don't generate the layer detail object out of the returned object from rename layer,
            // Instead go find it again in the current geos list and generate it
            GeoPackageDatabase newDb = geoPackageViewModel.getGeoByName(gpName);

//            createGeoPackageDetailAdapter(db);
            DetailPageLayerObject newLayerObject = newDb.getLayerObject(model.getActive().getDatabase(gpName), gpName, newLayerName);
            if (newLayerObject != null)
                createGeoPackageLayerDetailAdapter(newLayerObject);
        }
    }

    /**
     * Ask the view model to copy a layer in a given geopackage
     */
    public void onCopyLayer(String gpName, String oldLayer, String newLayerName) {
        Log.i("click", "Copy Layer");
        try {
            if (geoPackageViewModel.copyLayer(gpName, oldLayer, newLayerName)) {
                Toast.makeText(getActivity(), "Layer copied", Toast.LENGTH_SHORT).show();

            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_copy_label), "Copy from "
                                + gpName + " to " + newLayerName + " was not successful");
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.geopackage_copy_label),
                    e.getMessage());
        }
    }

    /**
     * Ask the view model to create a new layer feature column
     */
    public void onAddFeatureField(String gpName, String layerName, String fieldName,
                                  GeoPackageDataType type) {
        try {
            if (geoPackageViewModel.createFeatureColumnLayer(gpName, layerName, fieldName, type)) {
                GeoPackageDatabase newDb = geoPackageViewModel.getGeoByName(gpName);
                DetailPageLayerObject newLayerObject = newDb.getLayerObject(model.getActive().getDatabase(gpName), gpName, layerName);
                if (newLayerObject != null)
                    createGeoPackageLayerDetailAdapter(newLayerObject);
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.new_feature_column_label), "Creating new Feature Column in "
                                + " " + layerName + " was not successful");
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.new_feature_column_label),
                    e.getMessage());
        }
    }

    /**
     * Remove a Feature Column from a layer via the view model
     */
    public void onDeleteFeatureColumn(String gpName, String layerName, String columnName) {
        try {
            if (geoPackageViewModel.deleteFeatureColumnLayer(gpName, layerName, columnName)) {
                GeoPackageDatabase newDb = geoPackageViewModel.getGeoByName(gpName);
                DetailPageLayerObject newLayerObject = newDb.getLayerObject(model.getActive().getDatabase(gpName), gpName, layerName);
                if (newLayerObject != null)
                    createGeoPackageLayerDetailAdapter(newLayerObject);
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.delete_feature_column_label), "Delete Feature Column in "
                                + " " + layerName + " was not successful");
            }
        } catch (Exception e) {
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.delete_feature_column_label),
                    e.getMessage());
        }
    }

    /**
     * Implement OnDialogButtonClickListener Cancel button click
     */
    @Override
    public void onCancelButtonClicked() {
        Log.i("click", "close clicked");
    }


    /**
     * Pop up menu for map view type icon button - selector for map, satellite, terrain
     */
    public void openMapSelect() {
        if (getActivity() != null) {
            PopupMenu pm = new PopupMenu(getActivity(), mapSelectButton);
            // Needed to make the icons visible
            try {
                Method method = pm.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                //method.setAccessible(true);
                method.invoke(pm.getMenu(), true);
            } catch (Exception e) {
                Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
            }

            pm.getMenuInflater().inflate(R.menu.popup_map_type, pm.getMenu());
            MenuCompat.setGroupDividerEnabled(pm.getMenu(), true);
            pm.setOnMenuItemClickListener((MenuItem item) -> {
                if (item.getItemId() == R.id.map) {
                    setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                } else if (item.getItemId() == R.id.satellite) {
                    setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                } else if (item.getItemId() == R.id.terrain) {
                    setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                } else if (item.getItemId() == R.id.NoGrid) {
                    setGridType(GridType.NONE);
                    return true;
                } else if (item.getItemId() == R.id.GARSGrid) {
                    setGridType(GridType.GARS);
                    return true;
                } else if (item.getItemId() == R.id.MGRSGrid) {
                    setGridType(GridType.MGRS);
                    return true;
                }

                return true;
            });
            pm.show();
        }
    }


    /**
     * Pop up menu for editing geoPackage - drawing features, bounding box, etc
     */
    public void openEditMenu() {
        if (getActivity() != null) {
            PopupMenu pm = new PopupMenu(getActivity(), editFeaturesButton);
            // Needed to make the icons visible
            try {
                Method method = pm.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                //method.setAccessible(true);
                method.invoke(pm.getMenu(), true);
            } catch (Exception e) {
                Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
            }

            pm.getMenuInflater().inflate(R.menu.popup_edit_menu, pm.getMenu());

            // Set text for edit features mode
            MenuItem editFeaturesItem = pm.getMenu().findItem(R.id.features);
            if (model.isEditFeaturesMode()) {
                editFeaturesItem.setTitle("Stop editing");
            } else {
                editFeaturesItem.setTitle("Edit Features");
            }

            // Set text for show/hide my location based on current visibility
            MenuItem showHideOption = pm.getMenu().findItem(R.id.showMyLocation);
            if (locationVisible) {
                showHideOption.setTitle("Hide my location");
            } else {
                showHideOption.setTitle("Show my location");
            }

            // Set text for show/hide my bearing based on current visibility
            MenuItem showBearing = pm.getMenu().findItem(R.id.showBearing);
            if (bearingVisible) {
                showBearing.setTitle("Hide Bearing");
            } else {
                showBearing.setTitle("Show Bearing");
            }

            int totalFeaturesAndTiles = model.getActive().getAllFeaturesAndTilesCount();
            if (totalFeaturesAndTiles == 0) {
                MenuItem zoomToActive = pm.getMenu().findItem(R.id.zoomToActive);
                zoomToActive.setEnabled(false);
            }
            pm.setOnMenuItemClickListener((MenuItem item) -> {
                if (item.getItemId() == R.id.zoomToActive) {
                    zoomer.zoomToActive();
                    return true;
                } else if (item.getItemId() == R.id.features) {
                    editFeaturesMenuItem = item;
                    if (!model.isEditFeaturesMode()) {
                        selectEditFeatures();
                    } else {
                        resetEditFeatures();
                        updateInBackground(false);
                    }
                    return true;
                } else if (item.getItemId() == R.id.boundingBox) {
                    boundingBoxMenuItem = item;
                    if (!boundingBoxMode) {

                        if (model.isEditFeaturesMode()) {
                            resetEditFeatures();
                            updateInBackground(false);
                        }

                        boundingBoxMode = true;
                    } else {
                        resetBoundingBox();
                    }
                    return true;
                } else if (item.getItemId() == R.id.maxFeatures) {
                    setMaxFeatures();
                    return true;
                } else if (item.getItemId() == R.id.clearAllActive) {
                    clearAllActive();
                    return true;
                } else if (item.getItemId() == R.id.showMyLocation) {
                    showMyLocation();
                    return true;
                } else if (item.getItemId() == R.id.showBearing) {
                    setMapBearing();
                    return true;
                }

                return true;
            });
            pm.show();
        }
    }


    /**
     * Zoom in on map
     */
    public void zoomIn() {
        if (map == null) return;
        map.animateCamera(CameraUpdateFactory.zoomIn());
    }


    /**
     * Zoom out on map
     */
    public void zoomOut() {
        if (map == null) return;
        map.animateCamera(CameraUpdateFactory.zoomOut());
    }

    /**
     * Toggles the "show my location" setting.  Turns the location on / off, then zooms
     */
    private void showMyLocation() {
        locationVisible = !locationVisible;
        // If my location did not have permissions to update and the map is becoming visible, ask for permission
        if (!setMyLocationEnabled() && locationVisible && getActivity() != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(R.string.location_access_rational_title)
                        .setMessage(R.string.location_access_rational_message)
                        .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                        )
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        // Only zoom when turning location on, not when hiding it
        if (locationVisible) {
            zoomToMyLocation();
        }
    }

    /**
     * Gets current location from fused location provider and zooms to that location
     */
    private void zoomToMyLocation() {
        if (getContext() != null && getActivity() != null) {
            // Verify permissions first
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), (Location location) -> {
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                }
            }).addOnFailureListener((@NonNull @NotNull Exception e) ->
                    Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e));
        }
    }


    /**
     * Handler to either show map bearing or stop updates
     */
    private void setMapBearing() {
        if (bearingVisible) {
            stopMapBearing();
        } else {
            showMapBearing();
        }
    }


    /**
     * Enable map bearing compass
     */
    private void showMapBearing() {
        if (getContext() != null && getActivity() != null) {
            // Verify permissions first
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

            // Callback to move the camera every time the handler gets a sensor update
            SensorCallback sensorCallback = (SensorEvent event, float bearing) -> {
                mCompassLastMeasuredBearing = bearing;
                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    float zoom = map.getCameraPosition().zoom;
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)             // Sets the center of the map to current location
                            .zoom(zoom)                   // Sets the zoom
                            .bearing(bearing)           // Sets the orientation of the camera
                            .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                            .build();                   // Creates a CameraPosition from the builder

                    //move map camera
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            };

            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 12000)
                    .setWaitForAccurateLocation(false)
                    .setMaxUpdateDelayMillis(100000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    //The last location in the list is the newest
                    mLastLocation = locationResult.getLastLocation();
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            sensorHandler = new SensorHandler(sensorCallback, getContext());
            bearingVisible = !bearingVisible;
        }
    }


    /**
     * Stop the map bearing view, then zoom out and center
     */
    private void stopMapBearing() {
        bearingVisible = !bearingVisible;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        sensorHandler.stopUpdates();
        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to current location
                    .zoom(13)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera
                    .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                    .build();                   // Creates a CameraPosition from the builder

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
        }
    }


    /**
     * Set Floating action button to open the create new geopackage wizard
     */
    private void setFloatingActionButton() {
        fab.setOnClickListener((View view) -> createNewWizard());
    }

    /**
     * Set Floating action button to create new layers
     */
    private void setNewLayerFab() {
        layerFab.setOnClickListener((View view) -> {
            String geoName = detailPageAdapter.getGeoPackageName();
            if (geoName != null) {
                newLayerWizard();
            }
        });
    }


    /**
     * Sets the visibility of the recycler view vs "no geoPackages found" message bases on the
     * recycler view being empty
     */
    private void setListVisibility(boolean empty) {
        LinearLayout emptyViewHolder = view.findViewById(R.id.empty_list_holder);
        TextView getStartedView = view.findViewById(R.id.geo_get_started);

        // Give the get started message a listener
        getStartedView.setOnClickListener((View view) -> createNewWizard());

        // Set the visibility
        if (empty) {
            emptyViewHolder.setVisibility(View.VISIBLE);
            BottomSheetBehavior<RecyclerView> behavior = BottomSheetBehavior.from(geoPackageRecycler);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            emptyViewHolder.setVisibility(View.GONE);
            geoPackageRecycler.setVisibility(View.VISIBLE);

        }
    }


    /**
     * Creates listeners for map icon buttons
     */
    public void setIconListeners() {
        // Create listeners for map view icon button
        setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapSelectButton = view.findViewById(R.id.mapTypeIcon);
        mapSelectButton.setOnClickListener((View v) -> openMapSelect());

        // Edit icon for editing features
        editFeaturesButton = view.findViewById(R.id.editFeaturesIcon);
        editFeaturesButton.setOnClickListener((View v) -> openEditMenu());

        zoomInButton = view.findViewById(R.id.zoomInIcon);
        zoomInButton.setOnClickListener((View v) -> zoomIn());

        zoomLevelText = view.findViewById(R.id.zoomLevelText);
        coordText = view.findViewById(R.id.coordText);
        coordTextCard = view.findViewById(R.id.coordTextCard);

        zoomOutButton = view.findViewById(R.id.zoomOutIcon);
        zoomOutButton.setOnClickListener((View v) -> zoomOut());

        settingsIcon = view.findViewById(R.id.settingsIcon);
        settingsIcon.setOnClickListener((View v) -> launchPreferences());

    }


    /**
     * Disclaimer popup
     */
    private void showDisclaimer() {
        if (getActivity() != null) {
            // Only show it if the user hasn't already accepted it before
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean disclaimerPref = sharedPreferences.getBoolean(getString(R.string.disclaimerPref), false);
            if (!disclaimerPref) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View disclaimerView = inflater.inflate(R.layout.disclaimer_window, null);
                Button acceptButton = disclaimerView.findViewById(R.id.accept_button);
                Button exitButton = disclaimerView.findViewById(R.id.exit_button);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setView(disclaimerView);
                final AlertDialog alertDialog = dialogBuilder.create();
                acceptButton.setOnClickListener((View view) -> {
                    sharedPreferences.edit().putBoolean(getString(R.string.disclaimerPref), true).apply();
                    alertDialog.dismiss();
                });
                exitButton.setOnClickListener((View view) -> getActivity().finish());

                // Prevent the dialog from closing when clicking outside the dialog or the back button
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setOnKeyListener((DialogInterface arg0, int keyCode,
                                              KeyEvent event) -> true);
                alertDialog.show();
            }
        }
    }


    /**
     * Show a warning that the user has selected more features than the current max features setting
     */
    private void showMaxFeaturesExceeded() {
        if (getActivity() != null) {
            // First check the settings to see if they disabled the message
            if (displayMaxFeatureWarning) {

                // Create Alert window with basic input text layout
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
                // Logo and title
                ImageView alertLogo = alertView.findViewById(R.id.alert_logo);
                alertLogo.setBackgroundResource(R.drawable.material_info);
                TextView titleText = alertView.findViewById(R.id.alert_title);
                titleText.setText(R.string.max_features);

                // Alert message
                final TextInputEditText inputName = alertView.findViewById(R.id.edit_text_input);
                inputName.setVisibility(View.GONE);
                TextView message = alertView.findViewById(R.id.alert_description);
                message.setText(R.string.max_features_message);
                message.setVisibility(View.VISIBLE);

                CheckBox dontShowAgain = alertView.findViewById(R.id.warn_again);
                dontShowAgain.setVisibility(View.VISIBLE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setView(alertView)
                        .setPositiveButton(getString(R.string.button_ok_label),
                                (DialogInterface d, int whichButton) -> {
                                    if (dontShowAgain.isChecked()) {
                                        // Update the preference for showing this message in the future
                                        SharedPreferences settings = PreferenceManager
                                                .getDefaultSharedPreferences(getActivity());
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putBoolean(MAX_FEATURES_MESSAGE_KEY, !dontShowAgain.isChecked());
                                        editor.apply();
                                        settingsUpdate();
                                    }
                                    d.cancel();
                                });
                dialog.show();
            }
        }
    }


    /**
     * Create wizard for Import or Create GeoPackage
     */
    private void createNewWizard() {
        if (getActivity() != null) {
            // Create Alert window with basic input text layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View alertView = inflater.inflate(R.layout.new_geopackage_wizard, null);
            ViewAnimation.setScaleAnimatiom(alertView, 200);
            // title
            TextView titleText = alertView.findViewById(R.id.alert_title);
            titleText.setText(R.string.new_geopackage);

            // Initial dialog asking for create or import
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setView(alertView);
            final AlertDialog alertDialog = dialog.create();

            // Click listener for "Create New"
            alertView.findViewById(R.id.new_wizard_create_card)
                    .setOnClickListener((View v) -> {
                        createGeoPackage();
                        alertDialog.dismiss();
                    });

            // Click listener for "Import URL"
            alertView.findViewById(R.id.new_wizard_download_card)
                    .setOnClickListener((View v) -> {
                        importGeopackageFromUrl();
                        alertDialog.dismiss();
                    });

            // Click listener for "Import from file"
            alertView.findViewById(R.id.new_wizard_file_card)
                    .setOnClickListener((View v) -> {
                        getImportPermissions(MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL);
                        alertDialog.dismiss();
                    });

            alertDialog.show();
        }
    }


    /**
     * Create a new GeoPackage
     */
    private void createGeoPackage() {
        if (getActivity() != null) {
            // Create Alert window with basic input text layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
            // Logo and title
            ImageView alertLogo = alertView.findViewById(R.id.alert_logo);
            alertLogo.setBackgroundResource(R.drawable.material_add_box);
            TextView titleText = alertView.findViewById(R.id.alert_title);
            titleText.setText(R.string.create_geopackage_full);
            // GeoPackage name
            final TextInputEditText inputName = alertView.findViewById(R.id.edit_text_input);
            inputName.setSingleLine(true);
            inputName.setImeOptions(EditorInfo.IME_ACTION_DONE);

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setView(alertView)
                    .setPositiveButton(getString(R.string.button_create_label),
                            (DialogInterface d, int whichButton) -> {
                                String value = inputName.getText() != null ? inputName.getText().toString() : null;
                                if (value != null && !value.isEmpty()) {
                                    try {
                                        if (!geoPackageViewModel.createGeoPackage(value)) {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            getActivity(),
                                                            getString(R.string.geopackage_create_label),
                                                            "Failed to create GeoPackage: "
                                                                    + value);
                                        }
                                    } catch (Exception e) {
                                        GeoPackageUtils.showMessage(
                                                getActivity(), "Create "
                                                        + value, e.getMessage());
                                    }
                                }
                            })
                    .setNegativeButton(getString(R.string.button_discard_label),
                            (DialogInterface d, int whichButton) -> d.cancel());

            dialog.show();
        }
    }


    /**
     * Pop up dialog for creating a new feature or tile layer from the geopackage detail view FAB
     */
    public void newLayerWizard() {
        if (getActivity() != null) {
            // Create Alert window with basic input text layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View alertView = inflater.inflate(R.layout.new_layer_wizard, null);
            // Logo and title
            ImageView closeLogo = alertView.findViewById(R.id.new_layer_close_logo);
            closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
            TextView titleText = alertView.findViewById(R.id.new_layer_title);
            titleText.setText(R.string.new_geopackage_layer);

            // Initial dialog asking for create or import
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setView(alertView);
            final AlertDialog alertDialog = dialog.create();

            // Click listener for close button
            closeLogo.setOnClickListener((View v) -> alertDialog.dismiss());

            // Listener for create features
            TextView createFeature = alertView.findViewById(R.id.create_feature);
            createFeature.setOnClickListener((View v) -> {
                createFeatureOption();
                alertDialog.dismiss();
            });

            // Listener for create tiles
            TextView createTile = alertView.findViewById(R.id.create_tile);
            createTile.setOnClickListener((View v) -> {
                String geoName = detailPageAdapter.getGeoPackageName();
                if (geoName != null) {
                    newTileLayerWizard(geoName);
                }
                alertDialog.dismiss();
            });


            alertDialog.show();
        }
    }


    /**
     * Create feature layer menu
     */
    private void createFeatureOption() {
        if (getActivity() != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View createFeaturesView = inflater.inflate(R.layout.create_features,
                    null);
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            dialog.setView(createFeaturesView);

            final EditText nameInput = createFeaturesView
                    .findViewById(R.id.create_features_name_input);
            final EditText minLatInput = createFeaturesView
                    .findViewById(R.id.bounding_box_min_latitude_input);
            final EditText maxLatInput = createFeaturesView
                    .findViewById(R.id.bounding_box_max_latitude_input);
            final EditText minLonInput = createFeaturesView
                    .findViewById(R.id.bounding_box_min_longitude_input);
            final EditText maxLonInput = createFeaturesView
                    .findViewById(R.id.bounding_box_max_longitude_input);
            final TextView preloadedLocationsButton = createFeaturesView
                    .findViewById(R.id.bounding_box_preloaded);
            final Spinner geometryTypeSpinner = createFeaturesView
                    .findViewById(R.id.create_features_geometry_type);

            GeoPackageUtils
                    .prepareBoundingBoxInputs(getActivity(), minLatInput,
                            maxLatInput, minLonInput, maxLonInput,
                            preloadedLocationsButton);

            dialog.setPositiveButton(
                    getString(R.string.geopackage_create_features_label),
                    (DialogInterface d, int id) -> {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_features_name_label)
                                                + " is required");
                            }
                            double minLat = Double.parseDouble(minLatInput
                                    .getText().toString());
                            double maxLat = Double.parseDouble(maxLatInput
                                    .getText().toString());
                            double minLon = Double.parseDouble(minLonInput
                                    .getText().toString());
                            double maxLon = Double.parseDouble(maxLonInput
                                    .getText().toString());

                            if (minLat > maxLat) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_latitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_latitude_label));
                            }

                            if (minLon > maxLon) {
                                throw new GeoPackageException(
                                        getString(R.string.bounding_box_min_longitude_label)
                                                + " can not be larger than "
                                                + getString(R.string.bounding_box_max_longitude_label));
                            }

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    minLat, maxLon, maxLat);

                            GeometryType geometryType = GeometryType
                                    .fromName(geometryTypeSpinner
                                            .getSelectedItem().toString());
                            String geoName = detailPageAdapter.getGeoPackageName();
                            if (geoName != null) {
                                if (!geoPackageViewModel.createFeatureTable(geoName, boundingBox, geometryType, tableName)) {
                                    GeoPackageUtils
                                            .showMessage(
                                                    getActivity(),
                                                    getString(R.string.geopackage_create_features_label),
                                                    "There was a problem generating a tile table");
                                }
                            }


                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_features_label),
                                            e.getMessage());
                        }
                    }).setNegativeButton(getString(R.string.button_cancel_label),
                    (DialogInterface d, int id) -> d.cancel());
            dialog.show();
        }
    }


    /**
     * Animate and hide the map buttons and new layer FAB during new layer wizard
     */
    public void hideMapIcons() {
        ViewAnimation.rotateFadeOut(editFeaturesButton, 200);
        ViewAnimation.rotateFadeOut(settingsIcon, 200);
        layerFab.hide();
    }

    /**
     * Animate and show the map buttons and new layer FAB during new layer wizard
     */
    public void showMapIcons() {
        ViewAnimation.rotateFadeIn(editFeaturesButton, 200);
        ViewAnimation.rotateFadeIn(settingsIcon, 200);
        layerFab.show();
    }

    /**
     * Launches a wizard to create a new tile layer in the given geopackage
     *
     * @param geopackageName The name of the geoPackage.
     */
    private void newTileLayerWizard(final String geopackageName) {
        NewTileLayerUI newTileLayerUI = new NewTileLayerUI(geoPackageRecycler, this,
                this, getActivity(), getContext(), this,
                geoPackageViewModel, this, geopackageName);
        newTileLayerUI.show();
    }

    /**
     * Make sure we have permissions to read/write to external before importing.  The result will
     * send MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL or MANAGER_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE
     * back up to main activity, and should call importGeopackageFromFile or exportGeoPackageToExternal
     */
    private void getImportPermissions(int returnCode) {
        if (getActivity() != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(R.string.storage_access_rational_title)
                        .setMessage(R.string.storage_access_rational_message)
                        .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, returnCode)
                        )
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, returnCode);
            }
        }
    }


    /**
     * Import a GeoPackage from a file (after we've been given permission)
     */
    public void importGeopackageFromFile() {
        try {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("application/octet-stream");
            Intent intent = Intent.createChooser(chooseFile,
                    "Choose a GeoPackage file");
            importGeoPackageActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(GeoPackageMapFragment.class.getSimpleName(), e.getMessage(), e);
        }
    }


    /**
     * Save a GeoPackage to external disk (after we've been given permission)
     */
    public void exportGeoPackageToExternal() {
        if (shareTask != null && shareTask.getGeoPackageName() != null) {
            shareTask.askToSaveOrShare(shareTask.getGeoPackageName());
        }
    }


    /**
     * Clear all active layers from the map and zoom out 1 level
     */
    private void clearAllActive() {
        geoPackageViewModel.clearAllActive();
        zoomOut();
    }


    /**
     * Import a GeoPackage from a URL
     */
    private void importGeopackageFromUrl() {
        if (getActivity() != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View importUrlView = inflater.inflate(R.layout.import_url, null);
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            dialog.setView(importUrlView);

            // Set example url links
            ((TextView) importUrlView.findViewById(R.id.import_url_web1)).setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView) importUrlView.findViewById(R.id.import_url_web2)).setMovementMethod(LinkMovementMethod.getInstance());

            // Text validation
            final TextInputLayout inputLayoutName = importUrlView.findViewById(R.id.import_url_name_layout);
            final TextInputLayout inputLayoutUrl = importUrlView.findViewById(R.id.import_url_layout);
            final TextInputEditText inputName = importUrlView.findViewById(R.id.import_url_name_input);
            final TextInputEditText inputUrl = importUrlView.findViewById(R.id.import_url_input);

            // Listen for text changes in the name input.  This will clear error messages when the user types
            TextWatcher inputNameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    inputLayoutName.setErrorEnabled(false);
                    validateInput(inputLayoutName, inputName, false);
                }
            };
            inputName.addTextChangedListener(inputNameWatcher);

            // Listen for text changes in the url input.  This will clear error messages when the user types
            TextWatcher inputUrlWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    inputLayoutUrl.setErrorEnabled(false);
                    validateInput(inputLayoutUrl, inputUrl, true);
                }
            };
            inputUrl.addTextChangedListener(inputUrlWatcher);

            // Example GeoPackages link handler
            importUrlView.findViewById(R.id.import_examples)
                    .setOnClickListener((View v) -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(), android.R.layout.select_dialog_item);
                        // Download sample geopackages from our github server, and combine that list
                        // with our own locally provided preloaded geopackages
                        SampleDownloader sampleDownloader = new SampleDownloader(getActivity(), adapter);
                        sampleDownloader.loadLocalGeoPackageSamples();
                        sampleDownloader.getExampleData(getString(R.string.sample_geopackage_url));
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity(), R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(getString(R.string.import_url_preloaded_label));
                        builder.setAdapter(adapter,
                                (DialogInterface d, int item) -> {
                                    if (item >= 0) {
                                        String name = adapter.getItem(item);
                                        inputName.setText(name);
                                        inputUrl.setText(sampleDownloader.getSampleList().get(name));
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    });

            dialog.setPositiveButton(getString(R.string.geopackage_import_label),
                    (DialogInterface d, int id) -> {
                        // This will be overridden by click listener after show is called
                    }).setNegativeButton(getString(R.string.button_cancel_label),
                    (DialogInterface d, int id) -> d.cancel());

            final AlertDialog alertDialog = dialog.create();
            alertDialog.show();

            // Override the positive click listener to enable validation
            Button downloadButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            downloadButton.setOnClickListener((View v) -> {

                // Validate input on both fields
                boolean nameValid = validateInput(inputLayoutName, inputName, false);
                boolean urlValid = validateInput(inputLayoutUrl, inputUrl, true);

                if (nameValid && urlValid) {
                    String database = inputName.getText() != null ? inputName.getText().toString() : "";
                    String url = inputUrl.getText() != null ? inputUrl.getText().toString() : "";
                    DownloadTask downloadTask = new DownloadTask(database, url, getActivity());

                    downloadTask.execute();
                    alertDialog.dismiss();
                } else if (!nameValid) {
                    inputName.requestFocus();
                } else {
                    inputUrl.requestFocus();
                }
            });
        }
    }


    /**
     * Initiate an Import task (received from intent outside of application)
     */
    public void startImportTask(String name, Uri uri, Intent intent) {
        importTask = new ImportTask(getActivity(), intent);
        importTask.importGeoPackage(name, uri, null);
    }


    /**
     * Initiate an Import task with permissions(received from intent outside of application)
     */
    public void startImportTaskWithPermissions(String name, Uri uri, String path, Intent intent) {
        importTask = new ImportTask(getActivity(), intent);
        importTask.importGeoPackageExternalLinkWithPermissions(name, uri, path);
    }

    /**
     * validate input - check for empty or valid url
     *
     * @param inputLayout The layout for the view.
     * @return true if input is not empty and is valid
     */
    private boolean validateInput(TextInputLayout inputLayout, TextInputEditText inputName, boolean isUrl) {
        if (inputName.getText() == null || inputName.getText().toString().trim().isEmpty()) {
            inputLayout.setError(inputLayout.getHint() + " " + getString(R.string.err_msg_invalid));
            return false;
        }
        if (isUrl) {
            if (!URLUtil.isValidUrl(inputName.getText().toString().trim())) {
                inputLayout.setError(inputLayout.getHint() + " " + getString(R.string.err_msg_invalid_url));
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        initializeMap();
    }

    /**
     * Initialize the map
     */
    private void initializeMap() {
        if (map == null) return;

        setEditFeaturesView();
        zoomer = new Zoomer(this.model, this.geoPackageViewModel, getActivity(), map, getView());
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnCameraIdleListener(this);
        map.getUiSettings().setRotateGesturesEnabled(false);
        //map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLoadedCallback(() -> {
            updateInBackground(true);
            mapLoaded = true;
        });

        map.moveCamera(CameraUpdateFactory.zoomTo(3));

        // Keep track of the current zoom level
        float zoom = MapUtils.getCurrentZoom(map);

        zoomLevelText.setText(getResources().getString(R.string.zoom_level, zoom));
        map.setOnCameraMoveListener(this.moveListener);

        basemapApplier = new BasemapApplier(getActivity(),
                PreferenceManager.getDefaultSharedPreferences(getActivity()),
                coordText,
                coordTextCard,
                this,
                this.moveListener);
        // Call the initial update to the settings
        settingsUpdate();
    }


    /**
     * Set the map color scheme to dark or default
     *
     * @param makeDark True if the map style should be the dark style, false otherwise.
     */
    private void setMapDarkMode(boolean makeDark) {
        if (map == null || getContext() == null) return;

        if (makeDark) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.dark_map));
        } else {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.default_map));
        }
    }



    /**
     * Set the full app color scheme to dark or default
     *
     * @param makeDark True if the app style should be the dark style, false otherwise.
     */
    private void setAppDarkMode(boolean makeDark) {
        if (makeDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }



    /**
     * Make the zoom in / zoom out icons visible
     */
    private void setZoomIconsVisible(boolean visible) {
        if (visible) {
            zoomInButton.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.VISIBLE);
        } else {
            zoomInButton.setVisibility(View.INVISIBLE);
            zoomOutButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Make the current zoom level visible as a text field in the top of the map
     */
    public void setZoomLevelVisible(boolean zoomVisible) {
        if (zoomVisible) {
            zoomLevelText.setVisibility(View.VISIBLE);
            this.moveListener.onCameraMove();
        } else {
            zoomLevelText.setVisibility(View.GONE);
        }
    }

    public View getTransBox() {
        return transBox;
    }

    @Override
    public TouchableMap getTouchableMap() {
        return touch;
    }

    @Override
    public GoogleMap getMap() {
        return map;
    }

    @Override
    public BasemapApplier getBaseApplier() {
        return this.basemapApplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraIdle() {

        // If visible & not editing a shape, update the feature shapes for the current map view region
        if (visible && (!model.isEditFeaturesMode() || editFeatureType == null || (editPoints.isEmpty() && editFeatureMarker == null))) {

            int previousZoom = currentZoom;
            int zoom = (int) MapUtils.getCurrentZoom(map);
            currentZoom = zoom;
            if (zoom != previousZoom) {
                // Zoom level changed, remove all feature shapes except for markers
                model.getFeatureShapes().removeShapesExcluding(GoogleMapShapeType.MARKER, GoogleMapShapeType.MULTI_MARKER);
            } else {
                // Remove shapes no longer visible on the map view
                model.getFeatureShapes().removeShapesNotWithinMap(map);
            }

            BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
            double toleranceDistance = MapUtils.getToleranceDistance(view, map);
            int maxFeatures = getMaxFeatures();

            updateLock.lock();
            try {
                if (updateFeaturesTask != null) {
                    updateFeaturesTask.cancel();
                }
                updateFeaturesTask = new MapFeaturesUpdateTask(getActivity(), map, model, geoPackageViewModel);
                updateFeaturesTask.execute(maxFeatures, mapViewBoundingBox, toleranceDistance, true);
            } finally {
                updateLock.unlock();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    private SupportMapFragment getMapFragment() {
        FragmentManager fm;
        fm = getChildFragmentManager();
        SupportMapFragment frag = null;
        frag = (SupportMapFragment) fm.findFragmentById(R.id.fragment_map_view_ui);
        return frag;
    }

    /**
     * Set the edit features view and buttons
     */
    private void setEditFeaturesView() {
        editFeaturesView = view.findViewById(R.id.mapFeaturesButtons);
        editFeaturesPolygonHoleView = view
                .findViewById(R.id.mapFeaturesPolygonHoleButtons);

        editPointButton = editFeaturesView
                .findViewById(R.id.mapEditPointButton);
        editPointButton.setOnClickListener((View arg0) -> validateAndClearEditFeatures(EditType.POINT));

        editLinestringButton = editFeaturesView
                .findViewById(R.id.mapEditLinestringButton);
        editLinestringButton.setOnClickListener((View arg0) -> validateAndClearEditFeatures(EditType.LINESTRING));

        editPolygonButton = editFeaturesView
                .findViewById(R.id.mapEditPolygonButton);
        editPolygonButton.setOnClickListener((View arg0) -> validateAndClearEditFeatures(EditType.POLYGON));

        editAcceptButton = editFeaturesView
                .findViewById(R.id.mapEditAcceptButton);
        editAcceptButton.setOnClickListener((View arg0) -> {
            if (editFeatureType != null
                    && (!editPoints.isEmpty() || editFeatureType == EditType.EDIT_FEATURE)) {
                boolean accept = false;
                switch (editFeatureType) {
                    case POINT:
                        accept = true;
                        break;
                    case LINESTRING:
                        if (editPoints.size() >= 2) {
                            accept = true;
                        }
                        break;
                    case POLYGON:
                    case POLYGON_HOLE:
                        if (editPoints.size() >= 3 && editHolePoints.isEmpty()) {
                            accept = true;
                        }
                        break;
                    case EDIT_FEATURE:
                        accept = editFeatureShape != null
                                && editFeatureShape.isValid();
                        break;
                }
                if (accept) {
                    saveEditFeatures();
                }
            }
        });

        editClearButton = editFeaturesView
                .findViewById(R.id.mapEditClearButton);
        editClearButton.setOnClickListener((View arg0) -> {
            if (!editPoints.isEmpty()
                    || editFeatureType == EditType.EDIT_FEATURE) {
                if (editFeatureType == EditType.EDIT_FEATURE) {
                    editFeatureType = null;
                }
                clearEditFeaturesAndPreserveType();
            }
        });

        editPolygonHolesButton = editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleButton);
        editPolygonHolesButton.setOnClickListener((View arg0) -> {
            if (editFeatureType != EditType.POLYGON_HOLE) {
                editFeatureType = EditType.POLYGON_HOLE;
                editPolygonHolesButton
                        .setImageResource(R.drawable.cut_hole_active);
            } else {
                editFeatureType = EditType.POLYGON;
                editPolygonHolesButton
                        .setImageResource(R.drawable.cut_hole);
            }
        });

        editAcceptPolygonHolesButton = editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleAcceptButton);
        editAcceptPolygonHolesButton
                .setOnClickListener((View arg0) -> {
                    if (editHolePoints.size() >= 3) {
                        List<LatLng> latLngPoints = getLatLngPoints(editHolePoints);
                        holePolygons.add(latLngPoints);
                        clearEditHoleFeatures();
                        updateEditState(true);
                    }
                });

        editClearPolygonHolesButton = editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleClearButton);
        editClearPolygonHolesButton
                .setOnClickListener((View arg0) -> {
                    clearEditHoleFeatures();
                    updateEditState(true);
                });

    }

    /**
     * If there are unsaved edits prompt the user for validation. Clear edit
     * features if ok.
     *
     * @param editTypeClicked Which edit type did the user choose.
     */
    private void validateAndClearEditFeatures(final EditType editTypeClicked) {

        if (editPoints.isEmpty() && editFeatureType != EditType.EDIT_FEATURE) {
            clearEditFeaturesAndUpdateType(editTypeClicked);
        } else {
            if (getActivity() != null) {
                AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(
                                getString(R.string.edit_features_clear_validation_label))
                        .setMessage(
                                getString(R.string.edit_features_clear_validation_message))
                        .setPositiveButton(getString(R.string.button_ok_label),
                                (DialogInterface dialog, int which) -> {
                                    if (editFeatureType == EditType.EDIT_FEATURE) {
                                        editFeatureType = null;
                                    }
                                    clearEditFeaturesAndUpdateType(editTypeClicked);
                                })
                        .setOnCancelListener(
                                (DialogInterface dialog) -> tempEditFeatureMarker = null)
                        .setNegativeButton(getString(R.string.button_cancel_label),
                                (DialogInterface dialog, int which) -> {
                                    tempEditFeatureMarker = null;
                                    dialog.dismiss();
                                }).create();
                deleteDialog.show();
            }
        }
    }

    /**
     * Clear edit features and update the type
     *
     * @param editType The new edit type.
     */
    private void clearEditFeaturesAndUpdateType(EditType editType) {
        EditType previousType = editFeatureType;
        clearEditFeatures();
        setEditType(previousType, editType);
    }

    /**
     * Clear edit features and preserve type
     */
    private void clearEditFeaturesAndPreserveType() {
        EditType previousType = editFeatureType;
        clearEditFeatures();
        setEditType(null, previousType);
    }

    /**
     * Set the edit type
     *
     * @param editType The edit type to set.
     */
    private void setEditType(EditType previousType, EditType editType) {

        if (editType != null && previousType != editType) {

            editFeatureType = editType;
            switch (editType) {
                case POINT:
                    editPointButton
                            .setImageResource(R.drawable.draw_point_active);
                    break;
                case LINESTRING:
                    editLinestringButton
                            .setImageResource(R.drawable.draw_line_active);
                    break;
                case POLYGON_HOLE:
                    editFeatureType = EditType.POLYGON;
                case POLYGON:
                    editPolygonButton
                            .setImageResource(R.drawable.draw_poly_active);
                    editFeaturesPolygonHoleView.setVisibility(View.VISIBLE);
                    break;
                case EDIT_FEATURE:
                    editFeatureMarker = tempEditFeatureMarker;
                    tempEditFeatureMarker = null;
                    Long featureId = model.getEditFeatureIds().get(editFeatureMarker.getId());
                    if (featureId != null) {
                        final GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
                        final FeatureDao featureDao = geoPackage
                                .getFeatureDao(model.getEditFeaturesTable());
                        final FeatureRow featureRow = featureDao
                                .queryForIdRow(featureId);
                        Geometry geometry = featureRow.getGeometry().getGeometry();
                        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                                featureDao.getProjection());
                        GoogleMapShape shape = converter.toShape(geometry);

                        editFeatureMarker.remove();
                        GoogleMapShape featureObject = model.getEditFeatureObjects()
                                .remove(editFeatureMarker.getId());
                        if (featureObject != null) {
                            featureObject.remove();
                        }

                        MarkerOptions editFeatureShapeMarker = getEditFeatureShapeMarker();
                        editFeatureShape = converter.addShapeToMapAsMarkers(map,
                                shape, getEditFeatureMarker(),
                                editFeatureShapeMarker, editFeatureShapeMarker,
                                getEditFeatureShapeHoleMarker(),
                                getDrawPolylineOptions(), getDrawPolygonOptions());

                        updateEditState(true);
                    }

                    break;
            }
        }
    }

    /**
     * Add editable shape back after editing is complete
     */
    private void addEditableShapeBack() {

        Long featureId = model.getEditFeatureIds().get(editFeatureMarker.getId());
        if (featureId != null) {
            final GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
            final FeatureDao featureDao = geoPackage
                    .getFeatureDao(model.getEditFeaturesTable());
            final FeatureRow featureRow = featureDao.queryForIdRow(featureId);
            GeoPackageGeometryData geomData = featureRow.getGeometry();
            if (geomData != null) {
                Geometry geometry = geomData.getGeometry();
                if (geometry != null) {
                    GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                            featureDao.getProjection());
                    GoogleMapShape shape = converter.toShape(geometry);
                    StyleCache styleCache = new StyleCache(geoPackage, getResources().getDisplayMetrics().density);
                    ShapeHelper.getInstance().prepareShapeOptions(
                            shape,
                            styleCache,
                            featureRow,
                            true,
                            true,
                            getContext());
                    GoogleMapShape mapShape = GoogleMapShapeConverter
                            .addShapeToMap(map, shape);
                    ShapeHelper.getInstance().addEditableShape(
                            getContext(), map, model, featureId, mapShape);
                    styleCache.clear();
                }
            }
        }
    }

    /**
     * Get the feature marker options for editing points
     *
     * @return The edit feature marker.
     */
    private MarkerOptions getEditFeatureMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.marker_create_color, typedValue, true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue
                .getFloat()));
        return markerOptions;
    }

    /**
     * Get the feature marker options to edit poly lines and polygons
     *
     * @return The edit feature shape marker.
     */
    private MarkerOptions getEditFeatureShapeMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_draw));
        TypedValue typedValueWidth = new TypedValue();
        getResources().getValue(R.dimen.shape_draw_icon_anchor_width,
                typedValueWidth, true);
        TypedValue typedValueHeight = new TypedValue();
        getResources().getValue(R.dimen.shape_draw_icon_anchor_height,
                typedValueHeight, true);
        markerOptions.anchor(typedValueWidth.getFloat(),
                typedValueHeight.getFloat());
        markerOptions.draggable(true);
        return markerOptions;
    }

    /**
     * Get the feature marker options to edit polygon holes
     *
     * @return The edit feature hole marker.
     */
    private MarkerOptions getEditFeatureShapeHoleMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_hole_draw));
        TypedValue typedValueWidth = new TypedValue();
        getResources().getValue(R.dimen.shape_hole_draw_icon_anchor_width,
                typedValueWidth, true);
        TypedValue typedValueHeight = new TypedValue();
        getResources().getValue(R.dimen.shape_hole_draw_icon_anchor_height,
                typedValueHeight, true);
        markerOptions.anchor(typedValueWidth.getFloat(),
                typedValueHeight.getFloat());
        markerOptions.draggable(true);
        return markerOptions;
    }

    /**
     * Save the edit features
     */
    private void saveEditFeatures() {

        boolean changesMade = false;

        GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
        EditType tempEditFeatureType = editFeatureType;
        try {
            FeatureDao featureDao = geoPackage.getFeatureDao(model.getEditFeaturesTable());
            long srsId = featureDao.getGeometryColumns().getSrsId();
            FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
            List<FeatureIndexType> indexedTypes = indexer.getIndexedTypes();

            GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                    featureDao.getProjection());

            switch (editFeatureType) {
                case POINT:

                    for (Marker pointMarker : editPoints.values()) {
                        mil.nga.sf.Point point = converter
                                .toPoint(pointMarker.getPosition());
                        FeatureRow newPoint = featureDao.newRow();
                        GeoPackageGeometryData pointGeomData = new GeoPackageGeometryData(
                                srsId);
                        pointGeomData.setGeometry(point);
                        newPoint.setGeometry(pointGeomData);
                        featureDao.insert(newPoint);
                        expandBounds(geoPackage, featureDao, point);
                        updateLastChange(geoPackage, featureDao);
                        if (!indexedTypes.isEmpty()) {
                            indexer.index(newPoint, indexedTypes);
                        }
                    }
                    changesMade = true;
                    break;

                case LINESTRING:

                    LineString lineString = converter.toLineString(editLinestring);
                    FeatureRow newLineString = featureDao.newRow();
                    GeoPackageGeometryData lineStringGeomData = new GeoPackageGeometryData(
                            srsId);
                    lineStringGeomData.setGeometry(lineString);
                    newLineString.setGeometry(lineStringGeomData);
                    featureDao.insert(newLineString);
                    expandBounds(geoPackage, featureDao, lineString);
                    updateLastChange(geoPackage, featureDao);
                    if (!indexedTypes.isEmpty()) {
                        indexer.index(newLineString, indexedTypes);
                    }
                    changesMade = true;
                    break;

                case POLYGON:
                case POLYGON_HOLE:

                    mil.nga.sf.Polygon polygon = converter
                            .toPolygon(editPolygon);
                    FeatureRow newPolygon = featureDao.newRow();
                    GeoPackageGeometryData polygonGeomData = new GeoPackageGeometryData(
                            srsId);
                    polygonGeomData.setGeometry(polygon);
                    newPolygon.setGeometry(polygonGeomData);
                    featureDao.insert(newPolygon);
                    expandBounds(geoPackage, featureDao, polygon);
                    updateLastChange(geoPackage, featureDao);
                    if (!indexedTypes.isEmpty()) {
                        indexer.index(newPolygon, indexedTypes);
                    }
                    changesMade = true;
                    break;

                case EDIT_FEATURE:
                    editFeatureType = null;
                    Long featureId = model.getEditFeatureIds().get(editFeatureMarker.getId());

                    if (featureId != null) {
                        Geometry geometry = converter.toGeometry(editFeatureShape
                                .getShape());
                        if (geometry != null) {
                            final FeatureRow featureRow = featureDao
                                    .queryForIdRow(featureId);
                            GeoPackageGeometryData geomData = featureRow.getGeometry();
                            geomData.setGeometry(geometry);
                            if (geomData.getEnvelope() != null) {
                                geomData.setEnvelope(GeometryEnvelopeBuilder.buildEnvelope(geometry));
                            }
                            featureDao.update(featureRow);
                            expandBounds(geoPackage, featureDao, geometry);
                            updateLastChange(geoPackage, featureDao);
                            if (!indexedTypes.isEmpty()) {
                                indexer.index(featureRow, indexedTypes);
                            }
                        } else {
                            featureDao.deleteById(featureId);
                            editFeatureMarker = null;
                            updateLastChange(geoPackage, featureDao);
                            if (!indexedTypes.isEmpty()) {
                                indexer.deleteIndex(featureId, indexedTypes);
                            }
                        }
                        model.getActive().setModified(true);
                    }

                    break;
            }
            indexer.close();

        } catch (Exception e) {
            if (GeoPackageUtils.isUnsupportedSQLiteException(e)) {
                GeoPackageUtils
                        .showMessage(
                                getActivity(),
                                getString(R.string.edit_features_save_label)
                                        + " " + model.getEditFeaturesTable(),
                                "GeoPackage contains unsupported SQLite function, module, or trigger for writing: " + e.getMessage());
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.edit_features_save_label) + " "
                                + tempEditFeatureType, e.getMessage());
            }
        }

        clearEditFeaturesAndPreserveType();

        if (changesMade) {
            model.getActive().setModified(true);
            updateInBackground(false);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        if (locationVisible) {
            showMyLocation();
        }
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView() {
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        visible = !hidden;

        if (visible && model.getActive().isModified()) {
            model.getActive().setModified(false);
            resetBoundingBox();
            resetEditFeatures();
            if (mapLoaded) {
                updateInBackground(true);
            }
        } else if (!visible) {
            updateLock.lock();
            try {
                if (updateTask != null) {
                    updateTask.cancel();
                    model.getActive().setModified(true);
                    updateTask = null;
                }
                if (updateFeaturesTask != null) {
                    updateFeaturesTask.cancel();
                    model.getActive().setModified(true);
                    updateFeaturesTask = null;
                }
            } finally {
                updateLock.unlock();
            }
        }
    }

    /**
     * Set the my location enabled state on the map if permission has been granted
     *
     * @return true if updated, false if permission is required
     */
    public boolean setMyLocationEnabled() {
        boolean updated = false;
        if (map != null && getActivity() != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(locationVisible);
            updated = true;
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
        return updated;
    }

    /**
     * Handle map menu clicks
     *
     * @param item The item that was clicked.
     * @return True if the click was handled, false if it was not.
     */
    public boolean handleMenuClick(MenuItem item) {
        boolean handled = false;

        if (item.getItemId() == R.id.map_zoom) {
            zoomer.zoomToActive();
            handled = true;
        } else if (item.getItemId() == R.id.map_features) {
            editFeaturesMenuItem = item;
            if (!model.isEditFeaturesMode()) {
                selectEditFeatures();
            } else {
                resetEditFeatures();
                updateInBackground(false);
            }
            handled = true;
        } else if (item.getItemId() == R.id.map_bounding_box) {
            boundingBoxMenuItem = item;
            if (!boundingBoxMode) {

                if (model.isEditFeaturesMode()) {
                    resetEditFeatures();
                    updateInBackground(false);
                }

                boundingBoxMode = true;
                boundingBoxMenuItem.setIcon(R.drawable.ic_bounding_box_active);
            } else {
                resetBoundingBox();
            }
            handled = true;
        } else if (item.getItemId() == R.id.max_features) {
            setMaxFeatures();
            handled = true;
        }

        return handled;
    }


    /**
     * Open Edit features mode with a preselected GeoPackage and Layer
     * (This happens when a user clicks the edit features button from a layer detail page)
     */
    private void openEditFeatures(String geoPackage, String layer) {
        try {

            if (boundingBoxMode) {
                resetBoundingBox();
            }

            model.setEditFeaturesDatabase(geoPackage);
            model.setEditFeaturesTable(layer);

            model.setEditFeaturesMode(true);
            editFeaturesView.setVisibility(View.VISIBLE);


            updateInBackground(false);

        } catch (Exception e) {
            GeoPackageUtils
                    .showMessage(
                            getActivity(),
                            getString(R.string.edit_features_selection_features_label),
                            e.getMessage());
        }
    }

    /**
     * Select the features to edit
     */
    private void selectEditFeatures() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View editFeaturesSelectionView = inflater.inflate(
                R.layout.edit_features_selection, null);

        final Spinner geoPackageInput = editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_geopackage);
        final Spinner featuresInput = editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_features);

        AlertDialog.Builder dialog = getFeatureSelectionDialog(editFeaturesSelectionView,
                geoPackageInput, featuresInput);

        if (dialog != null) {

            dialog.setPositiveButton(getString(R.string.button_ok_label),
                    (DialogInterface d, int id) -> {

                        try {

                            if (boundingBoxMode) {
                                resetBoundingBox();
                            }

                            model.setEditFeaturesDatabase(geoPackageInput.getSelectedItem().toString());
                            model.setEditFeaturesTable(featuresInput.getSelectedItem().toString());

                            model.setEditFeaturesMode(true);
                            editFeaturesView.setVisibility(View.VISIBLE);
                            editFeaturesMenuItem
                                    .setIcon(R.drawable.ic_features_active);

                            updateInBackground(false);

                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.edit_features_selection_features_label),
                                            e.getMessage());
                        }
                    }).setNegativeButton(getString(R.string.button_cancel_label),
                    (DialogInterface d, int id) -> d.cancel());
            dialog.show();
        }

    }

    /**
     * Update the features selection based upon the database
     *
     * @param featuresInput The feature input spinner.
     * @param database      The name of the geoPackage.
     */
    private void updateFeaturesSelection(Spinner featuresInput, String database) {

        GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(database);
        List<String> features = geoPackage.getFeatureTables();
        ArrayAdapter<String> featuresAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_item, features);
        featuresInput.setAdapter(featuresAdapter);
    }

    /**
     * Reset the bounding box mode
     */
    private void resetBoundingBox() {
        boundingBoxMode = false;
        clearBoundingBox();
    }

    /**
     * Reset the edit features state
     */
    private void resetEditFeatures() {
        model.setEditFeaturesMode(false);
        editFeaturesView.setVisibility(View.INVISIBLE);
        model.setEditFeaturesDatabase(null);
        model.setEditFeaturesTable(null);
        model.getEditFeatureIds().clear();
        model.getEditFeatureObjects().clear();
        editFeatureShape = null;
        editFeatureShapeMarkers = null;
        editFeatureMarker = null;
        tempEditFeatureMarker = null;
        clearEditFeatures();
    }

    @Override
    public Polygon getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void setBoundingBoxStartCorner(LatLng startCorner) {
        this.boundingBoxStartCorner = startCorner;
    }

    @Override
    public void setBoundingBoxEndCorner(LatLng endCorner) {
        this.boundingBoxEndCorner = endCorner;
    }

    /**
     * Turn off the loading of tiles
     */
    public void clearBoundingBox() {
        if (boundingBox != null) {
            boundingBox.remove();
        }
        boundingBoxStartCorner = null;
        boundingBoxEndCorner = null;
        boundingBox = null;
        setDrawing(false);
    }

    /**
     * Clear the edit features
     */
    private void clearEditFeatures() {
        editFeatureType = null;
        for (Marker editMarker : editPoints.values()) {
            editMarker.remove();
        }
        editPoints.clear();
        if (editLinestring != null) {
            editLinestring.remove();
            editLinestring = null;
        }
        if (editPolygon != null) {
            editPolygon.remove();
            editPolygon = null;
        }
        holePolygons.clear();
        editPointButton.setImageResource(R.drawable.draw_point);
        editLinestringButton.setImageResource(R.drawable.draw_line);
        editPolygonButton.setImageResource(R.drawable.draw_poly);
        editFeaturesPolygonHoleView.setVisibility(View.INVISIBLE);
        editAcceptButton.setImageResource(R.drawable.save_changes);
        editClearButton.setImageResource(R.drawable.cancel_changes);
        editPolygonHolesButton
                .setImageResource(R.drawable.cut_hole);
        clearEditHoleFeatures();
        if (editFeatureShape != null) {
            editFeatureShape.remove();
            if (editFeatureMarker != null) {
                addEditableShapeBack();
                editFeatureMarker = null;
            }
            editFeatureShape = null;
            editFeatureShapeMarkers = null;
        }
    }

    /**
     * Clear the edit hole features
     */
    private void clearEditHoleFeatures() {

        for (Marker editMarker : editHolePoints.values()) {
            editMarker.remove();
        }
        editHolePoints.clear();
        if (editHolePolygon != null) {
            editHolePolygon.remove();
            editHolePolygon = null;
        }
        editAcceptPolygonHolesButton.setImageResource(R.drawable.save_changes);
        editClearPolygonHolesButton.setImageResource(R.drawable.cancel_changes);
    }

    /**
     * Let the user set the max number of features to draw
     */
    private void setMaxFeatures() {
        if (getActivity() != null) {
            // Create Alert window with basic input text layout
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
            // Logo and title
            ImageView alertLogo = alertView.findViewById(R.id.alert_logo);
            alertLogo.setBackgroundResource(R.drawable.material_edit);
            TextView titleText = alertView.findViewById(R.id.alert_title);
            titleText.setText(R.string.max_active_features);
            // Set description
            TextView descText = alertView.findViewById(R.id.alert_description);
            descText.setText(R.string.limit_features);
            descText.setVisibility(View.VISIBLE);
            // Set input to current max features value
            final EditText input = alertView.findViewById(R.id.edit_text_input);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            final String maxFeatures = String.valueOf(getMaxFeatures());
            input.setText(maxFeatures);
            input.setHint(maxFeatures);

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setView(alertView)
                    .setPositiveButton(getString(R.string.button_save_label),
                            (DialogInterface d, int whichButton) -> {
                                String value = input.getText().toString();
                                if (!value.equals(maxFeatures)) {
                                    int maxFeature = Integer.parseInt(value);
                                    SharedPreferences settings = PreferenceManager
                                            .getDefaultSharedPreferences(getActivity());
                                    Editor editor = settings.edit();
                                    editor.putInt(MAX_FEATURES_KEY, maxFeature);
                                    editor.apply();
                                    updateInBackground(false);
                                    // ignoreHighFeatures will tell if the user previously checked the
                                    // 'do not show this warning again' checkbox last time
                                    boolean ignoreHighFeatures = settings.getBoolean(String.valueOf(R.string.ignore_high_features), false);
                                    if (maxFeature > 10000 && !ignoreHighFeatures) {
                                        maxFeatureWarning();
                                    }
                                }
                            })
                    .setNegativeButton(getString(R.string.button_cancel_label),
                            (DialogInterface d, int whichButton) -> d.cancel());

            AlertDialog alert = dialog.create();

            // Listener to make sure there's always a value in the input before submitting
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Prevent users from setting to less than 1, or greater than 1 million
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    int length = charSequence.length();
                    int highestMaxFeatures = 1000000;
                    if (length < 1) {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else if (length > String.valueOf(highestMaxFeatures).length()) {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        input.setError("Cannot set max features higher than " + highestMaxFeatures);
                    } else {
                        int maxFeature = Integer.parseInt(charSequence.toString());
                        if (maxFeature > highestMaxFeatures) {
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            input.setError("Cannot set max features higher than " + highestMaxFeatures);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            alert.show();
        }
    }

    /**
     * Makes a warning popup to alert the user that the max features setting is high
     */
    public void maxFeatureWarning() {
        if (getActivity() != null) {
            View checkBoxView = View.inflate(getContext(), R.layout.checkbox, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.showHighFeatureBox);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.max_feature_size_warning)
                    .setView(checkBoxView)
                    .setTitle("Warning")
                    .setPositiveButton("ok", (DialogInterface dialog, int id) -> {
                        if (checkBox.isChecked()) {
                            // If they check the 'do not show again' box, save that setting
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            settings.edit().putBoolean(String.valueOf(R.string.ignore_high_features), true).apply();
                        }
                        dialog.cancel();
                    });
            // Create the AlertDialog object and return it
            builder.show();
        }
    }

    /**
     * Get the max features
     *
     * @return The number of maximum features allowed on map.
     */
    private int getMaxFeatures() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        return settings.getInt(MAX_FEATURES_KEY, getResources()
                .getInteger(R.integer.map_max_features_default));
    }

    /**
     * Set the map type
     *
     * @param mapType The base map type.
     */
    private void setMapType(int mapType) {
        if (basemapApplier != null) {
            basemapApplier.setMapType(map, mapType);
        }
    }

    /**
     * Sets the new grid type.
     *
     * @param gridType The new grid type.
     */
    private void setGridType(GridType gridType) {
        if (basemapApplier != null) {
            basemapApplier.setGridType(map, gridType);
        }
    }

    /**
     * Update the map by kicking off a background task
     *
     * @param zoom zoom flag
     */
    private void updateInBackground(boolean zoom) {
        if (getActivity() != null) {
            model.getFeatureDaos().clear();
            basemapApplier.clear();

            if (zoom) {
                zoomer.zoomToActiveBounds();
            }

            model.setFeaturesBoundingBox(null);
            model.setTilesBoundingBox(null);
            model.setFeatureOverlayTiles(false);
            model.getFeatureOverlayQueries().clear();
            model.getFeatureShapes().clear();
            model.getMarkerIds().clear();

            getActivity().runOnUiThread(() -> {
                map.clear();
                MapUpdateTask localUpdateTask;
                updateLock.lock();
                try {
                    if (updateTask != null) {
                        updateTask.cancel();
                    }
                    if (updateFeaturesTask != null) {
                        updateFeaturesTask.cancel();
                    }
                    updateTask = new MapUpdateTask(getActivity(), map, basemapApplier, model, geoPackageViewModel);
                    localUpdateTask = updateTask;

                    BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
                    double toleranceDistance = MapUtils.getToleranceDistance(view, map);
                    int maxFeatures = getMaxFeatures();
                    updateFeaturesTask = new MapFeaturesUpdateTask(getActivity(), map, model, geoPackageViewModel);
                    updateTask.setFinishListener(() -> updateFeaturesTask.execute(maxFeatures, mapViewBoundingBox, toleranceDistance, true));
                } finally {
                    updateLock.unlock();
                }

                localUpdateTask.execute();
            });
        }
    }

    /**
     * Draw a bounding box with boundingBoxStartCorner and boundingBoxEndCorner
     */
    public boolean drawBoundingBox() {
        PolygonOptions polygonOptions = new PolygonOptions();

        if (getActivity() != null) {
            polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.bounding_box_draw_color));
            polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.bounding_box_draw_fill_color));
        }

        List<LatLng> points = getPolygonPoints(boundingBoxStartCorner,
                boundingBoxEndCorner);
        polygonOptions.addAll(points);
        boundingBox = map.addPolygon(polygonOptions);
        setDrawing(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapLongClick(@NonNull LatLng point) {
        if (getActivity() != null) {
            if (boundingBoxMode) {
                vibrator.vibrate(VibrationEffect.createOneShot(getActivity().getResources()
                                .getInteger(R.integer.edit_features_add_long_click_vibrate_quick),
                        VibrationEffect.DEFAULT_AMPLITUDE));
                // Check to see if editing any of the bounding box corners
                if (boundingBox != null && boundingBoxEndCorner != null) {
                    Projection projection = map.getProjection();

                    double allowableScreenPercentage = (getActivity()
                            .getResources()
                            .getInteger(
                                    R.integer.map_tiles_long_click_screen_percentage) / 100.0);
                    Point screenPoint = projection.toScreenLocation(point);

                    if (isWithinDistance(projection, screenPoint,
                            boundingBoxEndCorner, allowableScreenPercentage)) {
                        setDrawing(true);
                    } else if (isWithinDistance(projection, screenPoint,
                            boundingBoxStartCorner, allowableScreenPercentage)) {
                        LatLng temp = boundingBoxStartCorner;
                        boundingBoxStartCorner = boundingBoxEndCorner;
                        boundingBoxEndCorner = temp;
                        setDrawing(true);
                    } else {
                        LatLng corner1 = new LatLng(
                                boundingBoxStartCorner.latitude,
                                boundingBoxEndCorner.longitude);
                        LatLng corner2 = new LatLng(boundingBoxEndCorner.latitude,
                                boundingBoxStartCorner.longitude);
                        if (isWithinDistance(projection, screenPoint, corner1,
                                allowableScreenPercentage)) {
                            boundingBoxStartCorner = corner2;
                            boundingBoxEndCorner = corner1;
                            setDrawing(true);
                        } else if (isWithinDistance(projection, screenPoint,
                                corner2, allowableScreenPercentage)) {
                            boundingBoxStartCorner = corner1;
                            boundingBoxEndCorner = corner2;
                            setDrawing(true);
                        }
                    }
                }

                // Start drawing a new polygon
                if (!drawing) {
                    if (boundingBox != null) {
                        boundingBox.remove();
                    }
                    boundingBoxStartCorner = point;
                    boundingBoxEndCorner = point;
                    PolygonOptions polygonOptions = new PolygonOptions();
                    polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.bounding_box_draw_color));
                    polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.bounding_box_draw_fill_color));
                    List<LatLng> points = getPolygonPoints(boundingBoxStartCorner,
                            boundingBoxEndCorner);
                    polygonOptions.addAll(points);
                    boundingBox = map.addPolygon(polygonOptions);
                    setDrawing(true);
                }
            } else if (editFeatureType != null) {
                if (editFeatureType == EditType.EDIT_FEATURE) {
                    if (editFeatureShapeMarkers != null) {
                        vibrator.vibrate(VibrationEffect.createOneShot(getActivity().getResources()
                                        .getInteger(R.integer.edit_features_add_long_click_vibrate_quick),
                                VibrationEffect.DEFAULT_AMPLITUDE));
                        Marker marker = addEditPoint(point);
                        editFeatureShapeMarkers.addNew(marker);
                        editFeatureShape.add(marker, editFeatureShapeMarkers);
                        updateEditState(true);
                    }
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(getActivity().getResources()
                            .getInteger(R.integer.edit_features_add_long_click_vibrate_quick),
                            VibrationEffect.DEFAULT_AMPLITUDE));
                    Marker marker = addEditPoint(point);
                    if (editFeatureType == EditType.POLYGON_HOLE) {
                        editHolePoints.put(marker.getId(), marker);
                    } else {
                        editPoints.put(marker.getId(), marker);
                    }
                    updateEditState(true);
                }
            }
        }
    }

    /**
     * Get the edit point marker options
     *
     * @param point The location of the point.
     * @return The marker to be put on the map.
     */
    private Marker addEditPoint(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.draggable(true);
        switch (editFeatureType) {
            case POINT:
                setEditPointOptions(markerOptions);
                break;
            case LINESTRING:
            case POLYGON:
                setEditPointShapeOptions(markerOptions);
                break;
            case POLYGON_HOLE:
                setEditPointShapeHoleOptions(markerOptions);
                break;
            case EDIT_FEATURE:
                if (editFeatureShapeMarkers instanceof PolygonHoleMarkers) {
                    setEditPointShapeHoleOptions(markerOptions);
                } else {
                    setEditPointShapeOptions(markerOptions);
                }
                break;
        }

        return map.addMarker(markerOptions);
    }

    /**
     * Set the marker options for edit points
     *
     * @param markerOptions The options for the marker to be put on the map.
     */
    private void setEditPointOptions(MarkerOptions markerOptions) {
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.marker_create_color, typedValue, true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue
                .getFloat()));
    }

    /**
     * Set the marker options for edit shape points
     *
     * @param markerOptions The options for the marker to be put on the map.
     */
    private void setEditPointShapeOptions(MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_draw));
        TypedValue drawWidth = new TypedValue();
        getResources().getValue(R.dimen.shape_draw_icon_anchor_width,
                drawWidth, true);
        TypedValue drawHeight = new TypedValue();
        getResources().getValue(R.dimen.shape_draw_icon_anchor_height,
                drawHeight, true);
        markerOptions.anchor(drawHeight.getFloat(), drawHeight.getFloat());
    }

    /**
     * Set the marker options for edit shape hole point
     *
     * @param markerOptions The options for the marker to be put on the map.
     */
    private void setEditPointShapeHoleOptions(MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_hole_draw));
        TypedValue holeWidth = new TypedValue();
        getResources().getValue(R.dimen.shape_hole_draw_icon_anchor_width,
                holeWidth, true);
        TypedValue holeHeight = new TypedValue();
        getResources().getValue(R.dimen.shape_hole_draw_icon_anchor_height,
                holeHeight, true);
        markerOptions.anchor(holeWidth.getFloat(), holeHeight.getFloat());
    }

    /**
     * Update the current edit state, buttons, and visuals
     *
     * @param updateAcceptClear True if the accept and clear buttons active appearance should be
     *                          updated.
     */
    private void updateEditState(boolean updateAcceptClear) {
        boolean accept = false;
        switch (editFeatureType) {

            case POINT:
                if (!editPoints.isEmpty()) {
                    accept = true;
                }
                break;

            case LINESTRING:

                if (editPoints.size() >= 2) {
                    accept = true;

                    List<LatLng> points = getLatLngPoints(editPoints);
                    if (editLinestring != null) {
                        editLinestring.setPoints(points);
                    } else {
                        PolylineOptions polylineOptions = getDrawPolylineOptions();
                        polylineOptions.addAll(points);
                        editLinestring = map.addPolyline(polylineOptions);
                    }
                } else if (editLinestring != null) {
                    editLinestring.remove();
                    editLinestring = null;
                }

                break;

            case POLYGON:
            case POLYGON_HOLE:

                if (editPoints.size() >= 3) {
                    accept = true;

                    List<LatLng> points = getLatLngPoints(editPoints);
                    if (editPolygon != null) {
                        editPolygon.setPoints(points);
                        editPolygon.setHoles(holePolygons);
                    } else {
                        PolygonOptions polygonOptions = getDrawPolygonOptions();
                        polygonOptions.addAll(points);
                        for (List<LatLng> hole : holePolygons) {
                            polygonOptions.addHole(hole);
                        }
                        editPolygon = map.addPolygon(polygonOptions);
                    }
                } else if (editPolygon != null) {
                    editPolygon.remove();
                    editPolygon = null;
                }

                if (editFeatureType == EditType.POLYGON_HOLE) {

                    if (!editHolePoints.isEmpty()) {
                        accept = false;
                        editClearPolygonHolesButton
                                .setImageResource(R.drawable.cancel_changes_active);
                    } else {
                        editClearPolygonHolesButton
                                .setImageResource(R.drawable.cancel_changes);
                    }

                    if (editHolePoints.size() >= 3) {

                        editAcceptPolygonHolesButton
                                .setImageResource(R.drawable.save_changes_active);

                        List<LatLng> points = getLatLngPoints(editHolePoints);
                        if (editHolePolygon != null) {
                            editHolePolygon.setPoints(points);
                        } else {
                            PolygonOptions polygonOptions = getHoleDrawPolygonOptions();
                            polygonOptions.addAll(points);
                            editHolePolygon = map.addPolygon(polygonOptions);
                        }

                    } else {
                        editAcceptPolygonHolesButton
                                .setImageResource(R.drawable.save_changes);
                        if (editHolePolygon != null) {
                            editHolePolygon.remove();
                            editHolePolygon = null;
                        }
                    }
                }

                break;

            case EDIT_FEATURE:
                accept = true;

                if (editFeatureShape != null) {
                    editFeatureShape.update();
                    accept = editFeatureShape.isValid();
                }
                break;
        }

        if (updateAcceptClear) {
            if (!editPoints.isEmpty()
                    || editFeatureType == EditType.EDIT_FEATURE) {
                editClearButton.setImageResource(R.drawable.cancel_changes_active);
            } else {
                editClearButton.setImageResource(R.drawable.cancel_changes);
            }
            if (accept) {
                editAcceptButton.setImageResource(R.drawable.save_changes_active);
            } else {
                editAcceptButton.setImageResource(R.drawable.save_changes);
            }
        }
    }

    /**
     * Get draw polyline options
     *
     * @return The polyline options.
     */
    private PolylineOptions getDrawPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        if (this.getActivity() != null) {
            polylineOptions.color(ContextCompat.getColor(getActivity(), R.color.polyline_draw_color));
        }
        return polylineOptions;
    }

    /**
     * Get draw polygon options
     *
     * @return The polygon options.
     */
    private PolygonOptions getDrawPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        if (this.getActivity() != null) {
            polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_draw_color));
            polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_draw_fill_color));
        }
        return polygonOptions;
    }

    /**
     * Get hold draw polygon options
     *
     * @return The polygon options.
     */
    private PolygonOptions getHoleDrawPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        if (this.getActivity() != null) {
            polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_hole_draw_color));
            polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_hole_draw_fill_color));
        }
        return polygonOptions;
    }

    /**
     * Get a list of points as LatLng
     *
     * @param markers The markers to collect points from.
     * @return The list of points of the marker locations.
     */
    private List<LatLng> getLatLngPoints(Map<String, Marker> markers) {
        List<LatLng> points = new ArrayList<>();
        for (Marker editPoint : markers.values()) {
            points.add(editPoint.getPosition());
        }
        return points;
    }

    /**
     * Set the drawing value
     *
     * @param drawing True if drawing false if not.
     */
    private void setDrawing(boolean drawing) {
        this.drawing = drawing;
        map.getUiSettings().setScrollGesturesEnabled(!drawing);
    }

    /**
     * Check if the point is within clicking distance to the lat lng corner
     *
     * @param projection                The projection to use.
     * @param point                     The point to check.
     * @param latLng                    The corner to check.
     * @param allowableScreenPercentage The percentage of the screen distance the point and corner
     *                                  must be within.
     * @return True if the point an corner are within the specified screen distance percentage.
     */
    private boolean isWithinDistance(Projection projection, Point point,
                                     LatLng latLng, double allowableScreenPercentage) {
        Point point2 = projection.toScreenLocation(latLng);
        double distance = Math.sqrt(Math.pow(point.x - point2.x, 2)
                + Math.pow(point.y - point2.y, 2));

        return distance / Math.min(view.getWidth(), view.getHeight()) <= allowableScreenPercentage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapClick(@NonNull LatLng point) {

        if (!model.isEditFeaturesMode()) {

            StringBuilder clickMessage = new StringBuilder();

            if (!model.getFeatureOverlayQueries().isEmpty()) {
                for (FeatureOverlayQuery query : model.getFeatureOverlayQueries()) {
                    String message = query.buildMapClickMessage(point, view, map);
                    if (message != null) {
                        if (clickMessage.length() > 0) {
                            clickMessage.append("\n\n");
                        }
                        clickMessage.append(message);
                    }
                }
            }

            for (GeoPackageDatabase database : model.getActive().getDatabases()) {
                if (!database.getFeatures().isEmpty()) {

                    TypedValue screenPercentage = new TypedValue();
                    getResources().getValue(R.dimen.map_feature_click_screen_percentage, screenPercentage, true);
                    float screenClickPercentage = screenPercentage.getFloat();

                    BoundingBox clickBoundingBox = MapUtils.buildClickBoundingBox(point, view, map, screenClickPercentage);
                    clickBoundingBox = clickBoundingBox.expandWgs84Coordinates();
                    mil.nga.proj.Projection clickProjection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

                    double tolerance = MapUtils.getToleranceDistance(point, view, map, screenClickPercentage);

                    for (GeoPackageTable features : database.getFeatures()) {

                        GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(database.getDatabase());
                        Map<String, FeatureDao> databaseFeatureDaos = model.getFeatureDaos().get(database.getDatabase());

                        if (geoPackage != null && databaseFeatureDaos != null) {

                            FeatureDao featureDao = databaseFeatureDaos.get(features.getName());

                            if (featureDao != null) {

                                FeatureIndexResults indexResults;

                                FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
                                if (indexer.isIndexed()) {

                                    indexResults = indexer.query(clickBoundingBox, clickProjection);
                                    BoundingBox complementary = clickBoundingBox.complementaryWgs84();
                                    if (complementary != null) {
                                        FeatureIndexResults indexResults2 = indexer.query(complementary, clickProjection);
                                        indexResults = new MultipleFeatureIndexResults(indexResults, indexResults2);
                                    }

                                } else {

                                    mil.nga.proj.Projection featureProjection = featureDao.getProjection();
                                    ProjectionTransform projectionTransform = clickProjection.getTransformation(featureProjection);
                                    BoundingBox boundedClickBoundingBox = clickBoundingBox.boundWgs84Coordinates();
                                    BoundingBox transformedBoundingBox = boundedClickBoundingBox.transform(projectionTransform);
                                    double filterMaxLongitude = 0;
                                    if (featureProjection.isUnit(Units.DEGREES)) {
                                        filterMaxLongitude = ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH;
                                    } else if (featureProjection.isUnit(Units.METRES)) {
                                        filterMaxLongitude = ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH;
                                    }

                                    FeatureIndexListResults listResults = new FeatureIndexListResults();

                                    // Query for all rows
                                    try (FeatureCursor cursor = featureDao.query()) {

                                        while (cursor.moveToNext()) {

                                            try {
                                                FeatureRow row = cursor.getRow();

                                                GeoPackageGeometryData geometryData = row.getGeometry();
                                                if (geometryData != null && !geometryData.isEmpty()) {

                                                    Geometry geometry = geometryData.getGeometry();

                                                    if (geometry != null) {

                                                        GeometryEnvelope envelope = geometryData.getEnvelope();
                                                        if (envelope == null) {
                                                            envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
                                                        }
                                                        if (envelope != null) {
                                                            BoundingBox geometryBoundingBox = new BoundingBox(envelope);

                                                            if (TileBoundingBoxUtils.overlap(transformedBoundingBox, geometryBoundingBox, filterMaxLongitude) != null) {
                                                                listResults.addRow(row);
                                                            }

                                                        }
                                                    }
                                                }

                                            } catch (Exception e) {
                                                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                                        "Failed to query feature. database: " + database.getDatabase()
                                                                + ", feature table: " + features.getName()
                                                                + ", row: " + cursor.getPosition(), e);
                                            }
                                        }

                                    }

                                    indexResults = listResults;
                                }
                                indexer.close();

                                if (indexResults.count() > 0 && this.getActivity() != null) {
                                    FeatureInfoBuilder featureInfoBuilder = new FeatureInfoBuilder(getActivity(), featureDao);
                                    featureInfoBuilder.ignoreGeometryType(GeometryType.POINT);
                                    String message = featureInfoBuilder.buildResultsInfoMessageAndClose(indexResults, tolerance, point);
                                    if (message != null) {
                                        if (clickMessage.length() > 0) {
                                            clickMessage.append("\n\n");
                                        }
                                        clickMessage.append(message);
                                    }
                                }
                            }

                        }
                    }
                }
            }

            if (clickMessage.length() > 0 && this.getActivity() != null) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setMessage(clickMessage.toString())
                        .setPositiveButton(android.R.string.yes,
                                (DialogInterface dialog, int which) -> {
                                })
                        .show();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        String markerId = marker.getId();

        if (model.isEditFeaturesMode()) {

            // Handle clicks to edit contents of an existing feature
            if (editFeatureShape != null && editFeatureShape.contains(markerId)) {
                editFeatureShapeClick(marker);
                return true;
            }

            // Handle clicks on an existing feature in edit mode
            Long featureId = model.getEditFeatureIds().get(markerId);
            if (featureId != null) {
                editExistingFeatureClick(marker, featureId);
                return true;
            }

            // Handle clicks on new edit points
            Marker editPoint = editPoints.get(markerId);
            if (editPoint != null) {
                editMarkerClick(marker, editPoints);
                return true;
            }

            // Handle clicks on new edit hole points
            editPoint = editHolePoints.get(markerId);
            if (editPoint != null) {
                editMarkerClick(marker, editHolePoints);
                return true;
            }

        } else {
            // Handle clicks on point markers
            MarkerFeature markerFeature = model.getMarkerIds().get(markerId);
            if (markerFeature != null) {
                infoFeatureClick(markerFeature);
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        updateEditState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        updateEditState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        if (getActivity() != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(getActivity().getResources()
                            .getInteger(R.integer.edit_features_add_long_click_vibrate_quick),
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /**
     * Edit feature shape marker click
     *
     * @param marker The marker to edit.
     */
    private void editFeatureShapeClick(final Marker marker) {

        final ShapeMarkers shapeMarkers = editFeatureShape
                .getShapeMarkers(marker);
        if (shapeMarkers != null && getActivity() != null) {

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.select_dialog_item);
            adapter.add(getString(R.string.edit_features_shape_point_delete_label));
            adapter.add(getString(R.string.edit_features_shape_add_points_label));
            if (shapeMarkers instanceof ShapeWithChildrenMarkers) {
                adapter.add(getString(R.string.edit_features_shape_add_hole_label));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            DecimalFormat formatter = new DecimalFormat("0.0###");
            LatLng position = marker.getPosition();
            final String title = "(lat=" + formatter.format(position.latitude)
                    + ", lon=" + formatter.format(position.longitude) + ")";
            builder.setTitle(title);
            builder.setAdapter(adapter, (DialogInterface dialog, int item) -> {

                if (item >= 0) {
                    switch (item) {
                        case 0:
                            editFeatureShape.delete(marker);
                            updateEditState(true);
                            break;
                        case 1:
                            editFeatureShapeMarkers = shapeMarkers;
                            break;
                        case 2:
                            if (shapeMarkers instanceof ShapeWithChildrenMarkers) {
                                ShapeWithChildrenMarkers shapeWithChildrenMarkers = (ShapeWithChildrenMarkers) shapeMarkers;
                                editFeatureShapeMarkers = shapeWithChildrenMarkers
                                        .createChild();
                            }
                            break;
                        default:
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Edit marker click
     *
     * @param marker The marker to edit.
     * @param points The points to edit.
     */
    private void editMarkerClick(final Marker marker,
                                 final Map<String, Marker> points) {

        if (getActivity() != null) {
            LatLng position = marker.getPosition();
            String message = editFeatureType.name();
            if (editFeatureType != EditType.POINT) {
                message += " " + EditType.POINT.name();
            }
            AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setCancelable(false)
                    .setTitle(getString(R.string.edit_features_delete_label))
                    .setMessage(
                            getString(R.string.edit_features_delete_label) + " "
                                    + message + " (lat=" + position.latitude
                                    + ", lon=" + position.longitude + ") ?")
                    .setPositiveButton(
                            getString(R.string.edit_features_delete_label),

                            (DialogInterface dialog, int which) -> {

                                points.remove(marker.getId());
                                marker.remove();

                                updateEditState(true);
                            })

                    .setNegativeButton(getString(R.string.button_cancel_label),
                            (DialogInterface dialog, int which) -> dialog.dismiss()).create();
            deleteDialog.show();
        }
    }

    /**
     * Edit existing feature click
     *
     * @param marker    The marker to edit.
     * @param featureId The id of the feature being edited.
     */
    private void editExistingFeatureClick(final Marker marker, long featureId) {
        final GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
        final FeatureDao featureDao = geoPackage
                .getFeatureDao(model.getEditFeaturesTable());

        final FeatureRow featureRow = featureDao.queryForIdRow(featureId);

        if (featureRow != null && getActivity() != null) {
            final GeoPackageGeometryData geomData = featureRow.getGeometry();
            final GeometryType geometryType = geomData.getGeometry()
                    .getGeometryType();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.select_dialog_item);
            adapter.add(getString(R.string.edit_features_info_label));
            adapter.add(getString(R.string.edit_features_edit_label));
            adapter.add(getString(R.string.edit_features_delete_label));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            final String title = getTitle(geometryType, marker);
            builder.setTitle(title);
            builder.setAdapter(adapter, (DialogInterface dialog, int item) -> {

                if (item >= 0) {
                    switch (item) {
                        case 0:
                            infoExistingFeatureOption(geoPackage, featureRow, title, geomData);
                            break;
                        case 1:
                            tempEditFeatureMarker = marker;
                            validateAndClearEditFeatures(EditType.EDIT_FEATURE);
                            break;
                        case 2:
                            deleteExistingFeatureOption(title, featureRow, marker, geometryType);
                            break;
                        default:
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    /**
     * Get a title from the Geometry Type and marker
     *
     * @param geometryType The geometry type of the marker.
     * @param marker       The marker to get the title for.
     * @return The title.
     */
    private String getTitle(GeometryType geometryType, Marker marker) {
        LatLng position = marker.getPosition();
        DecimalFormat formatter = new DecimalFormat("0.0###");
        return geometryType.getName() + "\n(lat="
                + formatter.format(position.latitude) + ", lon="
                + formatter.format(position.longitude) + ")";
    }

    /**
     * Info feature click
     *
     * @param markerFeature The feature of the marker.
     */
    private void infoFeatureClick(MarkerFeature markerFeature) {
        Intent intent = new Intent(getContext(), FeatureViewActivity.class);
        intent.putExtra(String.valueOf(R.string.marker_feature_param), markerFeature);
        startActivity(intent);
    }

    /**
     * Info existing feature option
     *
     * @param geoPackage The geoPackage.
     * @param featureRow The feature row.
     * @param title      The title for the pop up.
     * @param geomData   The geometry info.
     */
    private void infoExistingFeatureOption(final GeoPackage geoPackage,
                                           FeatureRow featureRow,
                                           String title,
                                           GeoPackageGeometryData geomData) {

        DataColumnsDao dataColumnsDao = (new SchemaExtension(geoPackage)).getDataColumnsDao();
        try {
            if (!dataColumnsDao.isTableExists()) {
                dataColumnsDao = null;
            }
        } catch (SQLException e) {
            dataColumnsDao = null;
            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                    "Failed to check if Data Columns table exists for GeoPackage: "
                            + geoPackage.getName(), e);
        }


        StringBuilder message = new StringBuilder();
        int geometryColumn = featureRow.getGeometryColumnIndex();
        for (int i = 0; i < featureRow.columnCount(); i++) {
            if (i != geometryColumn) {
                Object value = featureRow.getValue(i);
                if (value != null) {
                    String columnName = featureRow.getColumn(i).getName();
                    if (dataColumnsDao != null) {
                        try {
                            DataColumns dataColumn = dataColumnsDao.getDataColumn(featureRow.getTable().getTableName(), columnName);
                            if (dataColumn != null) {
                                columnName = dataColumn.getName();
                            }
                        } catch (SQLException e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    "Failed to search for Data Column name for column: " + columnName
                                            + ", Feature Table: " + featureRow.getTable().getTableName()
                                            + ", GeoPackage: " + geoPackage.getName(), e);
                        }
                    }
                    message.append(columnName).append(": ");
                    message.append(value);
                    message.append("\n");
                }
            }
        }

        if (message.length() > 0) {
            message.append("\n");
        }

        message.append(GeometryPrinter.getGeometryString(geomData
                .getGeometry()));

        if (getActivity() != null) {
            AlertDialog viewDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(title)
                    .setPositiveButton(getString(R.string.button_ok_label), (dialog, which) -> dialog.dismiss())
                    .setMessage(message).create();
            viewDialog.show();
        }
    }

    /**
     * Delete existing feature options
     *
     * @param title        The title for the pop up.
     * @param featureRow   The feature row.
     * @param marker       The marker.
     * @param geometryType The geometry type.
     */
    private void deleteExistingFeatureOption(final String title,
                                             final FeatureRow featureRow, final Marker marker,
                                             final GeometryType geometryType) {

        if (getActivity() != null) {
            final LatLng position = marker.getPosition();

            AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setCancelable(false)
                    .setTitle(
                            getString(R.string.edit_features_delete_label) + " "
                                    + title)
                    .setMessage(
                            getString(R.string.edit_features_delete_label) + " "
                                    + geometryType.getName() + " from "
                                    + model.getEditFeaturesDatabase() + " - "
                                    + model.getEditFeaturesTable() + " (lat="
                                    + position.latitude + ", lon="
                                    + position.longitude + ") ?")
                    .setPositiveButton(
                            getString(R.string.edit_features_delete_label),

                            (dialog, which) -> {
                                GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(model.getEditFeaturesDatabase());
                                try {

                                    FeatureDao featureDao = geoPackage
                                            .getFeatureDao(model.getEditFeaturesTable());
                                    featureDao.delete(featureRow);
                                    marker.remove();
                                    model.getEditFeatureIds().remove(marker.getId());
                                    GoogleMapShape featureObject = model.getEditFeatureObjects()
                                            .remove(marker.getId());
                                    if (featureObject != null) {
                                        featureObject.remove();
                                    }
                                    updateLastChange(geoPackage, featureDao);

                                    model.getActive().setModified(true);
                                } catch (Exception e) {
                                    if (GeoPackageUtils
                                            .isUnsupportedSQLiteException(e)) {
                                        GeoPackageUtils
                                                .showMessage(
                                                        getActivity(),
                                                        getString(R.string.edit_features_delete_label)
                                                                + " "
                                                                + geometryType
                                                                .getName(),
                                                        "GeoPackage contains unsupported SQLite function, module, or trigger for writing: " + e.getMessage());
                                    } else {
                                        GeoPackageUtils
                                                .showMessage(
                                                        getActivity(),
                                                        getString(R.string.edit_features_delete_label)
                                                                + " "
                                                                + geometryType
                                                                .getName(),
                                                        e.getMessage());
                                    }
                                }
                            })

                    .setNegativeButton(getString(R.string.button_cancel_label), (dialog, which) -> dialog.dismiss()).create();
            deleteDialog.show();
        }
    }

    /**
     * Expand the bounding box of the contents
     *
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @param geometry   geometry
     */
    private static void expandBounds(GeoPackage geoPackage, FeatureDao featureDao, Geometry geometry) {
        if (geometry != null) {
            try {
                Contents contents = featureDao.getGeometryColumns().getContents();
                BoundingBox boundingBox = contents.getBoundingBox();
                if (boundingBox != null) {
                    GeometryEnvelope envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
                    BoundingBox geometryBoundingBox = new BoundingBox(envelope);
                    BoundingBox unionBoundingBox = boundingBox.union(geometryBoundingBox);
                    contents.setBoundingBox(unionBoundingBox);
                    ContentsDao contentsDao = geoPackage.getContentsDao();
                    contentsDao.update(contents);
                }
            } catch (Exception e) {
                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                        "Failed to update contents bounding box. GeoPackage: "
                                + geoPackage.getName() + ", Table: " + featureDao.getTableName(), e);
            }
        }
    }

    /**
     * Update the last change date of the contents
     *
     * @param geoPackage The geoPackage.
     * @param featureDao The feature data access object.
     */
    private static void updateLastChange(GeoPackage geoPackage, FeatureDao featureDao) {
        try {
            Contents contents = featureDao.getGeometryColumns().getContents();
            contents.setLastChange(new Date());
            ContentsDao contentsDao = geoPackage.getContentsDao();
            contentsDao.update(contents);
        } catch (Exception e) {
            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                    "Failed to update contents last change date. GeoPackage: "
                            + geoPackage.getName() + ", Table: " + featureDao.getTableName(), e);
        }
    }

    /**
     * Get a list of the polygon points for the bounding box
     *
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The bounding box corners.
     */
    private List<LatLng> getPolygonPoints(LatLng point1, LatLng point2) {
        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(point1.latitude, point1.longitude));
        points.add(new LatLng(point1.latitude, point2.longitude));
        points.add(new LatLng(point2.latitude, point2.longitude));
        points.add(new LatLng(point2.latitude, point1.longitude));
        return points;
    }

    /**
     * Touchable map layout
     *
     * @author osbornb
     */
    public class TouchableMap extends FrameLayout {

        public TouchableMap(Context context) {
            super(context);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    if (boundingBoxMode) {
                        if (drawing && boundingBox != null) {
                            Point point = new Point((int) ev.getX(),
                                    (int) ev.getY());
                            boundingBoxEndCorner = map.getProjection()
                                    .fromScreenLocation(point);
                            List<LatLng> points = getPolygonPoints(
                                    boundingBoxStartCorner, boundingBoxEndCorner);
                            boundingBox.setPoints(points);
                        }
                        if (ev.getAction() == MotionEvent.ACTION_UP) {
                            setDrawing(false);
                        }
                    }
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }

    }

    /**
     * Get feature selection dialog
     *
     * @param editFeaturesSelectionView The view.
     * @param featuresInput             The features input spinner.
     * @param geoPackageInput           The geoPackage input spinner.
     * @return The dialog builder.
     */
    private AlertDialog.Builder getFeatureSelectionDialog(View editFeaturesSelectionView,
                                                          final Spinner geoPackageInput,
                                                          final Spinner featuresInput) {

        AlertDialog.Builder dialog = null;

        if (getActivity() != null) {
            dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            dialog.setView(editFeaturesSelectionView);

            boolean searchForActive = true;
            int defaultDatabase = 0;
            int defaultTable = 0;

            List<String> databases = geoPackageViewModel.getDatabases();
            List<String> featureDatabases = new ArrayList<>();
            if (databases != null) {
                for (String database : databases) {
                    GeoPackage geoPackage = geoPackageViewModel.getGeoPackage(database);
                    List<String> featureTables = geoPackage.getFeatureTables();
                    if (!featureTables.isEmpty()) {
                        featureDatabases.add(database);

                        if (searchForActive) {
                            for (int i = 0; i < featureTables.size(); i++) {
                                String featureTable = featureTables.get(i);
                                boolean isActive = model.getActive().exists(database, featureTable, GeoPackageTableType.FEATURE);
                                if (isActive) {
                                    defaultDatabase = featureDatabases.size() - 1;
                                    defaultTable = i;
                                    searchForActive = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (featureDatabases.isEmpty()) {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.edit_features_selection_features_label),
                        "No GeoPackages with features");
                return null;
            }
            ArrayAdapter<String> geoPackageAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.spinner_item,
                    featureDatabases);
            geoPackageInput.setAdapter(geoPackageAdapter);

            updateFeaturesSelection(featuresInput, featureDatabases.get(defaultDatabase));

            geoPackageInput.setSelection(defaultDatabase);
            featuresInput.setSelection(defaultTable);

            geoPackageInput
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        boolean firstTime = true;

                        @Override
                        public void onItemSelected(AdapterView<?> parentView,
                                                   View selectedItemView, int position, long id) {

                            if (firstTime) {
                                firstTime = false;
                            } else {
                                String geoPackage = geoPackageInput.getSelectedItem()
                                        .toString();
                                updateFeaturesSelection(featuresInput, geoPackage);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }
                    });

        }

        return dialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesCancelled() {
        loadTilesFinished();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesPostExecute(String result) {
        if (result != null && getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    GeoPackageUtils.showMessage(getActivity(),
                            getString(R.string.geopackage_create_tiles_label), result));
        }
        loadTilesFinished();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIndexerCancelled(String result) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIndexerPostExecute(String result) {
        if (result != null) {
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_table_index_features_label), result);
        }
        loadTilesFinished();
    }

    /**
     * When loading tiles is finished
     */
    private void loadTilesFinished() {
        // Make sure the geopackage source is being repopulated to get the new layer
        geoPackageViewModel.regenerateGeoPackageTableList();

        if (model.getActive().isModified()) {
            updateInBackground(false);
            if (boundingBox != null) {
                PolygonOptions polygonOptions = new PolygonOptions();
                polygonOptions.fillColor(boundingBox.getFillColor());
                polygonOptions.strokeColor(boundingBox.getStrokeColor());
                polygonOptions.addAll(boundingBox.getPoints());
                boundingBox = map.addPolygon(polygonOptions);
            }
        }
    }
}

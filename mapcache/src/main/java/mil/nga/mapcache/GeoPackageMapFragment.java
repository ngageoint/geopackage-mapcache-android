package mil.nga.mapcache;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import org.locationtech.proj4j.units.Units;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageCache;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.link.FeatureTileTableLinker;
import mil.nga.geopackage.extension.scale.TileScaling;
import mil.nga.geopackage.extension.scale.TileTableScaling;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.index.FeatureIndexListResults;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.index.MultipleFeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.map.MapUtils;
import mil.nga.geopackage.map.features.FeatureInfoBuilder;
import mil.nga.geopackage.map.features.StyleCache;
import mil.nga.geopackage.map.geom.FeatureShapes;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeConverter;
import mil.nga.geopackage.map.geom.GoogleMapShapeMarkers;
import mil.nga.geopackage.map.geom.GoogleMapShapeType;
import mil.nga.geopackage.map.geom.MultiLatLng;
import mil.nga.geopackage.map.geom.MultiMarker;
import mil.nga.geopackage.map.geom.MultiPolygon;
import mil.nga.geopackage.map.geom.MultiPolygonOptions;
import mil.nga.geopackage.map.geom.MultiPolyline;
import mil.nga.geopackage.map.geom.MultiPolylineOptions;
import mil.nga.geopackage.map.geom.PolygonHoleMarkers;
import mil.nga.geopackage.map.geom.ShapeMarkers;
import mil.nga.geopackage.map.geom.ShapeWithChildrenMarkers;
import mil.nga.geopackage.map.tiles.TileBoundingBoxMapUtils;
import mil.nga.geopackage.map.tiles.overlay.BoundedOverlay;
import mil.nga.geopackage.map.tiles.overlay.FeatureOverlay;
import mil.nga.geopackage.map.tiles.overlay.FeatureOverlayQuery;
import mil.nga.geopackage.map.tiles.overlay.GeoPackageOverlayFactory;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageFeatureOverlayTable;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTableType;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.listeners.DetailLayerClickListener;
import mil.nga.mapcache.load.DownloadTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.ImportTask;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.mapcache.load.ShareTask;
import mil.nga.mapcache.preferences.PreferencesActivity;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.listeners.OnDialogButtonClickListener;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.view.GeoPackageAdapter;
import mil.nga.mapcache.listeners.GeoPackageClickListener;
import mil.nga.mapcache.view.GeoPackageViewAdapter;
import mil.nga.mapcache.listeners.RecyclerViewClickListener;
import mil.nga.mapcache.view.detail.DetailActionUtil;
import mil.nga.mapcache.view.detail.DetailPageAdapter;
import mil.nga.mapcache.view.detail.DetailPageHeaderObject;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;
import mil.nga.mapcache.view.layer.LayerPageAdapter;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryEnvelopeBuilder;
import mil.nga.sf.util.GeometryPrinter;

/**
 * GeoPackage Map Fragment
 *
 * @author osbornb
 */
public class GeoPackageMapFragment extends Fragment implements
        OnMapReadyCallback, OnMapLongClickListener, OnMapClickListener, OnMarkerClickListener,
        OnMarkerDragListener, ILoadTilesTask, IIndexerTask, OnCameraIdleListener, OnDialogButtonClickListener {

    /**
     * Max features key for saving to preferences
     */
    private static final String MAX_FEATURES_KEY = "max_features_key";

    /**
     * Map type key for saving to preferences
     */
    private static final String MAP_TYPE_KEY = "map_type_key";

    /**
     * Key for using dark mode from preferences
     */
    private static final String SETTINGS_DARK_KEY = "dark_map";

    /**
     * Key for zoom icons being visible from shared preferences
     */
    private static final String SETTINGS_ZOOM_KEY = "zoom_icons";


    /**
     * Active GeoPackages
     */
    private GeoPackageDatabases active;

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
    private static View view;

    /**
     * Load tiles view
     */
    private static View loadTilesView;

    /**
     * Edit features view
     */
    private static View editFeaturesView;

    /**
     * Edit features polygon hole view
     */
    private static View editFeaturesPolygonHoleView;

    /**
     * True when the map is visible
     */
    private static boolean visible = false;

    /**
     * GeoPackage manager
     */
    private GeoPackageManager manager;

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
    private Lock updateLock = new ReentrantLock();

    /**
     * Mapping of open GeoPackages by name
     */
    private GeoPackageCache geoPackages;

    /**
     * Mapping of open GeoPackage feature DAOs
     */
    private Map<String, Map<String, FeatureDao>> featureDaos = new HashMap<>();

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
     * Edit features mode
     */
    private boolean editFeaturesMode = false;

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
     * Bounding box clear button
     */
    private ImageButton boundingBoxClearButton;

    /**
     * Edit Features menu item
     */
    private MenuItem editFeaturesMenuItem;

    /**
     * Edit features database
     */
    private String editFeaturesDatabase;

    /**
     * Edit features table
     */
    private String editFeaturesTable;

    /**
     * Feature shapes
     */
    private FeatureShapes featureShapes = new FeatureShapes();

    /**
     * Current zoom level
     */
    private int currentZoom = -1;

    /**
     * Flag indicating if the initial zoom is still needed
     */
    private boolean needsInitialZoom = true;

    /**
     * Mapping between marker ids and the feature ids
     */
    private Map<String, Long> editFeatureIds = new HashMap<String, Long>();

    /**
     * Marker feature
     */
    class MarkerFeature {
        long featureId;
        String database;
        String tableName;
    }

    /**
     * Mapping between marker ids and the features
     */
    private Map<String, MarkerFeature> markerIds = new HashMap<String, MarkerFeature>();

    /**
     * Mapping between marker ids and feature objects
     */
    private Map<String, GoogleMapShape> editFeatureObjects = new HashMap<String, GoogleMapShape>();

    /**
     * Edit points type
     */
    private EditType editFeatureType = null;

    /**
     * Edit type enumeration
     */
    private enum EditType {

        POINT, LINESTRING, POLYGON, POLYGON_HOLE, EDIT_FEATURE;

    }

    /**
     * Map of edit point marker ids and markers
     */
    private Map<String, Marker> editPoints = new LinkedHashMap<String, Marker>();

    /**
     * Map of edit point hole marker ids and markers
     */
    private Map<String, Marker> editHolePoints = new LinkedHashMap<String, Marker>();

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
    private List<List<LatLng>> holePolygons = new ArrayList<List<LatLng>>();

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
     * Bounding box around the features on the map
     */
    private BoundingBox featuresBoundingBox;

    /**
     * Lock for concurrently updating the features bounding box
     */
    private Lock featuresBoundingBoxLock = new ReentrantLock();

    /**
     * Bounding box around the tiles on the map
     */
    private BoundingBox tilesBoundingBox;

    /**
     * True when a tile layer is drawn from features
     */
    private boolean featureOverlayTiles = false;

    /**
     * List of Feature Overlay Queries for querying tile overlay clicks
     */
    private List<FeatureOverlayQuery> featureOverlayQueries = new ArrayList<>();

    /**
     * GeoPackage name constant
     */
    public static final String GEO_PACKAGE_DETAIL = "mil.nga.mapcache.extra.GEOPACKAGEDETAIL";

    /**
     * View holding the recyler view list of geopackages
     */
    private RecyclerView geoPackageRecyclerView;

    /**
     * view adapter for the recycler view
     */
    private GeoPackageViewAdapter geoAdapter;

    /**
     * Views to show "no geopackages found" message when the list is empty
     */
    private TextView getStartedView;
    private LinearLayout emptyViewHolder;

    /**
     * Progress dialog for network operations
     */
    private ProgressDialog progressDialog;

    /**
     * Intent activity request code when choosing a file
     */
    public static final int ACTIVITY_CHOOSE_FILE = 3342;

    /**
     * Intent activity request code when opening app settings
     */
    public static final int ACTIVITY_APP_SETTINGS = 3344;

    /**
     * Intent activity request code when opening preferences menu
     */
    public static final int ACTIVITY_PREFERENCES = 3345;

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
     * Floating Action Button for creating geopackages
     */
    private FloatingActionButton fab;

    /**
     * Floating Action Button for new layers
     */
    private FloatingActionButton layerFab;

    /**
     * Task for importing a geopackage
     */
    private ImportTask importTask;

    /**
     * Adapter for the LayerDetailView in the Recycler
     */
    private LayerPageAdapter layerAdapter;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        geoPackageViewModel = ViewModelProviders.of(getActivity()).get(GeoPackageViewModel.class);
        geoPackageViewModel.init();

        active = new GeoPackageDatabases(getActivity().getApplicationContext(), "active");

        vibrator = (Vibrator) getActivity().getSystemService(
                Context.VIBRATOR_SERVICE);

        view = inflater.inflate(R.layout.fragment_map, container, false);
        getMapFragment().getMapAsync(this);

        touch = new TouchableMap(getActivity());
        touch.addView(view);

        manager = GeoPackageFactory.getManager(getContext().getApplicationContext());

        geoPackages = new GeoPackageCache(manager);

        // Set listeners for icons on map
        setIconListeners();

        // Util class for launching dialogs when clicking buttons on GeoPackage detail page
        detailButtonUtil = new DetailActionUtil(getActivity());

        // Floating action button
        layerFab = view.findViewById(R.id.layer_fab);
        fab = view.findViewById(R.id.bottom_sheet_fab);
        setFLoatingActionButton();
        setNewLayerFab();

        // Create the GeoPackage recycler view
        createGeoPackageRecycler();
        subscribeGeoPackageRecycler();

        // Show disclaimer
        showDisclaimer();

        return touch;
    }





    /**
     * Launch the preferences activity
     */
    public void launchPreferences(){
        Intent intent = new Intent(getContext(), PreferencesActivity.class);
        startActivityForResult(intent, ACTIVITY_PREFERENCES);
    }

    /**
     * Update after the settings activity is closed
     *
     * Note: instead of being called in the initial onCreateView, it gets called in onMapReady, because
     * we need the map to be initialized before we can set it to dark mode
     *
     */
    private void settingsUpdate(){
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        boolean darkMode = settings.getBoolean(SETTINGS_DARK_KEY, false);
        boolean zoomIconsVisible = settings.getBoolean(SETTINGS_ZOOM_KEY, false);

        setMapDarkMode(darkMode);
        setZoomIconsVisible(zoomIconsVisible);
    }





    /**
     * When the main activity gets a DETAIL_FRAGMENT_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE,
     * it forwards the request here.  This will search through our open fragments for the currently
     * open GeoPackageDetailDrawer fragment (there can only be one open at a time) and tell it to
     * execute a share task
     * @param requestCode Activity request code
     * @param permissions Requested permissions
     * @param grantResults Result of the request
     */
    public void giveSharePermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == MainActivity.DETAIL_FRAGMENT_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Find the open GeoPackage Detail fragment, and share it
                List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if(fragment instanceof GeoPackageDetailDrawer) {
                            ((GeoPackageDetailDrawer) fragment).shareGeopackage();
                        }
                    }
                }
            }
        }
    }







    /**
     * Sets the main RecyclerView to show the list of GeoPackages by setting the adapter
     */
    private void populateRecyclerWithGeoPackages(){
        layerFab.hide();
        fab.show();
        geoPackageRecycler.setAdapter(geoPackageRecyclerAdapter);
    }

    /**
     * Sets the main RecyclerView to show the details for a selected GeoPackage
     */
    private void populateRecyclerWithDetail(){
        layerFab.show();
        fab.hide();
        if(detailPageAdapter != null) {
            geoPackageRecycler.setAdapter(detailPageAdapter);
        }
    }

    /**
     * Sets the main RecyclerView to show the details for a selected layer from the GeoPackage
     * detail page
     * @param layerAdapter - A prepopulated adapter to populate with a layer's detail
     */
    private void populateRecyclerWithLayerDetail(LayerPageAdapter layerAdapter){
        layerFab.hide();
        fab.hide();
        if(layerAdapter != null){
            geoPackageRecycler.setAdapter(layerAdapter);
        }
    }

    /**
     * Populate the top level GeoPackage recyclerview with GeoPackage names
     */
    private void createGeoPackageRecycler(){
        geoPackageRecycler = (RecyclerView) view.findViewById(R.id.recycler_geopackages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        geoPackageRecycler.setLayoutManager(layoutManager);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(geoPackageRecycler);
//        behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

        GeoPackageClickListener geoClickListener = new GeoPackageClickListener() {
            @Override
            public void onClick(View view, int position, GeoPackageDatabase db) {
                createGeoPackageDetailAdapter(db);
            }
        };
        // Create the adapter and set it for the recyclerview
        geoPackageRecyclerAdapter = new GeoPackageAdapter(geoClickListener);
        populateRecyclerWithGeoPackages();

        // TODO: use the below touch helper to make a delete on swipe function
//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            // Called when a user swipes left or right on a ViewHolder
//            @Override
//            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
//                // Here is where you'll implement swipe to delete
//                int position = viewHolder.getAdapterPosition();
//                Log.i("position", "position: " + position);
//            }
//        }).attachToRecyclerView(geoPackageRecycler);
    }

    /**
     * Subscribe to populate the list of GeoPackages for the recyclerview.
     * Gets a list of GeoPackageTables and sends them to the adapter.
     * Also subscribes to the list of active tables.  When that is updated, the adapter will set
     * the active status for all GeoPackages in the RecyclerView
     */
    private void subscribeGeoPackageRecycler(){
        // Observe list of GeoPackages
        geoPackageViewModel.getGeos().observe(this, newGeos ->{
            // Set the visibility of the 'no geopackages found' message
            setListVisibility(newGeos.getDatabases().isEmpty());
            // If not empty, repopulate the list
            geoPackageRecyclerAdapter.clear();
            geoPackageRecyclerAdapter.insertDefaultHeader();
            for(GeoPackageDatabase db : newGeos.getDatabases()){
                geoPackageRecyclerAdapter.insertToEnd(db);
            }
            geoPackageRecyclerAdapter.notifyDataSetChanged();

            // Make sure the detail page is repopulated in case a new layer is added
            if(detailPageAdapter != null){
                detailPageAdapter.updateAllTables(newGeos, active);
            }
        });

        // Observe Active Tables - used to determine which layers are enabled.  Update main list
        // of geoPackages when a change is made in order to change the active state
        geoPackageViewModel.getActive().observe(this, newTables ->{
            active = newTables;
            geoPackageRecyclerAdapter.updateActiveTables(newTables.getDatabases());
            geoPackageRecyclerAdapter.notifyDataSetChanged();

            // if the detail page has been used, send the updated active list for it to update itself
            if(detailPageAdapter != null){
                detailPageAdapter.updateActiveTables(active);
            }

            // if the layer detail page has been created, send the updated active list for it to update itself
            if(layerAdapter != null){
                layerAdapter.updateActiveTables(active);
            }

            // Update the map
            if(newTables.isEmpty()){
                if(map != null){
                    map.clear();
                }
            } else{
                if(map != null) {
                    updateInBackground(true, false);
                }
            }
        });
    }


    /**
     * Populate the RecyclerView with details about a single GeoPackage, and generate click listeners
     * for the detail view
     * @param db - GeoPackageDatabase object of the GP that we're going to create the view for
     */
    private void createGeoPackageDetailAdapter(GeoPackageDatabase db){
        if(db != null) {
            // Listener for clicking on Layer
            DetailLayerClickListener layerListener = new DetailLayerClickListener() {
                @Override
                public void onClick(DetailPageLayerObject layerObject) {
                    createGeoPackageLayerDetailAdapter(layerObject);
                }
            };

            // Listener for clicking on Layer's active switch.  Sends the table and active state to the
            // repository to be stored in the active tables list
            LayerActiveSwitchListener activeLayerListener = new LayerActiveSwitchListener() {
                @Override
                public void onClick(boolean active, GeoPackageTable table) {
                    geoPackageViewModel.setLayerActive(table);
                }
            };

            // Listener to forward a button click on the detail header to the appropriate dialog function
            // Note: Layer name will be empty string for the GeoPackage detail page
            DetailActionListener detailActionListener = new DetailActionListener() {
                @Override
                public void onClick(View view, int actionType, String gpName, String layerName) {
                    openActionDialog(gpName, layerName, actionType);
                }
            };

            // Click listener for the back arrow on the detail header.  Resets the RecyclerView to
            // show GeoPackages
            View.OnClickListener detailBackListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    populateRecyclerWithGeoPackages();
                }
            };

            // Generate a list to pass to the adapter.  Should contain:
            // - A heaader: DetailPageHeaderObject
            // - N number of DetailPageLayerObject objects generated from the GeoPackageDatabase object
            DetailPageHeaderObject detailHeader = new DetailPageHeaderObject(db);
            List<Object> detailList = new ArrayList<>();
            detailList.add(detailHeader);
            detailList.addAll(db.getLayerObjects(active.getDatabase(db.getDatabase())));

            detailPageAdapter = new DetailPageAdapter(detailList, layerListener,
                    detailBackListener, detailActionListener, activeLayerListener, db);
            populateRecyclerWithDetail();
        }
    }


    /**
     * Create a view adapter to populate the RecyclerView with a Layer detail view (used when
     * clicking a Layer row from the GP Detail page)
     */
    private void createGeoPackageLayerDetailAdapter(DetailPageLayerObject layerObject){

        // Click listener for the back arrow on the layer page.  Resets the RecyclerView to
        // show the previous GeoPackage Detail view
        View.OnClickListener detailBackListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateRecyclerWithDetail();
            }
        };

        // Listener for clicking on Layer's active switch.  Sends the table and active state to the
        // repository to be stored in the active tables list
        LayerActiveSwitchListener activeLayerListener = new LayerActiveSwitchListener() {
            @Override
            public void onClick(boolean active, GeoPackageTable table) {
                geoPackageViewModel.setLayerActive(table);
            }
        };

        // Listener to forward a button click layer detail page to the appropriate dialog function
        DetailActionListener detailActionListener = new DetailActionListener(){
            @Override
            public void onClick(View view, int actionType, String gpName, String layerName) {
                openActionDialog(gpName, layerName, actionType);
            }
        };

        layerAdapter = new LayerPageAdapter(layerObject, detailBackListener,
                activeLayerListener, detailActionListener);
        populateRecyclerWithLayerDetail(layerAdapter);
    }





    /**
     * Ask the DetailButtonUtil to open a dialog to complete the action related to the button
     * that was clicked
     * @param gpName GeoPackage name
     * @param layerName Name of the layer to delete (will be empty string for anything but
     *                  DELETE_LAYER action
     * @param actionType ActionType enum
     */
    private void openActionDialog(String gpName, String layerName, int actionType){
        if(actionType == DetailActionListener.DETAIL_GP){
            detailButtonUtil.openDetailDialog(getActivity(), gpName, this);
        } else if(actionType == DetailActionListener.RENAME_GP){
            detailButtonUtil.openRenameDialog(getActivity(), gpName, this);
        } else if(actionType == DetailActionListener.SHARE_GP){
            detailButtonUtil.openShareDialog(getActivity(), gpName, this);
        } else if(actionType == DetailActionListener.COPY_GP){
            detailButtonUtil.openCopyDialog(getActivity(), gpName, this);
        } else if(actionType == DetailActionListener.DELETE_GP){
            detailButtonUtil.openDeleteDialog(getActivity(), gpName, this);
        } else if(actionType == DetailActionListener.DELETE_LAYER){
            detailButtonUtil.openDeleteLayerDialog(getActivity(), gpName, layerName, this);
        }
    }

    /**
     * Implement OnDialogButtonClickListener Detail button confirm click
     * Open a dialog with the GeoPackages advanced details
     * @param gpName - GeoPackage name
     */
    @Override
    public void onDetailGP(String gpName) {
        AlertDialog viewDialog = geoPackageViewModel.getGeoPackageDetailDialog(gpName, getActivity());
        viewDialog.show();
    }

    /**
     * Implement OnDialogButtonClickListener Rename button confirm click
     * Rename a GeoPackage and recreate the detailview adapter to make it refresh
     * @param oldName - GeoPackage original name
     * @param newName - New GeoPackage name
     */
    @Override
    public void onRenameGP(String oldName, String newName) {
        Log.i("click", "Rename GeoPackage from: " + oldName + " to: " + newName);
        try {
            if (geoPackageViewModel.setGeoPackageName(oldName, newName)) {
                // recreate the adapter and repopulate the recyclerview
                createGeoPackageDetailAdapter(geoPackageViewModel.getGeoByName(newName));
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.geopackage_rename_label),
                        "Rename from " + oldName + " to " + newName
                                + " was not successful");
            }
        } catch(Exception e){
            GeoPackageUtils.showMessage(getActivity(), getString(R.string.geopackage_rename_label),
                            e.getMessage());
        }
    }

    /**
     * Implement OnDialogButtonClickListener Share button confirm click
     * Kick off a share task with this GeoPackage
     * @param gpName - GeoPackage name
     */
    @Override
    public void onShareGP(String gpName) {
        ShareTask shareTask = new ShareTask(getActivity());
        shareTask.shareDatabaseOption(gpName);
    }

    /**
     * Implement OnDialogButtonClickListener Copy button confirm click
     * Copy a GeoPackage in the repository and replace the recyclerview with the geopackages list
     * @param gpName - GeoPackage name
     */
    @Override
    public void onCopyGP(String gpName, String newName) {
        Log.i("click", "Copy Geopackage");
        try {
            if (geoPackageViewModel.copyGeoPackage(gpName, newName)) {
                populateRecyclerWithGeoPackages();
            }else{
                GeoPackageUtils.showMessage(getActivity(),
                                getString(R.string.geopackage_copy_label),"Copy from "
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
     * @param gpName - GeoPackage name
     */
    @Override
    public void onDeleteGP(String gpName) {
        // remove any active layers drawn on map
        geoPackageViewModel.removeActiveTableLayers(gpName);
        // Delete the geopackage and take us back to the GeoPackage list
        if(geoPackageViewModel.deleteGeoPackage(gpName)){
            populateRecyclerWithGeoPackages();
        }
    }

    /**
     * Ask the viewModel to remove the given layer name from the given GeoPackage name
     * @param gpName GeoPackage name
     * @param layerName Layer name to delete
     */
    @Override
    public void onDeleteLayer(String gpName, String layerName){
        //TODO:
        // Remove from active layers
        // Mark it for delete
        // Return the DB to repopuplate recycler
        // send background task to delete from manager in repository



        // TODO: instead of waiting for a GeoPackageDatabase to come back from the repository, and
        // recreating the adapter, see if you can just update the detailAdapter when the active
        // layers live data is updated



        // First remove it from the active layers
        geoPackageViewModel.removeActiveLayer(gpName, layerName);
        // Ask the repository to delete the layer
        GeoPackageDatabase db = geoPackageViewModel.removeLayerFromGeo(gpName, layerName);
        if(db != null){
            createGeoPackageDetailAdapter(db);
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
     * @param view
     */
    public void openMapSelect(View view){
        PopupMenu pm = new PopupMenu(getActivity(), mapSelectButton);
        // Needed to make the icons visible
        try {
            Method method = pm.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            //method.setAccessible(true);
            method.invoke(pm.getMenu(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pm.getMenuInflater().inflate(R.menu.popup_map_type, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.map:
                        setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        return true;

                    case R.id.satellite:
                        setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        return true;

                    case R.id.terrain:
                        setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        return true;
                }

                return true;
            }
        });
        pm.show();
    }




    /**
     * Pop up menu for editing geoapackage - drawing features, bounding box, etc
     * @param view
     */
    public void openEditMenu(View view){
        PopupMenu pm = new PopupMenu(getActivity(), editFeaturesButton);
        // Needed to make the icons visible
        try {
            Method method = pm.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            //method.setAccessible(true);
            method.invoke(pm.getMenu(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pm.getMenuInflater().inflate(R.menu.popup_edit_menu, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.zoomToActive:
                        zoomToActive();
                        return true;

                    case R.id.features:
                        editFeaturesMenuItem = item;
                        if (!editFeaturesMode) {
                            selectEditFeatures();
                        } else {
                            resetEditFeatures();
                            updateInBackground(false, true);
                        }
                        return true;

                    case R.id.boundingBox:
                        boundingBoxMenuItem = item;
                        if (!boundingBoxMode) {

                            if (editFeaturesMode) {
                                resetEditFeatures();
                                updateInBackground(false, true);
                            }

                            boundingBoxMode = true;
                            loadTilesView.setVisibility(View.VISIBLE);
                        } else {
                            resetBoundingBox();
                        }
                        return true;

                    case R.id.maxFeatures:
                        setMaxFeatures();
                        return true;

                    case R.id.clearAllActive:
                        clearAllActive();
                        return true;

                }

                return true;
            }
        });
        pm.show();
    }




    /**
     *  Zoom in on map
     */
    public void zoomIn(){
        if (map == null) return;
        map.animateCamera(CameraUpdateFactory.zoomIn());
    }


    /**
     *  Zoom out on map
     */
    public void zoomOut(){
        if (map == null) return;
        map.animateCamera(CameraUpdateFactory.zoomOut());
    }



    /**
     * Set Floating action button to open the create new geopackage wizard
     */
    private void setFLoatingActionButton(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewWizard();
            }
        });
    }

    /**
     * Set Floating action button to create new layers
     */
    private void setNewLayerFab(){
        layerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newLayerWizard();
            }
        });
    }


    /**
     * Sets the visibility of the recycler view vs "no geopackages found" message bases on the
     * recycler view being empty
     */
    private void setListVisibility(boolean empty){
        emptyViewHolder = (LinearLayout) view.findViewById(R.id.empty_list_holder);
        getStartedView = (TextView) view.findViewById(R.id.geo_get_started);

        // Give the get started message a listener
        getStartedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewWizard();
            }
        });

        // Set the visibility
        if(empty){
            emptyViewHolder.setVisibility(View.VISIBLE);
        } else{
            emptyViewHolder.setVisibility(View.GONE);
            geoPackageRecycler.setVisibility(View.VISIBLE);

        }
    }


    /**
     *  Creates listeners for map icon buttons
     */
    public void setIconListeners(){
        // Create listeners for map view icon button
        setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapSelectButton = (ImageButton) view.findViewById(R.id.mapTypeIcon);
        mapSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapSelect(v);
            }
        });

        // Edit icon for editing features
        editFeaturesButton = (ImageButton) view.findViewById(R.id.editFeaturesIcon);
        editFeaturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditMenu(v);
            }
        });

        zoomInButton = (ImageButton) view.findViewById(R.id.zoomInIcon);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn();
            }
        });

        zoomOutButton = (ImageButton) view.findViewById(R.id.zoomOutIcon);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut();
            }
        });

        settingsIcon = (ImageButton) view.findViewById(R.id.settingsIcon);
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPreferences();
            }
        });

    }


    /**
     *  Disclaimer popup
     */
    private void showDisclaimer(){
        // Only show it if the user hasn't already accepted it before
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean disclaimerPref = sharedPreferences.getBoolean(getString(R.string.disclaimerPref), false);
        if(!disclaimerPref) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View disclaimerView = inflater.inflate(R.layout.disclaimer_window, null);
            Button acceptButton = (Button) disclaimerView.findViewById(R.id.accept_button);
            Button exitButton = (Button) disclaimerView.findViewById(R.id.exit_button);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setView(disclaimerView);
            final AlertDialog alertDialog = dialogBuilder.create();
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferences.edit().putBoolean(getString(R.string.disclaimerPref), true).commit();
                    alertDialog.dismiss();
                }
            });
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });

            // Prevent the dialog from closing when clicking outside the dialog or the back button
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        // do nothing
                    }
                    return true;
                }
            });
            alertDialog.show();
        }

    }




    /**
     *  Create wizard for Import or Create GeoPackage
     */
    private void createNewWizard(){

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.new_geopackage_wizard, null);
        // Logo and title
        ImageView closeLogo = (ImageView) alertView.findViewById(R.id.close_logo);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("New GeoPackage");

        // Initial dialog asking for create or import
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // Click listener for "Create New"
        ((TextView) alertView.findViewById(R.id.wizard_create))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createGeoPackage();
                        alertDialog.dismiss();
                    }
                });

        // Click listener for "Import URL"
        ((TextView) alertView.findViewById(R.id.wizard_import))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        importGeopackageFromUrl();
                        alertDialog.dismiss();
                    }
                });

        // Click listener for "Import from file"
        ((TextView) alertView.findViewById(R.id.wizard_import_file))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getImportPermissions();
                        alertDialog.dismiss();
                    }
                });

        // Click listener for close button
        closeLogo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }





    /**
     * Create a new GeoPackage
     */
    private void createGeoPackage() {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_add);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Create GeoPackage");
        // GeoPackage name
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        inputName.setSingleLine(true);
        inputName.setImeOptions(EditorInfo.IME_ACTION_DONE);

        final EditText input = new EditText(getActivity());

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = inputName.getText().toString();
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
                            }
                        })
                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });

        dialog.show();
    }


    /**
     * Pop up dialog for creating a new feature or tile layer from the geopackage detail view FAB
     */
    public void newLayerWizard(){
        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.new_layer_wizard, null);
        // Logo and title
        ImageView closeLogo = (ImageView) alertView.findViewById(R.id.new_layer_close_logo);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        TextView titleText = (TextView) alertView.findViewById(R.id.new_layer_title);
        titleText.setText("New GeoPackage Layer");

        // Initial dialog asking for create or import
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // Click listener for close button
        closeLogo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // Listener for create features
        TextView createFeature = (TextView) alertView.findViewById(R.id.create_feature);
        createFeature.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createFeatureOption();
                alertDialog.dismiss();
            }
        });

        // Listener for create tiles
        TextView createTile = (TextView) alertView.findViewById(R.id.create_tile);
        createTile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String geoName = detailPageAdapter.getGeoPackageName();
                if(geoName != null) {
                    createTilesOption(geoName);
                }
                alertDialog.dismiss();
            }
        });


        alertDialog.show();
    }



    /**
     * Create feature layer menu
     */
    private void createFeatureOption(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createFeaturesView = inflater.inflate(R.layout.create_features,
                null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(createFeaturesView);

        final EditText nameInput = (EditText) createFeaturesView
                .findViewById(R.id.create_features_name_input);
        final EditText minLatInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createFeaturesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final TextView preloadedLocationsButton = (TextView) createFeaturesView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner geometryTypeSpinner = (Spinner) createFeaturesView
                .findViewById(R.id.create_features_geometry_type);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        dialog.setPositiveButton(
                getString(R.string.geopackage_create_features_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_features_name_label)
                                                + " is required");
                            }
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
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
                            if(geoName != null){
                                if(!geoPackageViewModel.createFeatureTable(geoName, boundingBox, geometryType, tableName)){
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
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }




    /**
     * Create tiles option
     *
     * @param database
     */
    private void createTilesOption(final String database) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createTilesView = inflater.inflate(R.layout.create_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(createTilesView);

        final EditText nameInput = (EditText) createTilesView
                .findViewById(R.id.create_tiles_name_input);
        final EditText urlInput = (EditText) createTilesView
                .findViewById(R.id.load_tiles_url_input);
        final EditText epsgInput = (EditText) createTilesView
                .findViewById(R.id.load_tiles_epsg_input);
        final Button preloadedUrlsButton = (Button) createTilesView
                .findViewById(R.id.load_tiles_preloaded);
        final EditText minZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) createTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) createTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) createTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final TextView preloadedLocationsButton = (TextView) createTilesView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner tileScalingInput = (Spinner) createTilesView
                .findViewById(R.id.tile_scaling_type);
        final EditText tileScalingZoomOutInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_out_input);
        final EditText tileScalingZoomInInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_in_input);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, preloadedUrlsButton, nameInput, urlInput, epsgInput,
                compressFormatInput, compressQualityInput, true,
                maxFeaturesLabel, maxFeaturesInput, false, false,
                tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

        dialog.setPositiveButton(
                getString(R.string.geopackage_create_tiles_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_tiles_name_label)
                                                + " is required");
                            }
                            String tileUrl = urlInput.getText().toString();
                            long epsg = Long.valueOf(epsgInput.getText().toString());
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
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

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    minLat, maxLon, maxLat);

                            TileScaling scaling = GeoPackageUtils.getTileScaling(tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

                            // If not importing tiles, just create the table
                            if (tileUrl == null || tileUrl.isEmpty()) {
                                geoPackageViewModel.createTileTable(database, boundingBox, epsg, tableName, scaling);
                            } else {
                                // Load tiles
                                LoadTilesTask.loadTiles(getActivity(),
                                        GeoPackageMapFragment.this, active,
                                        database, tableName, tileUrl, minZoom,
                                        maxZoom, compressFormat,
                                        compressQuality, googleTiles,
                                        boundingBox, scaling,
                                        ProjectionConstants.AUTHORITY_EPSG, String.valueOf(epsg));
                                geoPackageViewModel.regenerateGeoPackageTableList();
                            }
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

    }









    /**
     * Make sure we have permissions to read/write to external before importing.  The result will
     * send MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL back up to mainactivity, and should
     * call importGeopackageFromFile after
     */
    private void getImportPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(R.string.storage_access_rational_title)
                    .setMessage(R.string.storage_access_rational_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL);
                        }
                    })
                    .create()
                    .show();

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.MANAGER_PERMISSIONS_REQUEST_ACCESS_IMPORT_EXTERNAL);
        }
    }




    /**
     * Import a GeoPackage from a file
     */
    public void importGeopackageFromFile() {

        try {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            Intent intent = Intent.createChooser(chooseFile,
                    "Choose a GeoPackage file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        } catch (Exception e) {
            // eat
        }
    }


    /**
     * Clear all active layers from the map and zoom out 1 level
     */
    private void clearAllActive(){
        geoPackageViewModel.clearAllActive();
//        zoomToZero();
        zoomOut();
    }







    /**
     * Import a GeoPackage from a URL
     */
    private void importGeopackageFromUrl() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View importUrlView = inflater.inflate(R.layout.import_url, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(importUrlView);

        // Text validation
        final TextInputLayout inputLayoutName = (TextInputLayout) importUrlView.findViewById(R.id.import_url_name_layout);
        final TextInputLayout inputLayoutUrl = (TextInputLayout) importUrlView.findViewById(R.id.import_url_layout);
        final TextInputEditText inputName = (TextInputEditText) importUrlView.findViewById(R.id.import_url_name_input);
        final TextInputEditText inputUrl = (TextInputEditText) importUrlView.findViewById(R.id.import_url_input);

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
                boolean newTextValid = validateInput(inputLayoutName, inputName);
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
                boolean newUrlValid = validateInput(inputLayoutUrl, inputUrl);
            }
        };
        inputUrl.addTextChangedListener(inputUrlWatcher);

        // Example Geopackages link handler
        ((TextView) importUrlView.findViewById(R.id.import_examples))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getActivity(), android.R.layout.select_dialog_item);
                        adapter.addAll(getResources().getStringArray(
                                R.array.preloaded_geopackage_url_labels));
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity(), R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(getString(R.string.import_url_preloaded_label));
                        builder.setAdapter(adapter,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        if (item >= 0) {
                                            String[] urls = getResources()
                                                    .getStringArray(
                                                            R.array.preloaded_geopackage_urls);
                                            String[] names = getResources()
                                                    .getStringArray(
                                                            R.array.preloaded_geopackage_url_names);
                                            inputName.setText(names[item]);
                                            inputUrl.setText(urls[item]);
                                        }
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

        dialog.setPositiveButton(getString(R.string.geopackage_import_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // This will be overridden by click listener after show is called
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();

        // Override the positive click listener to enable validation
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Validate input on both fields
                boolean nameValid = validateInput(inputLayoutName, inputName);
                boolean urlValid = validateInput(inputLayoutUrl, inputUrl);

                if(nameValid && urlValid) {
                    String database = inputName.getText().toString();
                    String url = inputUrl.getText().toString();
                    DownloadTask downloadTask = new DownloadTask(database, url, getActivity());

                    downloadTask.execute();
                    alertDialog.dismiss();
                }
                else if(!nameValid){
                    inputName.requestFocus();
                }
                else{
                    inputUrl.requestFocus();
                }

            }
        });


    }


    /**
     * Initiate an Import task (received from intent outside of application)
     */
    public void startImportTask(String name, Uri uri, String path, Intent intent){
        importTask = new ImportTask(getActivity(), intent);
        importTask.importGeoPackage(name, uri, path);
    }



    /**
     * Initiate an Import task with permissions(received from intent outside of application)
     */
    public void startImportTaskWithPermissions(String name, Uri uri, String path, Intent intent){
        importTask = new ImportTask(getActivity(), intent);
        importTask.importGeoPackageExternalLinkWithPermissions(name, uri, path);
    }



    /**
     * Import the GeoPackage by linking to the file after write external storage permission was granted
     *
     * @param granted
     */
    public void importGeoPackageExternalLinkAfterPermissionGranted(boolean granted) {
        if (granted) {
            importTask.importGeoPackageExternalLinkSavedData();
        } else {
            showDisabledExternalImportPermissionsDialog();
        }
    }


    /**
     * Show a disabled permissions dialog
     *
     * @param title
     * @param message
     */
    private void showDisabledPermissionsDialog(String title, String message) {
        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.settings, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                        startActivityForResult(intent, ACTIVITY_APP_SETTINGS);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }



    /**
     * Show a disabled external import permissions dialog when external GeoPackages can not be imported
     */
    private void showDisabledExternalImportPermissionsDialog() {
        // If the user has declared to no longer get asked about permissions
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showDisabledPermissionsDialog(
                    getResources().getString(R.string.external_import_geopackage_access_title),
                    getResources().getString(R.string.external_import_geopackage_access_message));
        }
    }





    /**
     * validate input
     * @param inputLayout
     * @return true if input is not empty and is valid
     */
    private boolean validateInput(TextInputLayout inputLayout, TextInputEditText inputName){
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayout.setError(inputLayout.getHint() + " " + getString(R.string.err_msg_invalid));
            return false;
        }
        return true;
    }







    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = true;

        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    // Import geopackage from file
                    ImportTask task = new ImportTask(getActivity(), data);
                    task.importFile();
                }
                break;
            case ACTIVITY_PREFERENCES:
                settingsUpdate();
                break;
            default:
                handled = false;
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }







    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        initializeMap();
    }

    /**
     * Initialize the map
     */
    private void initializeMap() {
        if (map == null) return;

        setLoadTilesView();
        setEditFeaturesView();

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        int mapType = settings.getInt(MAP_TYPE_KEY, 1);
        map.setMapType(mapType);

        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnCameraIdleListener(this);
        //map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                updateInBackground(true);
                mapLoaded = true;
            }
        });

        // Call the initial update to the settings
        settingsUpdate();
    }


    /**
     * Set the map color scheme to dark or default
     * @param makeDark
     */
    private void setMapDarkMode(boolean makeDark){
        if(map == null) return;

        if(makeDark){
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.dark_map));
        } else{
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.default_map));
        }
    }

    /**
     * Make the zoom in / zoom out icons visible
     */
    private void setZoomIconsVisible(boolean visible){
        if(visible){
            zoomInButton.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.VISIBLE);
        } else{
            zoomInButton.setVisibility(View.INVISIBLE);
            zoomOutButton.setVisibility(View.INVISIBLE);
        }
    }





    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraIdle() {

        // If visible & not editing a shape, update the feature shapes for the current map view region
        if (visible && (!editFeaturesMode || editFeatureType == null || (editPoints.isEmpty() && editFeatureMarker == null))) {

            int previousZoom = currentZoom;
            int zoom = (int) MapUtils.getCurrentZoom(map);
            currentZoom = zoom;
            if (zoom != previousZoom) {
                // Zoom level changed, remove all feature shapes except for markers
                featureShapes.removeShapesExcluding(GoogleMapShapeType.MARKER, GoogleMapShapeType.MULTI_MARKER);
            } else {
                // Remove shapes no longer visible on the map view
                featureShapes.removeShapesNotWithinMap(map);
            }

            BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
            double toleranceDistance = MapUtils.getToleranceDistance(view, map);
            int maxFeatures = getMaxFeatures();

            updateLock.lock();
            try {
                if (updateFeaturesTask != null) {
                    updateFeaturesTask.cancel(false);
                }
                updateFeaturesTask = new MapFeaturesUpdateTask();
                updateFeaturesTask.execute(false, maxFeatures, mapViewBoundingBox, toleranceDistance, true);
            } finally {
                updateLock.unlock();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    private SupportMapFragment getMapFragment() {
        FragmentManager fm = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            fm = getFragmentManager();
        } else {
            fm = getChildFragmentManager();
        }
        return (SupportMapFragment) fm.findFragmentById(R.id.fragment_map_view_ui);
    }

    /**
     * Set the load tiles view and buttons
     */
    private void setLoadTilesView() {
        loadTilesView = view.findViewById(R.id.mapLoadTilesButtons);
        ImageButton loadTilesButton = (ImageButton) loadTilesView
                .findViewById(R.id.mapLoadTilesButton);
        loadTilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createTiles();
            }
        });
        ImageButton loadFeatureTilesButton = (ImageButton) loadTilesView
                .findViewById(R.id.mapLoadFeaturesTilesButton);
        loadFeatureTilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createFeatureTiles();
            }
        });
        boundingBoxClearButton = (ImageButton) loadTilesView
                .findViewById(R.id.mapLoadTilesClearButton);
        boundingBoxClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                clearBoundingBox();
            }
        });
    }

    /**
     * Set the edit features view and buttons
     */
    private void setEditFeaturesView() {
        editFeaturesView = view.findViewById(R.id.mapFeaturesButtons);
        editFeaturesPolygonHoleView = view
                .findViewById(R.id.mapFeaturesPolygonHoleButtons);

        editPointButton = (ImageButton) editFeaturesView
                .findViewById(R.id.mapEditPointButton);
        editPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                validateAndClearEditFeatures(EditType.POINT);
            }
        });

        editLinestringButton = (ImageButton) editFeaturesView
                .findViewById(R.id.mapEditLinestringButton);
        editLinestringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                validateAndClearEditFeatures(EditType.LINESTRING);
            }
        });

        editPolygonButton = (ImageButton) editFeaturesView
                .findViewById(R.id.mapEditPolygonButton);
        editPolygonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                validateAndClearEditFeatures(EditType.POLYGON);
            }
        });

        editAcceptButton = (ImageButton) editFeaturesView
                .findViewById(R.id.mapEditAcceptButton);
        editAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

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
            }
        });

        editClearButton = (ImageButton) editFeaturesView
                .findViewById(R.id.mapEditClearButton);
        editClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!editPoints.isEmpty()
                        || editFeatureType == EditType.EDIT_FEATURE) {
                    if (editFeatureType == EditType.EDIT_FEATURE) {
                        editFeatureType = null;
                    }
                    clearEditFeaturesAndPreserveType();
                }
            }
        });

        editPolygonHolesButton = (ImageButton) editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleButton);
        editPolygonHolesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (editFeatureType != EditType.POLYGON_HOLE) {
                    editFeatureType = EditType.POLYGON_HOLE;
                    editPolygonHolesButton
                            .setImageResource(R.drawable.cut_hole_active);
                } else {
                    editFeatureType = EditType.POLYGON;
                    editPolygonHolesButton
                            .setImageResource(R.drawable.cut_hole);
                }

            }
        });

        editAcceptPolygonHolesButton = (ImageButton) editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleAcceptButton);
        editAcceptPolygonHolesButton
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (editHolePoints.size() >= 3) {
                            List<LatLng> latLngPoints = getLatLngPoints(editHolePoints);
                            holePolygons.add(latLngPoints);
                            clearEditHoleFeatures();
                            updateEditState(true);
                        }
                    }
                });

        editClearPolygonHolesButton = (ImageButton) editFeaturesPolygonHoleView
                .findViewById(R.id.mapEditPolygonHoleClearButton);
        editClearPolygonHolesButton
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        clearEditHoleFeatures();
                        updateEditState(true);
                    }
                });

    }

    /**
     * If there are unsaved edits prompt the user for validation. Clear edit
     * features if ok.
     *
     * @param editTypeClicked
     */
    private void validateAndClearEditFeatures(final EditType editTypeClicked) {

        if (editPoints.isEmpty() && editFeatureType != EditType.EDIT_FEATURE) {
            clearEditFeaturesAndUpdateType(editTypeClicked);
        } else {

            AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(
                            getString(R.string.edit_features_clear_validation_label))
                    .setMessage(
                            getString(R.string.edit_features_clear_validation_message))
                    .setPositiveButton(getString(R.string.button_ok_label),

                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (editFeatureType == EditType.EDIT_FEATURE) {
                                        editFeatureType = null;
                                    }
                                    clearEditFeaturesAndUpdateType(editTypeClicked);
                                }
                            })
                    .setOnCancelListener(
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    tempEditFeatureMarker = null;
                                }
                            })
                    .setNegativeButton(getString(R.string.button_cancel_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    tempEditFeatureMarker = null;
                                    dialog.dismiss();
                                }
                            }).create();
            deleteDialog.show();
        }
    }

    /**
     * Clear edit features and update the type
     *
     * @param editType
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
     * @param editType
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
                    Long featureId = editFeatureIds.get(editFeatureMarker.getId());
                    final GeoPackage geoPackage = manager
                            .open(editFeaturesDatabase, false);
                    try {
                        final FeatureDao featureDao = geoPackage
                                .getFeatureDao(editFeaturesTable);
                        final FeatureRow featureRow = featureDao
                                .queryForIdRow(featureId);
                        Geometry geometry = featureRow.getGeometry().getGeometry();
                        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                                featureDao.getProjection());
                        GoogleMapShape shape = converter.toShape(geometry);

                        editFeatureMarker.remove();
                        GoogleMapShape featureObject = editFeatureObjects
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
                    } finally {
                        if (geoPackage != null) {
                            geoPackage.close();
                        }
                    }

                    break;
            }
        }
    }

    /**
     * Add editable shape back after editing is complete
     */
    private void addEditableShapeBack() {

        Long featureId = editFeatureIds.get(editFeatureMarker.getId());
        final GeoPackage geoPackage = manager.open(editFeaturesDatabase, false);
        try {
            final FeatureDao featureDao = geoPackage
                    .getFeatureDao(editFeaturesTable);
            final FeatureRow featureRow = featureDao.queryForIdRow(featureId);
            GeoPackageGeometryData geomData = featureRow.getGeometry();
            if (geomData != null) {
                Geometry geometry = geomData.getGeometry();
                if (geometry != null) {
                    GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                            featureDao.getProjection());
                    GoogleMapShape shape = converter.toShape(geometry);
                    StyleCache styleCache = new StyleCache(geoPackage, getResources().getDisplayMetrics().density);
                    prepareShapeOptions(shape, styleCache, featureRow, true, true);
                    GoogleMapShape mapShape = GoogleMapShapeConverter
                            .addShapeToMap(map, shape);
                    addEditableShape(featureId, mapShape);
                    styleCache.clear();
                }
            }
        } finally {
            if (geoPackage != null) {
                geoPackage.close();
            }
        }
    }

    /**
     * Get the feature marker options for editing points
     *
     * @return
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
     * Get the feature marker options to edit polylines and polygons
     *
     * @return
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
     * @return
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

        GeoPackage geoPackage = manager.open(editFeaturesDatabase);
        EditType tempEditFeatureType = editFeatureType;
        try {
            FeatureDao featureDao = geoPackage.getFeatureDao(editFeaturesTable);
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
                    Long featureId = editFeatureIds.get(editFeatureMarker.getId());

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
                    active.setModified(true);

                    break;
            }
            indexer.close();

        } catch (Exception e) {
            if (GeoPackageUtils.isUnsupportedSQLiteException(e)) {
                GeoPackageUtils
                        .showMessage(
                                getActivity(),
                                getString(R.string.edit_features_save_label)
                                        + " " + editFeaturesTable,
                                "GeoPackage contains unsupported SQLite function, module, or trigger for writing: " + e.getMessage());
            } else {
                GeoPackageUtils.showMessage(getActivity(),
                        getString(R.string.edit_features_save_label) + " "
                                + tempEditFeatureType, e.getMessage());
            }
        } finally {
            if (geoPackage != null) {
                geoPackage.close();
            }
        }

        clearEditFeaturesAndPreserveType();

        if (changesMade) {
            active.setModified(true);
            updateInBackground(false, true);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        settingsUpdate();
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

        // If my location did not have permissions to update and the map is becoming visible, ask for permission
        if (!setMyLocationEnabled() && visible) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(R.string.location_access_rational_title)
                        .setMessage(R.string.location_access_rational_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MAP_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        if (visible && active.isModified()) {
            active.setModified(false);
            resetBoundingBox();
            resetEditFeatures();
            if (mapLoaded) {
                updateInBackground(true);
            }
        } else if (!visible) {
            updateLock.lock();
            try {
                if (updateTask != null) {
                    if (updateTask.getStatus() != AsyncTask.Status.FINISHED) {
                        updateTask.cancel(false);
                        active.setModified(true);
                    }
                    updateTask = null;
                }
                if (updateFeaturesTask != null) {
                    if (updateFeaturesTask.getStatus() != AsyncTask.Status.FINISHED) {
                        updateFeaturesTask.cancel(false);
                        active.setModified(true);
                    }
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
        if (map != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(visible);
            updated = true;
        }
        return updated;
    }

    /**
     * Handle the menu reset
     *
     * @param menu
     */
    public void handleMenu(Menu menu) {
        if (boundingBoxMode) {
            boundingBoxMenuItem = menu.findItem(R.id.map_bounding_box);
            if (boundingBoxMenuItem != null) {
                boundingBoxMenuItem.setIcon(R.drawable.ic_bounding_box_active);
            }
        }
        if (editFeaturesMode) {
            editFeaturesMenuItem = menu.findItem(R.id.map_features);
            if (editFeaturesMenuItem != null) {
                editFeaturesMenuItem.setIcon(R.drawable.ic_features_active);
            }
        }
    }

    /**
     * Handle map menu clicks
     *
     * @param item
     * @return
     */
    public boolean handleMenuClick(MenuItem item) {

        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.map_zoom:
                zoomToActive();
                break;
            case R.id.map_features:
                editFeaturesMenuItem = item;
                if (!editFeaturesMode) {
                    selectEditFeatures();
                } else {
                    resetEditFeatures();
                    updateInBackground(false, true);
                }
                break;
            case R.id.map_bounding_box:
                boundingBoxMenuItem = item;
                if (!boundingBoxMode) {

                    if (editFeaturesMode) {
                        resetEditFeatures();
                        updateInBackground(false, true);
                    }

                    boundingBoxMode = true;
                    loadTilesView.setVisibility(View.VISIBLE);
                    boundingBoxMenuItem.setIcon(R.drawable.ic_bounding_box_active);
                } else {
                    resetBoundingBox();
                }
                break;
            case R.id.max_features:
                setMaxFeatures();
                break;
//            case R.id.normal_map:
//                setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                break;
//            case R.id.satellite_map:
//                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                break;
//            case R.id.terrain_map:
//                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//                break;
//            case R.id.hybrid_map:
//                setMapType(GoogleMap.MAP_TYPE_HYBRID);
//                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

    /**
     * Select the features to edit
     */
    private void selectEditFeatures() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View editFeaturesSelectionView = inflater.inflate(
                R.layout.edit_features_selection, null);

        final Spinner geoPackageInput = (Spinner) editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_geopackage);
        final Spinner featuresInput = (Spinner) editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_features);

        AlertDialog.Builder dialog = getFeatureSelectionDialog(editFeaturesSelectionView,
                geoPackageInput, featuresInput);

        if (dialog != null) {

            dialog.setPositiveButton(getString(R.string.button_ok_label),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            try {

                                if (boundingBoxMode) {
                                    resetBoundingBox();
                                }

                                editFeaturesDatabase = geoPackageInput
                                        .getSelectedItem().toString();
                                editFeaturesTable = featuresInput.getSelectedItem()
                                        .toString();

                                editFeaturesMode = true;
                                editFeaturesView.setVisibility(View.VISIBLE);
                                editFeaturesMenuItem
                                        .setIcon(R.drawable.ic_features_active);

                                updateInBackground(false, true);

                            } catch (Exception e) {
                                GeoPackageUtils
                                        .showMessage(
                                                getActivity(),
                                                getString(R.string.edit_features_selection_features_label),
                                                e.getMessage());
                            }
                        }
                    }).setNegativeButton(getString(R.string.button_cancel_label),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog.show();
        }

    }

    /**
     * Update the features selection based upon the database
     *
     * @param featuresInput
     * @param database
     */
    private void updateFeaturesSelection(Spinner featuresInput, String database) {

        GeoPackage geoPackage = manager.open(database, false);
        List<String> features = geoPackage.getFeatureTables();
        geoPackage.close();
        ArrayAdapter<String> featuresAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_item, features);
        featuresInput.setAdapter(featuresAdapter);
    }

    /**
     * Reset the bounding box mode
     */
    private void resetBoundingBox() {
        boundingBoxMode = false;
        loadTilesView.setVisibility(View.INVISIBLE);
        if (boundingBoxMenuItem != null) {
//            boundingBoxMenuItem.setIcon(R.drawable.ic_bounding_box);
        }
        clearBoundingBox();
    }

    /**
     * Reset the edit features state
     */
    private void resetEditFeatures() {
        editFeaturesMode = false;
        editFeaturesView.setVisibility(View.INVISIBLE);
        if (editFeaturesMenuItem != null) {
//            editFeaturesMenuItem.setIcon(R.drawable.ic_features);
        }
        editFeaturesDatabase = null;
        editFeaturesTable = null;
        editFeatureIds.clear();
        editFeatureObjects.clear();
        editFeatureShape = null;
        editFeatureShapeMarkers = null;
        editFeatureMarker = null;
        tempEditFeatureMarker = null;
        clearEditFeatures();
    }

    /**
     * Turn off the loading of tiles
     */
    private void clearBoundingBox() {
        if (boundingBoxClearButton != null) {
            boundingBoxClearButton.setImageResource(R.drawable.cancel_changes);
        }
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

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_edit);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Max Active Features");
        // Set description
        TextView descText = (TextView) alertView.findViewById(R.id.alert_description);
        descText.setText("Limit the number of features to display in active layers for faster processing");
        descText.setVisibility(View.VISIBLE);
        // Set input to current max features value
        final EditText input = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        final String maxFeatures = String.valueOf(getMaxFeatures());
        input.setText(maxFeatures);
        input.setHint(maxFeatures);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                if (value != null && !value.equals(maxFeatures)) {
                                    int maxFeature = Integer.parseInt(value);
                                    SharedPreferences settings = PreferenceManager
                                            .getDefaultSharedPreferences(getActivity());
                                    Editor editor = settings.edit();
                                    editor.putInt(MAX_FEATURES_KEY, maxFeature);
                                    editor.commit();
                                    updateInBackground(false, true);
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });

        dialog.show();
    }

    /**
     * Get the max features
     *
     * @return
     */
    private int getMaxFeatures() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        int maxFeatures = settings.getInt(MAX_FEATURES_KEY, getResources()
                .getInteger(R.integer.map_max_features_default));
        return maxFeatures;
    }

    /**
     * Set the map type
     *
     * @param mapType
     */
    private void setMapType(int mapType) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        Editor editor = settings.edit();
        editor.putInt(MAP_TYPE_KEY, mapType);
        editor.commit();
        if (map != null) {
            map.setMapType(mapType);
        }
    }

    /**
     * Update the map by kicking off a background task
     *
     * @param zoom zoom flag
     */
    private void updateInBackground(boolean zoom) {
        updateInBackground(zoom, false);
    }

    /**
     * Update the map by kicking off a background task
     *
     * @param zoom   zoom flag
     * @param filter filter features flag
     */
    private void updateInBackground(boolean zoom, boolean filter) {

        MapUpdateTask localUpdateTask = null;
        updateLock.lock();
        try {
            if (updateTask != null) {
                updateTask.cancel(false);
            }
            if (updateFeaturesTask != null) {
                updateFeaturesTask.cancel(false);
            }
            updateTask = new MapUpdateTask();
            localUpdateTask = updateTask;
        } finally {
            updateLock.unlock();
        }

        map.clear();
        geoPackages.closeAll();
        featureDaos.clear();

        if (zoom) {
            zoomToActiveBounds();
        }

        featuresBoundingBox = null;
        tilesBoundingBox = null;
        featureOverlayTiles = false;
        featureOverlayQueries.clear();
        featureShapes.clear();
        markerIds.clear();
        int maxFeatures = getMaxFeatures();

        BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
        double toleranceDistance = MapUtils.getToleranceDistance(view, map);

        localUpdateTask.execute(zoom, maxFeatures, mapViewBoundingBox, toleranceDistance, filter);

    }

    /**
     * Zoom to the active feature and tile table data bounds
     */
    private void zoomToActiveBounds() {

        featuresBoundingBox = null;
        tilesBoundingBox = null;

        // Pre zoom
        List<GeoPackageDatabase> activeDatabases = new ArrayList<>();
//        activeDatabases.addAll(active.getDatabases());
        for (GeoPackageDatabase database : activeDatabases) {
            GeoPackage geoPackage = manager.open(database.getDatabase(), false);
            if (geoPackage != null) {

                Set<String> featureTableDaos = new HashSet<>();
                Collection<GeoPackageFeatureTable> features = database.getFeatures();
                if (!features.isEmpty()) {
                    for (GeoPackageFeatureTable featureTable : features) {
                        featureTableDaos.add(featureTable.getName());
                    }
                }

                for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                    if (featureOverlay.isActive()) {
                        featureTableDaos.add(featureOverlay.getFeatureTable());
                    }
                }

                if (!featureTableDaos.isEmpty()) {

                    ContentsDao contentsDao = geoPackage.getContentsDao();

                    for (String featureTable : featureTableDaos) {

                        if(featureTable != null && featureTable != "") {
                            try {
                                Contents contents = contentsDao.queryForId(featureTable);
                                BoundingBox contentsBoundingBox = contents.getBoundingBox();

                                if (contentsBoundingBox != null) {

                                    contentsBoundingBox = transformBoundingBoxToWgs84(contentsBoundingBox, contents.getSrs());

                                    if (featuresBoundingBox != null) {
                                        featuresBoundingBox = featuresBoundingBox.union(contentsBoundingBox);
                                    } else {
                                        featuresBoundingBox = contentsBoundingBox;
                                    }
                                }
                            } catch (SQLException e) {
                                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                        e.getMessage());
                            }
                        }
                    }
                }

                Collection<GeoPackageTileTable> tileTables = database.getTiles();
                if (!tileTables.isEmpty()) {

                    TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

                    for (GeoPackageTileTable tileTable : tileTables) {

                        try {
                            TileMatrixSet tileMatrixSet = tileMatrixSetDao.queryForId(tileTable.getName());
                            BoundingBox tileMatrixSetBoundingBox = tileMatrixSet.getBoundingBox();

                            tileMatrixSetBoundingBox = transformBoundingBoxToWgs84(tileMatrixSetBoundingBox, tileMatrixSet.getSrs());

                            if (tilesBoundingBox != null) {
                                tilesBoundingBox = tilesBoundingBox.union(tileMatrixSetBoundingBox);
                            } else {
                                tilesBoundingBox = tileMatrixSetBoundingBox;
                            }
                        } catch (SQLException e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    e.getMessage());
                        }
                    }
                }

                geoPackage.close();
            }
        }

        zoomToActive();
    }

    /**
     * Transform the bounding box in the spatial reference to a WGS84 bounding box
     *
     * @param boundingBox bounding box
     * @param srs         spatial reference system
     * @return bounding box
     */
    private BoundingBox transformBoundingBoxToWgs84(BoundingBox boundingBox, SpatialReferenceSystem srs) {

        mil.nga.sf.proj.Projection projection = srs.getProjection();
        if (projection.isUnit(Units.DEGREES)) {
            boundingBox = TileBoundingBoxUtils.boundDegreesBoundingBoxWithWebMercatorLimits(boundingBox);
        }
        ProjectionTransform transformToWebMercator = projection
                .getTransformation(
                        ProjectionConstants.EPSG_WEB_MERCATOR);
        BoundingBox webMercatorBoundingBox = boundingBox.transform(transformToWebMercator);
        ProjectionTransform transform = ProjectionFactory.getProjection(
                ProjectionConstants.EPSG_WEB_MERCATOR)
                .getTransformation(
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        boundingBox = webMercatorBoundingBox.transform(transform);
        return boundingBox;
    }

    /**
     * Update the map in the background
     */
    private class MapUpdateTask extends AsyncTask<Object, Void, Void> {

        /**
         * Zoom after update flag
         */
        private boolean zoom;

        /**
         * Max features to draw
         */
        private int maxFeatures;

        /**
         * Map view bounding box
         */
        private BoundingBox mapViewBoundingBox;

        /**
         * Tolerance distance for simplification
         */
        private double toleranceDistance;

        /**
         * Filter flag
         */
        private boolean filter;

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(Object... params) {
            zoom = (Boolean) params[0];
            maxFeatures = (Integer) params[1];
            mapViewBoundingBox = (BoundingBox) params[2];
            toleranceDistance = (Double) params[3];
            filter = (Boolean) params[4];
            update(this, zoom, maxFeatures, mapViewBoundingBox, toleranceDistance, filter);
            return null;
        }

    }

    /**
     * Update the map
     *
     * @param zoom
     * @param task
     * @param maxFeatures
     * @param mapViewBoundingBox
     * @param toleranceDistance
     * @param filter
     */
    private void update(MapUpdateTask task, boolean zoom, final int maxFeatures, BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {

        if (active != null) {

            // Open active GeoPackages and create feature DAOS, display tiles and feature tiles
            List<GeoPackageDatabase> activeDatabases = new ArrayList<>();
            activeDatabases.addAll(active.getDatabases());
            for (GeoPackageDatabase database : activeDatabases) {

                if (task.isCancelled()) {
                    break;
                }

                try {
                    GeoPackage geoPackage = geoPackages.getOrOpen(database.getDatabase(), false);

                if (geoPackage != null) {

                    Set<String> featureTableDaos = new HashSet<>();
                    Collection<GeoPackageFeatureTable> features = database.getFeatures();
                    if (!features.isEmpty()) {
                        for (GeoPackageFeatureTable featureTable : features) {
                            featureTableDaos.add(featureTable.getName());
                        }
                    }

                    for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                        if (featureOverlay.isActive()) {
                            featureTableDaos.add(featureOverlay.getFeatureTable());
                        }
                    }

                    if (!featureTableDaos.isEmpty()) {
                        Map<String, FeatureDao> databaseFeatureDaos = new HashMap<>();
                        featureDaos.put(database.getDatabase(), databaseFeatureDaos);
                        for (String featureTable : featureTableDaos) {

                            if (task.isCancelled()) {
                                break;
                            }

                            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                            databaseFeatureDaos.put(featureTable, featureDao);
                        }
                    }

                    // Display the tiles
                    for (GeoPackageTileTable tiles : database.getTiles()) {
                        if (task.isCancelled()) {
                            break;
                        }
                        try {
                            displayTiles(tiles);
                        } catch (Exception e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    e.getMessage());
                        }
                    }

                    // Display the feature tiles
                    for (GeoPackageFeatureOverlayTable featureOverlay : database.getFeatureOverlays()) {
                        if (task.isCancelled()) {
                            break;
                        }
                        if (featureOverlay.isActive()) {
                            try {
                                displayFeatureTiles(featureOverlay);
                            } catch (Exception e) {
                                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                        e.getMessage());
                            }
                        }
                    }

                } else {
                    active.removeDatabase(database.getDatabase(), false);
                }
                } catch (Exception e){
                    Log.i("Error", "Error opening geopackage: " + database.getDatabase());
                }
            }

            // Add features
            if (!task.isCancelled()) {
                updateLock.lock();
                try {
                    if (updateFeaturesTask != null) {
                        updateFeaturesTask.cancel(false);
                    }
                    updateFeaturesTask = new MapFeaturesUpdateTask();
                    updateFeaturesTask.execute(zoom, maxFeatures, mapViewBoundingBox, toleranceDistance, filter);
                } finally {
                    updateLock.unlock();
                }
            }

        }

    }

    /**
     * Update the map features in the background
     */
    private class MapFeaturesUpdateTask extends AsyncTask<Object, Object, Integer> {

        /**
         * Zoom after update flag
         */
        private boolean zoom;

        /**
         * Max features to draw
         */
        private int maxFeatures;

        /**
         * Map view bounding box
         */
        private BoundingBox mapViewBoundingBox;

        /**
         * Tolerance distance for simplification
         */
        private double toleranceDistance;

        /**
         * Filter flag
         */
        private boolean filter;

        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer doInBackground(Object... params) {
            zoom = (Boolean) params[0];
            maxFeatures = (Integer) params[1];
            mapViewBoundingBox = (BoundingBox) params[2];
            toleranceDistance = (Double) params[3];
            filter = (Boolean) params[4];
            int count = addFeatures(this, maxFeatures, mapViewBoundingBox, toleranceDistance, filter);
            return count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onProgressUpdate(Object... shapeUpdate) {

            long featureId = (Long) shapeUpdate[0];
            String database = (String) shapeUpdate[1];
            String tableName = (String) shapeUpdate[2];
            GoogleMapShape shape = (GoogleMapShape) shapeUpdate[3];

            synchronized (featureShapes) {

                if (!featureShapes.exists(featureId, database, tableName)) {

                    GoogleMapShape mapShape = GoogleMapShapeConverter.addShapeToMap(
                            map, shape);

                    if (editFeaturesMode) {
                        Marker marker = addEditableShape(featureId, mapShape);
                        if (marker != null) {
                            GoogleMapShape mapPointShape = new GoogleMapShape(GeometryType.POINT, GoogleMapShapeType.MARKER, marker);
                            featureShapes.addMapMetadataShape(mapPointShape, featureId, database, tableName);
                        }
                    } else {
                        addMarkerShape(featureId, database, tableName, mapShape);
                    }
                    featureShapes.addMapShape(mapShape, featureId, database, tableName);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Integer count) {

            if (needsInitialZoom || zoom) {
                zoomToActive(true);
                needsInitialZoom = false;
            }
        }

        /**
         * Add a shape to the map
         *
         * @param featureId
         * @param database
         * @param tableName
         * @param shape
         */
        public void addToMap(long featureId, String database, String tableName, GoogleMapShape shape) {
            publishProgress(new Object[]{featureId, database, tableName, shape});
        }

    }

    /**
     * Add features to the map
     *
     * @param task               udpate features task
     * @param maxFeatures        max features
     * @param mapViewBoundingBox map view bounding box
     * @param toleranceDistance  tolerance distance
     * @param filter             filter
     * @return feature count
     */
    private int addFeatures(MapFeaturesUpdateTask task, final int maxFeatures, BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {

        AtomicInteger count = new AtomicInteger();

        Map<String, List<String>> featureTables = new HashMap<>();
        if (editFeaturesMode) {
            List<String> databaseFeatures = new ArrayList<>();
            databaseFeatures.add(editFeaturesTable);
            featureTables.put(editFeaturesDatabase, databaseFeatures);
            GeoPackage geoPackage = geoPackages.getOrOpen(editFeaturesDatabase, false);
            Map<String, FeatureDao> databaseFeatureDaos = featureDaos.get(editFeaturesDatabase);
            if (databaseFeatureDaos == null) {
                databaseFeatureDaos = new HashMap<>();
                featureDaos.put(editFeaturesDatabase, databaseFeatureDaos);
            }
            FeatureDao featureDao = databaseFeatureDaos.get(editFeaturesTable);
            if (featureDao == null) {
                featureDao = geoPackage.getFeatureDao(editFeaturesTable);
                databaseFeatureDaos.put(editFeaturesTable, featureDao);
            }
        } else {
            for (GeoPackageDatabase database : active.getDatabases()) {
                if (!database.getFeatures().isEmpty()) {
                    List<String> databaseFeatures = new ArrayList<>();
                    featureTables.put(database.getDatabase(),
                            databaseFeatures);
                    for (GeoPackageTable features : database.getFeatures()) {
                        databaseFeatures.add(features.getName());
                    }
                }
            }
        }

        // Get the thread pool size, or 0 if single threaded
        int threadPoolSize = getActivity().getResources().getInteger(
                R.integer.map_update_thread_pool_size);

        // Create a thread pool for processing features
        ExecutorService threadPool = null;
        if (threadPoolSize > 0) {
            threadPool = Executors.newFixedThreadPool(threadPoolSize);
        }

        for (Map.Entry<String, List<String>> databaseFeaturesEntry : featureTables
                .entrySet()) {

            if (count.get() >= maxFeatures) {
                break;
            }

            String databaseName = databaseFeaturesEntry.getKey();

            if (geoPackages.has(databaseName)) {

                List<String> databaseFeatures = databaseFeaturesEntry.getValue();
                Map<String, FeatureDao> databaseFeatureDaos = featureDaos.get(databaseName);

                if (databaseFeatureDaos != null) {

                    GeoPackage geoPackage = geoPackages.get(databaseName);
                    StyleCache styleCache = new StyleCache(geoPackage, getResources().getDisplayMetrics().density);

                    for (String features : databaseFeatures) {

                        if (databaseFeatureDaos.containsKey(features)) {

                            displayFeatures(task, threadPool,
                                    geoPackage, styleCache, features, count,
                                    maxFeatures, editFeaturesMode, mapViewBoundingBox, toleranceDistance, filter);
                            if (task.isCancelled() || count.get() >= maxFeatures) {
                                break;
                            }
                        }
                    }

                    styleCache.clear();
                }
            }

            if (task.isCancelled()) {
                break;
            }
        }

        if (threadPool != null) {
            threadPool.shutdown();
            if (!task.isCancelled() && count.get() < maxFeatures) {
                try {
                    threadPool.awaitTermination(getActivity().getResources().getInteger(
                            R.integer.map_update_thread_pool_finish_wait),
                            TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.w(GeoPackageMapFragment.class.getSimpleName(), e);
                }
            }
        }

        return Math.min(count.get(), maxFeatures);
    }

    /**
     * Zoom to features on the map, or tiles if no features
     */
    private void zoomToActive() {
        zoomToActive(false);
    }

    /**
     * Zoom out to 0 over a 2 second animation period
     */
    private void zoomToZero(){
        map.animateCamera(CameraUpdateFactory.zoomTo(0), 2000, null);
    }

    /**
     * Zoom to features on the map, or tiles if no features
     *
     * @param nothingVisible zoom only if nothing is currently visible
     */
    private void zoomToActive(boolean nothingVisible) {

        BoundingBox bbox = featuresBoundingBox;
        boolean tileBox = false;

        float paddingPercentage;
        if (bbox == null) {
            bbox = tilesBoundingBox;
            tileBox = true;
            if (featureOverlayTiles) {
                paddingPercentage = getActivity().getResources().getInteger(
                        R.integer.map_feature_tiles_zoom_padding_percentage) * .01f;
            } else {
                paddingPercentage = getActivity().getResources().getInteger(
                        R.integer.map_tiles_zoom_padding_percentage) * .01f;
            }
        } else {
            paddingPercentage = getActivity().getResources().getInteger(
                    R.integer.map_features_zoom_padding_percentage) * .01f;
        }

        if (bbox != null) {

            boolean zoomToActive = true;
            if (nothingVisible) {
                BoundingBox mapViewBoundingBox = MapUtils.getBoundingBox(map);
                if (TileBoundingBoxUtils.overlap(bbox, mapViewBoundingBox, ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH) != null) {

                    double longitudeDistance = TileBoundingBoxMapUtils.getLongitudeDistance(bbox);
                    double latitudeDistance = TileBoundingBoxMapUtils.getLatitudeDistance(bbox);
                    double mapViewLongitudeDistance = TileBoundingBoxMapUtils.getLongitudeDistance(mapViewBoundingBox);
                    double mapViewLatitudeDistance = TileBoundingBoxMapUtils.getLatitudeDistance(mapViewBoundingBox);

                    if (mapViewLongitudeDistance > longitudeDistance && mapViewLatitudeDistance > latitudeDistance) {

                        double longitudeRatio = longitudeDistance / mapViewLongitudeDistance;
                        double latitudeRatio = latitudeDistance / mapViewLatitudeDistance;

                        double zoomAlreadyVisiblePercentage;
                        if (tileBox) {
                            zoomAlreadyVisiblePercentage = getActivity().getResources().getInteger(
                                    R.integer.map_tiles_zoom_already_visible_percentage) * .01f;
                        } else {
                            zoomAlreadyVisiblePercentage = getActivity().getResources().getInteger(
                                    R.integer.map_features_zoom_already_visible_percentage) * .01f;
                        }

                        if (longitudeRatio >= zoomAlreadyVisiblePercentage && latitudeRatio >= zoomAlreadyVisiblePercentage) {
                            zoomToActive = false;
                        }
                    }
                }
            }

            if (zoomToActive) {
                double minLatitude = Math.max(bbox.getMinLatitude(), ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE);
                double maxLatitude = Math.min(bbox.getMaxLatitude(), ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);

                LatLng lowerLeft = new LatLng(minLatitude, bbox.getMinLongitude());
                LatLng lowerRight = new LatLng(minLatitude, bbox.getMaxLongitude());
                LatLng topLeft = new LatLng(maxLatitude, bbox.getMinLongitude());
                LatLng topRight = new LatLng(maxLatitude, bbox.getMaxLongitude());

                if (lowerLeft.longitude == lowerRight.longitude) {
                    double adjustLongitude = lowerRight.longitude - .0000000000001;
                    lowerRight = new LatLng(minLatitude, adjustLongitude);
                    topRight = new LatLng(maxLatitude, adjustLongitude);
                }

                final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(lowerLeft);
                boundsBuilder.include(lowerRight);
                boundsBuilder.include(topLeft);
                boundsBuilder.include(topRight);

                View view = getView();
                int minViewLength = Math.min(view.getWidth(), view.getHeight());
                final int padding = (int) Math.floor(minViewLength
                        * paddingPercentage);

                try {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(), padding));
                } catch (Exception e) {
                    Log.w(GeoPackageMapFragment.class.getSimpleName(),
                            "Unable to move camera", e);
                }
            }
        }
    }

    /**
     * Display features
     *
     * @param task
     * @param threadPool
     * @param geoPackage
     * @param styleCache
     * @param features
     * @param count
     * @param maxFeatures
     * @param editable
     * @param mapViewBoundingBox
     * @param toleranceDistance
     * @param filter
     */
    private void displayFeatures(MapFeaturesUpdateTask task,
                                 ExecutorService threadPool, GeoPackage geoPackage, StyleCache styleCache, String features,
                                 AtomicInteger count, final int maxFeatures, final boolean editable,
                                 BoundingBox mapViewBoundingBox, double toleranceDistance, boolean filter) {

        // Get the GeoPackage and feature DAO
        String database = geoPackage.getName();
        FeatureDao featureDao = featureDaos.get(database).get(features);
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(featureDao.getProjection());

        converter.setSimplifyTolerance(toleranceDistance);

        if (!styleCache.getFeatureStyleExtension().has(features)) {
            styleCache = null;
        }

        count.getAndAdd(featureShapes.getFeatureIdsCount(database, features));

        if (!task.isCancelled() && count.get() < maxFeatures) {

            mil.nga.sf.proj.Projection mapViewProjection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

            FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
            if (filter && indexer.isIndexed()) {

                FeatureIndexResults indexResults = indexer.query(mapViewBoundingBox, mapViewProjection);
                BoundingBox complementary = mapViewBoundingBox.complementaryWgs84();
                if (complementary != null) {
                    FeatureIndexResults indexResults2 = indexer.query(complementary, mapViewProjection);
                    indexResults = new MultipleFeatureIndexResults(indexResults, indexResults2);
                }

                processFeatureIndexResults(task, threadPool, indexResults, database, featureDao, converter, styleCache,
                        count, maxFeatures, editable, filter);

            } else {

                BoundingBox filterBoundingBox = null;
                double filterMaxLongitude = 0;

                if (filter) {
                    mil.nga.sf.proj.Projection featureProjection = featureDao.getProjection();
                    ProjectionTransform projectionTransform = mapViewProjection.getTransformation(featureProjection);
                    BoundingBox boundedMapViewBoundingBox = mapViewBoundingBox.boundWgs84Coordinates();
                    BoundingBox transformedBoundingBox = boundedMapViewBoundingBox.transform(projectionTransform);
                    if (featureProjection.isUnit(Units.DEGREES)) {
                        filterMaxLongitude = ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH;
                    } else if (featureProjection.isUnit(Units.METRES)) {
                        filterMaxLongitude = ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH;
                    }
                    filterBoundingBox = transformedBoundingBox.expandCoordinates(filterMaxLongitude);
                }

                // Query for all rows
                FeatureCursor cursor = featureDao.queryForAll();
                try {
                    while (!task.isCancelled() && count.get() < maxFeatures
                            && cursor.moveToNext()) {
                        try {
                            FeatureRow row = cursor.getRow();

                            if (threadPool != null) {
                                // Process the feature row in the thread pool
                                FeatureRowProcessor processor = new FeatureRowProcessor(
                                        task, database, featureDao, row, count, maxFeatures, editable, converter,
                                        styleCache, filterBoundingBox, filterMaxLongitude, filter);
                                threadPool.execute(processor);
                            } else {

                                processFeatureRow(task, database, featureDao, converter, styleCache, row, count, maxFeatures, editable,
                                        filterBoundingBox, filterMaxLongitude, filter);
                            }
                        } catch (Exception e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    "Failed to display feature. database: " + database
                                            + ", feature table: " + features
                                            + ", row: " + cursor.getPosition(), e);
                        }
                    }

                } finally {
                    cursor.close();
                }
            }
            indexer.close();

        }

    }

    /**
     * Process the feature index results
     *
     * @param task
     * @param threadPool
     * @param indexResults
     * @param database
     * @param featureDao
     * @param converter
     * @param styleCache
     * @param count
     * @param maxFeatures
     * @param editable
     * @param filter
     */
    private void processFeatureIndexResults(MapFeaturesUpdateTask task, ExecutorService threadPool, FeatureIndexResults indexResults, String database, FeatureDao featureDao,
                                            GoogleMapShapeConverter converter, StyleCache styleCache, AtomicInteger count, final int maxFeatures, final boolean editable,
                                            boolean filter) {

        try {
            for (FeatureRow row : indexResults) {

                if (task.isCancelled() || count.get() >= maxFeatures) {
                    break;
                }

                try {

                    if (threadPool != null) {
                        // Process the feature row in the thread pool
                        FeatureRowProcessor processor = new FeatureRowProcessor(
                                task, database, featureDao, row, count, maxFeatures, editable, converter,
                                styleCache, null, 0, filter);
                        threadPool.execute(processor);
                    } else {

                        processFeatureRow(task, database, featureDao, converter, styleCache, row, count, maxFeatures, editable, null, 0, filter);
                    }

                } catch (Exception e) {
                    Log.e(GeoPackageMapFragment.class.getSimpleName(),
                            "Failed to display feature. database: " + database
                                    + ", feature table: " + featureDao.getTableName()
                                    + ", row id: " + row.getId(), e);
                }
            }
        } finally {
            indexResults.close();
        }
    }

    /**
     * Single feature row processor
     *
     * @author osbornb
     */
    private class FeatureRowProcessor implements Runnable {

        /**
         * Map update task
         */
        private final MapFeaturesUpdateTask task;

        /**
         * Database
         */
        private final String database;

        /**
         * Feature DAO
         */
        private final FeatureDao featureDao;

        /**
         * Feature row
         */
        private final FeatureRow row;

        /**
         * Total feature count
         */
        private final AtomicInteger count;

        /**
         * Total max features
         */
        private final int maxFeatures;

        /**
         * Editable shape flag
         */
        private final boolean editable;

        /**
         * Shape converter
         */
        private final GoogleMapShapeConverter converter;

        /**
         * Style Cache
         */
        private final StyleCache styleCache;

        /**
         * Filter bounding box
         */
        private final BoundingBox filterBoundingBox;

        /**
         * Max projection longitude
         */
        private final double maxLongitude;

        /**
         * Filter flag
         */
        private final boolean filter;

        /**
         * Constructor
         *
         * @param task
         * @param database
         * @param featureDao
         * @param row
         * @param count
         * @param maxFeatures
         * @param editable
         * @param converter
         * @param styleCache
         * @param filterBoundingBox
         * @param maxLongitude
         * @param filter
         */
        public FeatureRowProcessor(MapFeaturesUpdateTask task, String database, FeatureDao featureDao,
                                   FeatureRow row, AtomicInteger count, int maxFeatures,
                                   boolean editable, GoogleMapShapeConverter converter, StyleCache styleCache,
                                   BoundingBox filterBoundingBox, double maxLongitude, boolean filter) {
            this.task = task;
            this.database = database;
            this.featureDao = featureDao;
            this.row = row;
            this.count = count;
            this.maxFeatures = maxFeatures;
            this.editable = editable;
            this.converter = converter;
            this.styleCache = styleCache;
            this.filterBoundingBox = filterBoundingBox;
            this.maxLongitude = maxLongitude;
            this.filter = filter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            processFeatureRow(task, database, featureDao, converter, styleCache, row, count, maxFeatures,
                    editable, filterBoundingBox, maxLongitude, filter);
        }

    }

    /**
     * Process the feature row
     *
     * @param task
     * @param database
     * @param featureDao
     * @param converter
     * @param styleCache
     * @param row
     * @param count
     * @param maxFeatures
     * @param editable
     * @param boundingBox
     * @param maxLongitude
     * @param filter
     */
    private void processFeatureRow(MapFeaturesUpdateTask task, String database, FeatureDao featureDao,
                                   GoogleMapShapeConverter converter, StyleCache styleCache, FeatureRow row, AtomicInteger count,
                                   int maxFeatures, boolean editable, BoundingBox boundingBox, double maxLongitude,
                                   boolean filter) {

        boolean exists = false;
        synchronized (featureShapes) {
            exists = featureShapes.exists(row.getId(), database, featureDao.getTableName());
        }

        if (!exists) {

            GeoPackageGeometryData geometryData = row.getGeometry();
            if (geometryData != null && !geometryData.isEmpty()) {

                final Geometry geometry = geometryData.getGeometry();

                if (geometry != null) {

                    boolean passesFilter = true;

                    if (filter && boundingBox != null) {
                        GeometryEnvelope envelope = geometryData.getEnvelope();
                        if (envelope == null) {
                            envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
                        }
                        if (envelope != null) {
                            if (geometry.getGeometryType() == GeometryType.POINT) {
                                mil.nga.sf.Point point = (mil.nga.sf.Point) geometry;
                                passesFilter = TileBoundingBoxUtils.isPointInBoundingBox(point, boundingBox, maxLongitude);
                            } else {
                                BoundingBox geometryBoundingBox = new BoundingBox(envelope);
                                passesFilter = TileBoundingBoxUtils.overlap(boundingBox, geometryBoundingBox, maxLongitude) != null;
                            }
                        }
                    }

                    if (passesFilter && count.getAndIncrement() < maxFeatures) {
                        final long featureId = row.getId();
                        final GoogleMapShape shape = converter.toShape(geometry);
                        updateFeaturesBoundingBox(shape);
                        prepareShapeOptions(shape, styleCache, row, editable, true);
                        task.addToMap(featureId, database, featureDao.getTableName(), shape);
                    }
                }
            }
        }
    }

    /**
     * Update the features bounding box with the shape
     *
     * @param shape
     */
    private void updateFeaturesBoundingBox(GoogleMapShape shape) {
        try {
            featuresBoundingBoxLock.lock();
            if (featuresBoundingBox != null) {
                shape.expandBoundingBox(featuresBoundingBox);
            } else {
                featuresBoundingBox = shape.boundingBox();
            }
        } finally {
            featuresBoundingBoxLock.unlock();
        }
    }

    /**
     * Prepare the shape options
     *
     * @param shape      map shape
     * @param styleCache style cache
     * @param featureRow feature row
     * @param editable   editable flag
     * @param topLevel   top level flag
     */
    private void prepareShapeOptions(GoogleMapShape shape, StyleCache styleCache, FeatureRow featureRow, boolean editable,
                                     boolean topLevel) {

        FeatureStyle featureStyle = null;
        if (styleCache != null) {
            featureStyle = styleCache.getFeatureStyleExtension().getFeatureStyle(featureRow, shape.getGeometryType());
        }

        switch (shape.getShapeType()) {

            case LAT_LNG:
                LatLng latLng = (LatLng) shape.getShape();
                MarkerOptions markerOptions = getMarkerOptions(styleCache, featureStyle, editable, topLevel);
                markerOptions.position(latLng);
                shape.setShape(markerOptions);
                shape.setShapeType(GoogleMapShapeType.MARKER_OPTIONS);
                break;

            case POLYLINE_OPTIONS:
                PolylineOptions polylineOptions = (PolylineOptions) shape
                        .getShape();
                setPolylineOptions(styleCache, featureStyle, editable, polylineOptions);
                break;

            case POLYGON_OPTIONS:
                PolygonOptions polygonOptions = (PolygonOptions) shape.getShape();
                setPolygonOptions(styleCache, featureStyle, editable, polygonOptions);
                break;

            case MULTI_LAT_LNG:
                MultiLatLng multiLatLng = (MultiLatLng) shape.getShape();
                MarkerOptions sharedMarkerOptions = getMarkerOptions(styleCache, featureStyle, editable,
                        false);
                multiLatLng.setMarkerOptions(sharedMarkerOptions);
                break;

            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shape
                        .getShape();
                PolylineOptions sharedPolylineOptions = new PolylineOptions();
                setPolylineOptions(styleCache, featureStyle, editable, sharedPolylineOptions);
                multiPolylineOptions.setOptions(sharedPolylineOptions);
                break;

            case MULTI_POLYGON_OPTIONS:
                MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shape
                        .getShape();
                PolygonOptions sharedPolygonOptions = new PolygonOptions();
                setPolygonOptions(styleCache, featureStyle, editable, sharedPolygonOptions);
                multiPolygonOptions.setOptions(sharedPolygonOptions);
                break;

            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shape
                        .getShape();
                for (int i = 0; i < shapes.size(); i++) {
                    prepareShapeOptions(shapes.get(i), styleCache, featureRow, editable, false);
                }
                break;
            default:
        }

    }

    /**
     * Get marker options
     *
     * @param styleCache   style cache
     * @param featureStyle feature style
     * @param editable     editable flag
     * @param clickable    clickable flag
     * @return marker options
     */
    private MarkerOptions getMarkerOptions(StyleCache styleCache, FeatureStyle featureStyle, boolean editable, boolean clickable) {
        MarkerOptions markerOptions = new MarkerOptions();
        if (editable) {
            TypedValue typedValue = new TypedValue();
            if (clickable) {
                getResources().getValue(R.dimen.marker_edit_color, typedValue,
                        true);
            } else {
                getResources().getValue(R.dimen.marker_edit_read_only_color,
                        typedValue, true);
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue
                    .getFloat()));

        } else if (styleCache == null || !styleCache.setFeatureStyle(markerOptions, featureStyle)) {

            TypedValue typedValue = new TypedValue();
            getResources().getValue(R.dimen.marker_color, typedValue, true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(typedValue.getFloat()));
        }

        return markerOptions;
    }

    /**
     * Set the Polyline Option attributes
     *
     * @param styleCache      style cache
     * @param featureStyle    feature style
     * @param editable        editable flag
     * @param polylineOptions polyline options
     */
    private void setPolylineOptions(StyleCache styleCache, FeatureStyle featureStyle, boolean editable,
                                    PolylineOptions polylineOptions) {
        if (editable) {
            polylineOptions.color(ContextCompat.getColor(getActivity(), R.color.polyline_edit_color));
        } else if (styleCache == null || !styleCache.setFeatureStyle(polylineOptions, featureStyle)) {
            polylineOptions.color(ContextCompat.getColor(getActivity(), R.color.polyline_color));
        }
    }

    /**
     * Set the Polygon Option attributes
     *
     * @param styleCache     style cache
     * @param featureStyle   feature style
     * @param editable
     * @param polygonOptions
     */
    private void setPolygonOptions(StyleCache styleCache, FeatureStyle featureStyle, boolean editable,
                                   PolygonOptions polygonOptions) {
        if (editable) {
            polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_edit_color));
            polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_edit_fill_color));
        } else if (styleCache == null || !styleCache.setFeatureStyle(polygonOptions, featureStyle)) {
            polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_color));
            polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_fill_color));
        }
    }

    /**
     * Add editable shape
     *
     * @param featureId
     * @param shape
     * @return marker
     */
    private Marker addEditableShape(long featureId, GoogleMapShape shape) {

        Marker marker = null;

        if (shape.getShapeType() == GoogleMapShapeType.MARKER) {
            marker = (Marker) shape.getShape();
        } else {
            marker = getMarker(shape);
            if (marker != null) {
                editFeatureObjects.put(marker.getId(), shape);
            }
        }

        if (marker != null) {
            editFeatureIds.put(marker.getId(), featureId);
        }

        return marker;
    }

    /**
     * Add marker shape
     *
     * @param featureId
     * @param database
     * @param tableName
     * @param shape
     */
    private void addMarkerShape(long featureId, String database, String tableName, GoogleMapShape shape) {

        if (shape.getShapeType() == GoogleMapShapeType.MARKER) {
            Marker marker = (Marker) shape.getShape();
            MarkerFeature markerFeature = new MarkerFeature();
            markerFeature.database = database;
            markerFeature.tableName = tableName;
            markerFeature.featureId = featureId;
            markerIds.put(marker.getId(), markerFeature);
        }
    }

    /**
     * Get the first marker of the shape or create one at the location
     *
     * @param shape
     * @return
     */
    private Marker getMarker(GoogleMapShape shape) {

        Marker marker = null;

        switch (shape.getShapeType()) {

            case MARKER:
                Marker shapeMarker = (Marker) shape.getShape();
                marker = createEditMarker(shapeMarker.getPosition());
                break;

            case POLYLINE:
                Polyline polyline = (Polyline) shape.getShape();
                LatLng polylinePoint = polyline.getPoints().get(0);
                marker = createEditMarker(polylinePoint);
                break;

            case POLYGON:
                Polygon polygon = (Polygon) shape.getShape();
                LatLng polygonPoint = polygon.getPoints().get(0);
                marker = createEditMarker(polygonPoint);
                break;

            case MULTI_MARKER:
                MultiMarker multiMarker = (MultiMarker) shape.getShape();
                marker = createEditMarker(multiMarker.getMarkers().get(0)
                        .getPosition());
                break;

            case MULTI_POLYLINE:
                MultiPolyline multiPolyline = (MultiPolyline) shape.getShape();
                LatLng multiPolylinePoint = multiPolyline.getPolylines().get(0)
                        .getPoints().get(0);
                marker = createEditMarker(multiPolylinePoint);
                break;

            case MULTI_POLYGON:
                MultiPolygon multiPolygon = (MultiPolygon) shape.getShape();
                LatLng multiPolygonPoint = multiPolygon.getPolygons().get(0)
                        .getPoints().get(0);
                marker = createEditMarker(multiPolygonPoint);
                break;

            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shape
                        .getShape();
                for (GoogleMapShape listShape : shapes) {
                    marker = getMarker(listShape);
                    if (marker != null) {
                        break;
                    }
                }
                break;
            default:
        }

        return marker;
    }

    /**
     * Create an edit marker to edit polylines and polygons
     *
     * @param latLng
     * @return
     */
    private Marker createEditMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_shape_edit));
        TypedValue typedValueWidth = new TypedValue();
        getResources().getValue(R.dimen.shape_edit_icon_anchor_width,
                typedValueWidth, true);
        TypedValue typedValueHeight = new TypedValue();
        getResources().getValue(R.dimen.shape_edit_icon_anchor_height,
                typedValueHeight, true);
        markerOptions.anchor(typedValueWidth.getFloat(),
                typedValueHeight.getFloat());
        Marker marker = map.addMarker(markerOptions);
        return marker;
    }

    /**
     * Display tiles
     *
     * @param tiles
     */
    private void displayTiles(GeoPackageTileTable tiles) {

        GeoPackage geoPackage = geoPackages.get(tiles.getDatabase());

        TileDao tileDao = geoPackage.getTileDao(tiles.getName());

        TileTableScaling tileTableScaling = new TileTableScaling(geoPackage, tileDao);
        TileScaling tileScaling = tileTableScaling.get();

        BoundedOverlay overlay = GeoPackageOverlayFactory
                .getBoundedOverlay(tileDao, getResources().getDisplayMetrics().density, tileScaling);

        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();

        FeatureTileTableLinker linker = new FeatureTileTableLinker(geoPackage);
        List<FeatureDao> featureDaos = linker.getFeatureDaosForTileTable(tileDao.getTableName());
        for (FeatureDao featureDao : featureDaos) {

            // Create the feature tiles
            FeatureTiles featureTiles = new DefaultFeatureTiles(getActivity(), geoPackage, featureDao,
                    getResources().getDisplayMetrics().density);

            featureOverlayTiles = true;

            // Add the feature overlay query
            FeatureOverlayQuery featureOverlayQuery = new FeatureOverlayQuery(getActivity(), overlay, featureTiles);
            featureOverlayQueries.add(featureOverlayQuery);
        }

        // Set the tiles index to be -2 of it is behind features and tiles drawn from features
        int zIndex = -2;

        // If these tiles are linked to features, set the zIndex to -1 so they are placed before imagery tiles
        if (!featureDaos.isEmpty()) {
            zIndex = -1;
        }

        BoundingBox displayBoundingBox = tileMatrixSet.getBoundingBox();
        Contents contents = tileMatrixSet.getContents();
        BoundingBox contentsBoundingBox = contents.getBoundingBox();
        if (contentsBoundingBox != null) {
            ProjectionTransform transform = contents.getSrs().getProjection().getTransformation(tileMatrixSet.getSrs().getProjection());
            BoundingBox transformedContentsBoundingBox = contentsBoundingBox;
            if (!transform.isSameProjection()) {
                transformedContentsBoundingBox = transformedContentsBoundingBox.transform(transform);
            }
            displayBoundingBox = displayBoundingBox.overlap(transformedContentsBoundingBox);
        }

        displayTiles(overlay, displayBoundingBox, tileMatrixSet.getSrs(), zIndex, null);
    }

    /**
     * Display feature tiles
     *
     * @param featureOverlayTable
     */
    private void displayFeatureTiles(GeoPackageFeatureOverlayTable featureOverlayTable) {

        GeoPackage geoPackage = geoPackages.get(featureOverlayTable.getDatabase());
        FeatureDao featureDao = featureDaos.get(featureOverlayTable.getDatabase()).get(featureOverlayTable.getFeatureTable());

        BoundingBox boundingBox = new BoundingBox(featureOverlayTable.getMinLon(),
                featureOverlayTable.getMinLat(), featureOverlayTable.getMaxLon(), featureOverlayTable.getMaxLat());

        // Load tiles
        FeatureTiles featureTiles = new DefaultFeatureTiles(getActivity(), geoPackage, featureDao,
                getResources().getDisplayMetrics().density);
        if (featureOverlayTable.isIgnoreGeoPackageStyles()) {
            featureTiles.ignoreFeatureTableStyles();
        }

        featureTiles.setMaxFeaturesPerTile(featureOverlayTable.getMaxFeaturesPerTile());
        if (featureOverlayTable.getMaxFeaturesPerTile() != null) {
            featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(getActivity()));
        }

        Paint pointPaint = featureTiles.getPointPaint();
        pointPaint.setColor(Color.parseColor(featureOverlayTable.getPointColor()));

        pointPaint.setAlpha(featureOverlayTable.getPointAlpha());
        featureTiles.setPointRadius(featureOverlayTable.getPointRadius());

        Paint linePaint = featureTiles.getLinePaintCopy();
        linePaint.setColor(Color.parseColor(featureOverlayTable.getLineColor()));

        linePaint.setAlpha(featureOverlayTable.getLineAlpha());
        linePaint.setStrokeWidth(featureOverlayTable.getLineStrokeWidth());
        featureTiles.setLinePaint(linePaint);

        Paint polygonPaint = featureTiles.getPolygonPaintCopy();
        polygonPaint.setColor(Color.parseColor(featureOverlayTable.getPolygonColor()));

        polygonPaint.setAlpha(featureOverlayTable.getPolygonAlpha());
        polygonPaint.setStrokeWidth(featureOverlayTable.getPolygonStrokeWidth());
        featureTiles.setPolygonPaint(polygonPaint);

        featureTiles.setFillPolygon(featureOverlayTable.isPolygonFill());
        if (featureTiles.isFillPolygon()) {
            Paint polygonFillPaint = featureTiles.getPolygonFillPaintCopy();
            polygonFillPaint.setColor(Color.parseColor(featureOverlayTable.getPolygonFillColor()));

            polygonFillPaint.setAlpha(featureOverlayTable.getPolygonFillAlpha());
            featureTiles.setPolygonFillPaint(polygonFillPaint);
        }

        featureTiles.calculateDrawOverlap();

        FeatureOverlay featureOverlay = new FeatureOverlay(featureTiles);
        featureOverlay.setBoundingBox(boundingBox, ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM));
        featureOverlay.setMinZoom(featureOverlayTable.getMinZoom());
        featureOverlay.setMaxZoom(featureOverlayTable.getMaxZoom());

        // Get the tile linked overlay
        BoundedOverlay overlay = GeoPackageOverlayFactory.getLinkedFeatureOverlay(featureOverlay, geoPackage);

        GeometryColumns geometryColumns = featureDao.getGeometryColumns();
        Contents contents = geometryColumns.getContents();

        GeoPackageUtils.prepareFeatureTiles(featureTiles);

        featureOverlayTiles = true;

        FeatureOverlayQuery featureOverlayQuery = new FeatureOverlayQuery(getActivity(), overlay, featureTiles);
        featureOverlayQueries.add(featureOverlayQuery);

        displayTiles(overlay, contents.getBoundingBox(), contents.getSrs(), -1, boundingBox);
    }

    /**
     * Display tiles
     *
     * @param overlay
     * @param dataBoundingBox
     * @param srs
     * @param zIndex
     * @param specifiedBoundingBox
     */
    private void displayTiles(TileProvider overlay, BoundingBox dataBoundingBox, SpatialReferenceSystem srs, int zIndex, BoundingBox specifiedBoundingBox) {

        final TileOverlayOptions overlayOptions = new TileOverlayOptions();
        overlayOptions.tileProvider(overlay);
        overlayOptions.zIndex(zIndex);

        BoundingBox boundingBox = dataBoundingBox;
        if (boundingBox != null) {
            boundingBox = transformBoundingBoxToWgs84(boundingBox, srs);
        } else {
            boundingBox = new BoundingBox(-ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
                    ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE,
                    ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
                    ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);
        }

        if (specifiedBoundingBox != null) {
            boundingBox = boundingBox.overlap(specifiedBoundingBox);
        }

        if (tilesBoundingBox == null) {
            tilesBoundingBox = boundingBox;
        } else {
            tilesBoundingBox = tilesBoundingBox.union(boundingBox);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.addTileOverlay(overlayOptions);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapLongClick(LatLng point) {

        if (boundingBoxMode) {

            vibrator.vibrate(getActivity().getResources().getInteger(
                    R.integer.map_tiles_long_click_vibrate));

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
                if (boundingBoxClearButton != null) {
                    boundingBoxClearButton
                            .setImageResource(R.drawable.cancel_changes_active);
                }
            }
        } else if (editFeatureType != null) {
            if (editFeatureType == EditType.EDIT_FEATURE) {
                if (editFeatureShapeMarkers != null) {
                    vibrator.vibrate(getActivity().getResources().getInteger(
                            R.integer.edit_features_add_long_click_vibrate));
                    Marker marker = addEditPoint(point);
                    editFeatureShapeMarkers.addNew(marker);
                    editFeatureShape.add(marker, editFeatureShapeMarkers);
                    updateEditState(true);
                }
            } else {
                vibrator.vibrate(getActivity().getResources().getInteger(
                        R.integer.edit_features_add_long_click_vibrate));
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

    /**
     * Get the edit point marker options
     *
     * @param point
     * @return
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

        Marker marker = map.addMarker(markerOptions);

        return marker;
    }

    /**
     * Set the marker options for edit points
     *
     * @param markerOptions
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
     * @param markerOptions
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
     * @param markerOptions
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
     * @param updateAcceptClear
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
     * @return
     */
    private PolylineOptions getDrawPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(ContextCompat.getColor(getActivity(), R.color.polyline_draw_color));
        return polylineOptions;
    }

    /**
     * Get draw polygon options
     *
     * @return
     */
    private PolygonOptions getDrawPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_draw_color));
        polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_draw_fill_color));
        return polygonOptions;
    }

    /**
     * Get hold draw polygon options
     *
     * @return
     */
    private PolygonOptions getHoleDrawPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(ContextCompat.getColor(getActivity(), R.color.polygon_hole_draw_color));
        polygonOptions.fillColor(ContextCompat.getColor(getActivity(), R.color.polygon_hole_draw_fill_color));
        return polygonOptions;
    }

    /**
     * Get a list of points as LatLng
     *
     * @param markers
     * @return
     */
    private List<LatLng> getLatLngPoints(Map<String, Marker> markers) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (Marker editPoint : markers.values()) {
            points.add(editPoint.getPosition());
        }
        return points;
    }

    /**
     * Set the drawing value
     *
     * @param drawing
     */
    private void setDrawing(boolean drawing) {
        this.drawing = drawing;
        map.getUiSettings().setScrollGesturesEnabled(!drawing);
    }

    /**
     * Check if the point is within clicking distance to the lat lng corner
     *
     * @param projection
     * @param point
     * @param latLng
     * @param allowableScreenPercentage
     * @return
     */
    private boolean isWithinDistance(Projection projection, Point point,
                                     LatLng latLng, double allowableScreenPercentage) {
        Point point2 = projection.toScreenLocation(latLng);
        double distance = Math.sqrt(Math.pow(point.x - point2.x, 2)
                + Math.pow(point.y - point2.y, 2));

        boolean withinDistance = distance
                / Math.min(view.getWidth(), view.getHeight()) <= allowableScreenPercentage;
        return withinDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapClick(LatLng point) {

        if (!editFeaturesMode) {

            StringBuilder clickMessage = new StringBuilder();

            if (!featureOverlayQueries.isEmpty()) {
                for (FeatureOverlayQuery query : featureOverlayQueries) {
                    String message = query.buildMapClickMessage(point, view, map);
                    if (message != null) {
                        if (clickMessage.length() > 0) {
                            clickMessage.append("\n\n");
                        }
                        clickMessage.append(message);
                    }
                }
            }

            for (GeoPackageDatabase database : active.getDatabases()) {
                if (!database.getFeatures().isEmpty()) {

                    TypedValue screenPercentage = new TypedValue();
                    getResources().getValue(R.dimen.map_feature_click_screen_percentage, screenPercentage, true);
                    float screenClickPercentage = screenPercentage.getFloat();

                    BoundingBox clickBoundingBox = MapUtils.buildClickBoundingBox(point, view, map, screenClickPercentage);
                    clickBoundingBox = clickBoundingBox.expandWgs84Coordinates();
                    mil.nga.sf.proj.Projection clickProjection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

                    double tolerance = MapUtils.getToleranceDistance(point, view, map, screenClickPercentage);

                    for (GeoPackageTable features : database.getFeatures()) {

                        GeoPackage geoPackage = geoPackages.get(database.getDatabase());
                        Map<String, FeatureDao> databaseFeatureDaos = featureDaos.get(database.getDatabase());

                        if (geoPackage != null && databaseFeatureDaos != null) {

                            FeatureDao featureDao = databaseFeatureDaos.get(features.getName());

                            if (featureDao != null) {

                                FeatureIndexResults indexResults = null;

                                FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
                                if (indexer.isIndexed()) {

                                    indexResults = indexer.query(clickBoundingBox, clickProjection);
                                    BoundingBox complementary = clickBoundingBox.complementaryWgs84();
                                    if (complementary != null) {
                                        FeatureIndexResults indexResults2 = indexer.query(complementary, clickProjection);
                                        indexResults = new MultipleFeatureIndexResults(indexResults, indexResults2);
                                    }

                                } else {

                                    mil.nga.sf.proj.Projection featureProjection = featureDao.getProjection();
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
                                    FeatureCursor cursor = featureDao.queryForAll();
                                    try {

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

                                    } finally {
                                        cursor.close();
                                    }

                                    indexResults = listResults;
                                }
                                indexer.close();

                                if (indexResults.count() > 0) {
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

            if (clickMessage.length() > 0) {
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setMessage(clickMessage.toString())
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }
                        )
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

        if (editFeaturesMode) {

            // Handle clicks to edit contents of an existing feature
            if (editFeatureShape != null && editFeatureShape.contains(markerId)) {
                editFeatureShapeClick(marker);
                return true;
            }

            // Handle clicks on an existing feature in edit mode
            Long featureId = editFeatureIds.get(markerId);
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
            MarkerFeature markerFeature = markerIds.get(markerId);
            if (markerFeature != null) {
                infoFeatureClick(marker, markerFeature);
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDrag(Marker marker) {
        updateEditState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        updateEditState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        vibrator.vibrate(getActivity().getResources().getInteger(
                R.integer.edit_features_drag_long_click_vibrate));
    }

    /**
     * Edit feature shape marker click
     *
     * @param marker
     */
    private void editFeatureShapeClick(final Marker marker) {

        final ShapeMarkers shapeMarkers = editFeatureShape
                .getShapeMarkers(marker);
        if (shapeMarkers != null) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
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
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

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
                                ShapeWithChildrenMarkers shapeWithChildrenMarkers = (ShapeWithChildrenMarkers) shapeMarkers;
                                editFeatureShapeMarkers = shapeWithChildrenMarkers
                                        .createChild();
                                break;
                            default:
                        }
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
     * @param marker
     * @param points
     */
    private void editMarkerClick(final Marker marker,
                                 final Map<String, Marker> points) {

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

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                points.remove(marker.getId());
                                marker.remove();

                                updateEditState(true);

                            }
                        })

                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();
        deleteDialog.show();
    }

    /**
     * Edit existing feature click
     *
     * @param marker
     * @param featureId
     */
    private void editExistingFeatureClick(final Marker marker, long featureId) {
        final GeoPackage geoPackage = manager.open(editFeaturesDatabase, false);
        final FeatureDao featureDao = geoPackage
                .getFeatureDao(editFeaturesTable);

        final FeatureRow featureRow = featureDao.queryForIdRow(featureId);

        if (featureRow != null) {
            final GeoPackageGeometryData geomData = featureRow.getGeometry();
            final GeometryType geometryType = geomData.getGeometry()
                    .getGeometryType();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.select_dialog_item);
            adapter.add(getString(R.string.edit_features_info_label));
            adapter.add(getString(R.string.edit_features_edit_label));
            adapter.add(getString(R.string.edit_features_delete_label));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            final String title = getTitle(geometryType, marker);
            builder.setTitle(title);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (geoPackage != null) {
                        geoPackage.close();
                    }
                }
            });
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    if (item >= 0) {
                        switch (item) {
                            case 0:
                                infoExistingFeatureOption(geoPackage, featureRow, title, geomData);
                                break;
                            case 1:
                                tempEditFeatureMarker = marker;
                                validateAndClearEditFeatures(EditType.EDIT_FEATURE);
                                if (geoPackage != null) {
                                    geoPackage.close();
                                }
                                break;
                            case 2:
                                if (geoPackage != null) {
                                    geoPackage.close();
                                }
                                deleteExistingFeatureOption(title, editFeaturesDatabase,
                                        editFeaturesTable, featureRow, marker,
                                        geometryType);
                                break;
                            default:
                        }
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        } else {
            if (geoPackage != null) {
                geoPackage.close();
            }
        }
    }

    /**
     * Get a title from the Geometry Type and marker
     *
     * @param geometryType
     * @param marker
     * @return
     */
    private String getTitle(GeometryType geometryType, Marker marker) {
        LatLng position = marker.getPosition();
        DecimalFormat formatter = new DecimalFormat("0.0###");
        String title = geometryType.getName() + "\n(lat="
                + formatter.format(position.latitude) + ", lon="
                + formatter.format(position.longitude) + ")";
        return title;
    }

    /**
     * Info feature click
     *
     * @param marker
     * @param markerFeature
     */
    private void infoFeatureClick(final Marker marker, MarkerFeature markerFeature) {
        final GeoPackage geoPackage = manager.open(markerFeature.database, false);
        final FeatureDao featureDao = geoPackage
                .getFeatureDao(markerFeature.tableName);

        final FeatureRow featureRow = featureDao.queryForIdRow(markerFeature.featureId);

        if (featureRow != null) {
            final GeoPackageGeometryData geomData = featureRow.getGeometry();
            final GeometryType geometryType = geomData.getGeometry()
                    .getGeometryType();

            String title = getTitle(geometryType, marker);
            infoExistingFeatureOption(geoPackage, featureRow, title, geomData);
        } else {
            geoPackage.close();
        }
    }

    /**
     * Info existing feature option
     *
     * @param geoPackage
     * @param featureRow
     * @param title
     * @param geomData
     */
    private void infoExistingFeatureOption(final GeoPackage geoPackage,
                                           FeatureRow featureRow,
                                           String title,
                                           GeoPackageGeometryData geomData) {

        DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
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
        if (geoPackage != null) {
            geoPackage.close();
        }

        if (message.length() > 0) {
            message.append("\n");
        }

        message.append(GeometryPrinter.getGeometryString(geomData
                .getGeometry()));

        AlertDialog viewDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setPositiveButton(getString(R.string.button_ok_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage(message).create();
        viewDialog.show();
    }

    /**
     * Delete existing feature options
     *
     * @param title
     * @param database
     * @param table
     * @param featureRow
     * @param marker
     * @param geometryType
     */
    private void deleteExistingFeatureOption(final String title,
                                             final String database, final String table,
                                             final FeatureRow featureRow, final Marker marker,
                                             final GeometryType geometryType) {

        final LatLng position = marker.getPosition();

        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setCancelable(false)
                .setTitle(
                        getString(R.string.edit_features_delete_label) + " "
                                + title)
                .setMessage(
                        getString(R.string.edit_features_delete_label) + " "
                                + geometryType.getName() + " from "
                                + editFeaturesDatabase + " - "
                                + editFeaturesTable + " (lat="
                                + position.latitude + ", lon="
                                + position.longitude + ") ?")
                .setPositiveButton(
                        getString(R.string.edit_features_delete_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                GeoPackage geoPackage = manager.open(editFeaturesDatabase);
                                try {

                                    FeatureDao featureDao = geoPackage
                                            .getFeatureDao(editFeaturesTable);
                                    featureDao.delete(featureRow);
                                    marker.remove();
                                    editFeatureIds.remove(marker.getId());
                                    GoogleMapShape featureObject = editFeatureObjects
                                            .remove(marker.getId());
                                    if (featureObject != null) {
                                        featureObject.remove();
                                    }
                                    updateLastChange(geoPackage, featureDao);

                                    active.setModified(true);
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
                                } finally {
                                    if (geoPackage != null) {
                                        geoPackage.close();
                                    }
                                }
                            }
                        })

                .setNegativeButton(getString(R.string.button_cancel_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();
        deleteDialog.show();
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
     * @param geoPackage
     * @param featureDao
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
     * @param point1
     * @param point2
     * @return
     */
    private List<LatLng> getPolygonPoints(LatLng point1, LatLng point2) {
        List<LatLng> points = new ArrayList<LatLng>();
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
     * Create tiles
     */
    private void createTiles() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createTilesView = inflater
                .inflate(R.layout.map_create_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(createTilesView);

        final EditText geopackageInput = (EditText) createTilesView
                .findViewById(R.id.map_create_tiles_geopackage_input);
        final Button geopackagesButton = (Button) createTilesView
                .findViewById(R.id.map_create_tiles_preloaded);
        final EditText nameInput = (EditText) createTilesView
                .findViewById(R.id.create_tiles_name_input);
        final EditText urlInput = (EditText) createTilesView
                .findViewById(R.id.load_tiles_url_input);
        final EditText epsgInput = (EditText) createTilesView
                .findViewById(R.id.load_tiles_epsg_input);
        final Button preloadedUrlsButton = (Button) createTilesView
                .findViewById(R.id.load_tiles_preloaded);
        final EditText minZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) createTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) createTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) createTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final TextView preloadedLocationsButton = (TextView) createTilesView
                .findViewById(R.id.bounding_box_preloaded);
        final Spinner tileScalingInput = (Spinner) createTilesView
                .findViewById(R.id.tile_scaling_type);
        final EditText tileScalingZoomOutInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_out_input);
        final EditText tileScalingZoomInInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_in_input);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        boolean setZooms = true;

        if (boundingBox != null) {
            double minLat = 90.0;
            double minLon = 180.0;
            double maxLat = -90.0;
            double maxLon = -180.0;
            for (LatLng point : boundingBox.getPoints()) {
                minLat = Math.min(minLat, point.latitude);
                minLon = Math.min(minLon, point.longitude);
                maxLat = Math.max(maxLat, point.latitude);
                maxLon = Math.max(maxLon, point.longitude);
            }
            minLatInput.setText(String.valueOf(minLat));
            maxLatInput.setText(String.valueOf(maxLat));
            minLonInput.setText(String.valueOf(minLon));
            maxLonInput.setText(String.valueOf(maxLon));

            // Try to find a good zoom starting point
            ProjectionTransform webMercatorTransform = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                    .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
            BoundingBox bbox = new BoundingBox(minLon, minLat, maxLon, maxLat);
            BoundingBox webMercatorBoundingBox = bbox.transform(webMercatorTransform);
            int zoomLevel = TileBoundingBoxUtils.getZoomLevel(webMercatorBoundingBox);
            int maxZoomLevel = getActivity().getResources().getInteger(
                    R.integer.load_tiles_max_zoom_default);
            zoomLevel = Math.max(0, Math.min(zoomLevel, maxZoomLevel) - 2);
            minZoomInput.setText(String.valueOf(zoomLevel));
            maxZoomInput.setText(String.valueOf(maxZoomLevel));

            setZooms = false;
        }

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, preloadedUrlsButton, nameInput, urlInput, epsgInput,
                compressFormatInput, compressQualityInput, setZooms,
                maxFeaturesLabel, maxFeaturesInput, false, false,
                tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

        geopackagesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.select_dialog_item);
                final List<String> databases = getDatabases();
                adapter.addAll(databases);
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity(), R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getActivity()
                        .getString(
                                R.string.map_create_tiles_existing_geopackage_dialog_label));
                builder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item >= 0) {
                                    String database = databases.get(item);
                                    geopackageInput.setText(database);
                                }
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        dialog.setPositiveButton(
                getString(R.string.geopackage_create_tiles_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String database = geopackageInput.getText()
                                    .toString();
                            if (database == null || database.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.map_create_tiles_geopackage_label)
                                                + " is required");
                            }
                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.create_tiles_name_label)
                                                + " is required");
                            }
                            String tileUrl = urlInput.getText().toString();
                            long epsg = Long.valueOf(epsgInput.getText().toString());
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());
                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
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

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    minLat, maxLon, maxLat);

                            // Create the database if it doesn't exist
                            if (!manager.exists(database)) {
                                manager.create(database);
                            }

                            GeoPackageTable table = new GeoPackageTileTable(database, tableName, 0);
                            active.addTable(table);

                            TileScaling scaling = GeoPackageUtils.getTileScaling(tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

                            // Load tiles
                            LoadTilesTask.loadTiles(getActivity(),
                                    GeoPackageMapFragment.this, active,
                                    database, tableName, tileUrl, minZoom,
                                    maxZoom, compressFormat, compressQuality,
                                    googleTiles, boundingBox, scaling,
                                    ProjectionConstants.AUTHORITY_EPSG, String.valueOf(epsg));
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

    }

    /**
     * Create feature tiles
     */
    private void createFeatureTiles() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View editFeaturesSelectionView = inflater.inflate(
                R.layout.edit_features_selection, null);

        final Spinner geoPackageInput = (Spinner) editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_geopackage);
        final Spinner featuresInput = (Spinner) editFeaturesSelectionView
                .findViewById(R.id.edit_features_selection_features);

        AlertDialog.Builder dialog = getFeatureSelectionDialog(editFeaturesSelectionView,
                geoPackageInput, featuresInput);

        if (dialog != null) {

            dialog.setPositiveButton(getString(R.string.button_ok_label),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            try {

                                String database = geoPackageInput
                                        .getSelectedItem().toString();
                                String table = featuresInput.getSelectedItem()
                                        .toString();

                                createFeatureTiles(database, table);

                            } catch (Exception e) {
                                GeoPackageUtils
                                        .showMessage(
                                                getActivity(),
                                                getString(R.string.edit_features_selection_features_label),
                                                e.getMessage());
                            }
                        }
                    }).setNegativeButton(getString(R.string.button_cancel_label),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog.show();
        }

    }

    /**
     * Create tiles
     *
     * @param database
     * @param featureTable
     */
    private void createFeatureTiles(final String database, final String featureTable) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createTilesView = inflater.inflate(R.layout.feature_tiles, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(createTilesView);

        final TextView indexWarning = (TextView) createTilesView
                .findViewById(R.id.feature_tiles_index_warning);
        final EditText nameInput = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_name_input);
        final EditText minZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_min_zoom_input);
        final EditText maxZoomInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_zoom_input);
        final TextView maxFeaturesLabel = (TextView) createTilesView
                .findViewById(R.id.generate_tiles_max_features_label);
        final EditText maxFeaturesInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_max_features_input);
        final Spinner compressFormatInput = (Spinner) createTilesView
                .findViewById(R.id.generate_tiles_compress_format);
        final EditText compressQualityInput = (EditText) createTilesView
                .findViewById(R.id.generate_tiles_compress_quality);
        final RadioButton googleTilesRadioButton = (RadioButton) createTilesView
                .findViewById(R.id.generate_tiles_type_google_radio_button);
        final EditText minLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_latitude_input);
        final EditText maxLatInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_latitude_input);
        final EditText minLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_min_longitude_input);
        final EditText maxLonInput = (EditText) createTilesView
                .findViewById(R.id.bounding_box_max_longitude_input);
        final TextView preloadedLocationsButton = (TextView) createTilesView
                .findViewById(R.id.bounding_box_preloaded);
        final CheckBox ignoreGeoPackageStyles = (CheckBox) createTilesView
                .findViewById(R.id.feature_tiles_ignore_geopackage_styles);
        final EditText pointColor = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_color);
        final EditText pointAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_alpha);
        final EditText pointRadius = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_point_radius);
        final EditText lineColor = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_color);
        final EditText lineAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_alpha);
        final EditText lineStroke = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_line_stroke);
        final EditText polygonColor = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_color);
        final EditText polygonAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_alpha);
        final EditText polygonStroke = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_stroke);
        final CheckBox polygonFill = (CheckBox) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill);
        final EditText polygonFillColor = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_color);
        final EditText polygonFillAlpha = (EditText) createTilesView
                .findViewById(R.id.feature_tiles_draw_polygon_fill_alpha);
        final Spinner tileScalingInput = (Spinner) createTilesView
                .findViewById(R.id.tile_scaling_type);
        final EditText tileScalingZoomOutInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_out_input);
        final EditText tileScalingZoomInInput = (EditText) createTilesView
                .findViewById(R.id.tile_scaling_zoom_in_input);

        GeoPackageUtils
                .prepareBoundingBoxInputs(getActivity(), minLatInput,
                        maxLatInput, minLonInput, maxLonInput,
                        preloadedLocationsButton);

        boolean setZooms = true;

        if (boundingBox != null) {
            double minLat = 90.0;
            double minLon = 180.0;
            double maxLat = -90.0;
            double maxLon = -180.0;
            for (LatLng point : boundingBox.getPoints()) {
                minLat = Math.min(minLat, point.latitude);
                minLon = Math.min(minLon, point.longitude);
                maxLat = Math.max(maxLat, point.latitude);
                maxLon = Math.max(maxLon, point.longitude);
            }
            minLatInput.setText(String.valueOf(minLat));
            maxLatInput.setText(String.valueOf(maxLat));
            minLonInput.setText(String.valueOf(minLon));
            maxLonInput.setText(String.valueOf(maxLon));

            // Try to find a good zoom starting point
            ProjectionTransform webMercatorTransform = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                    .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
            BoundingBox bbox = new BoundingBox(minLon, minLat, maxLon, maxLat);
            BoundingBox webMercatorBoundingBox = bbox.transform(webMercatorTransform);
            int zoomLevel = TileBoundingBoxUtils.getZoomLevel(webMercatorBoundingBox);
            int maxZoomLevel = getActivity().getResources().getInteger(
                    R.integer.load_tiles_max_zoom_default);
            zoomLevel = Math.max(0, Math.min(zoomLevel, maxZoomLevel) - 1);
            minZoomInput.setText(String.valueOf(zoomLevel));
            maxZoomInput.setText(String.valueOf(maxZoomLevel));

            setZooms = false;
        }

        // Check if indexed
        GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
        GeoPackage geoPackage = manager.open(database, false);
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
        FeatureIndexManager indexer = new FeatureIndexManager(getActivity(), geoPackage, featureDao);
        boolean indexed = indexer.isIndexed();
        if (indexed) {
            indexWarning.setVisibility(View.GONE);
        }
        indexer.close();

        GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
                maxZoomInput, null, nameInput, null, null,
                compressFormatInput, compressQualityInput, setZooms,
                maxFeaturesLabel, maxFeaturesInput, true, indexed,
                tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

        // Set a default name
        nameInput.setText(featureTable + getString(R.string.feature_tiles_name_suffix));

        // Prepare the feature draw
        GeoPackageUtils.prepareFeatureDraw(getActivity(), geoPackage, featureTable, pointAlpha, lineAlpha, polygonAlpha, polygonFillAlpha,
                pointColor, lineColor, pointRadius, lineStroke,
                polygonColor, polygonStroke, polygonFill, polygonFillColor);

        // Close the GeoPackage
        geoPackage.close();

        dialog.setPositiveButton(
                getString(R.string.geopackage_table_create_feature_tiles_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {

                            String tableName = nameInput.getText().toString();
                            if (tableName == null || tableName.isEmpty()) {
                                throw new GeoPackageException(
                                        getString(R.string.feature_tiles_name_label)
                                                + " is required");
                            }
                            int minZoom = Integer.valueOf(minZoomInput
                                    .getText().toString());
                            int maxZoom = Integer.valueOf(maxZoomInput
                                    .getText().toString());

                            Integer maxFeatures = 500;
                            String maxFeaturesText = maxFeaturesInput.getText().toString();
                            if (maxFeaturesText != null && !maxFeaturesText.isEmpty()) {
                                maxFeatures = Integer.valueOf(maxFeaturesText);
                            }

                            double minLat = Double.valueOf(minLatInput
                                    .getText().toString());
                            double maxLat = Double.valueOf(maxLatInput
                                    .getText().toString());
                            double minLon = Double.valueOf(minLonInput
                                    .getText().toString());
                            double maxLon = Double.valueOf(maxLonInput
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

                            CompressFormat compressFormat = null;
                            Integer compressQuality = null;
                            if (compressFormatInput.getSelectedItemPosition() > 0) {
                                compressFormat = CompressFormat
                                        .valueOf(compressFormatInput
                                                .getSelectedItem().toString());
                                compressQuality = Integer
                                        .valueOf(compressQualityInput.getText()
                                                .toString());
                            }

                            boolean googleTiles = googleTilesRadioButton
                                    .isChecked();

                            BoundingBox boundingBox = new BoundingBox(minLon,
                                    minLat, maxLon, maxLat);

                            GeoPackageManager manager = GeoPackageFactory.getManager(getActivity());
                            GeoPackage geoPackage = manager.open(database);
                            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

                            // Load tiles
                            FeatureTiles featureTiles = new DefaultFeatureTiles(getActivity(), geoPackage, featureDao,
                                    getResources().getDisplayMetrics().density);
                            if (ignoreGeoPackageStyles.isChecked()) {
                                featureTiles.ignoreFeatureTableStyles();
                            }
                            featureTiles.setMaxFeaturesPerTile(maxFeatures);
                            if (maxFeatures != null) {
                                featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(getActivity()));
                            }

                            Paint pointPaint = featureTiles.getPointPaint();
                            pointPaint.setColor(GeoPackageUtils.parseColor(pointColor.getText().toString()));
                            pointPaint.setAlpha(Integer.valueOf(pointAlpha
                                    .getText().toString()));
                            featureTiles.setPointRadius(Float.valueOf(pointRadius.getText().toString()));

                            Paint linePaint = featureTiles.getLinePaintCopy();
                            linePaint.setColor(GeoPackageUtils.parseColor(lineColor.getText().toString()));
                            linePaint.setAlpha(Integer.valueOf(lineAlpha
                                    .getText().toString()));
                            linePaint.setStrokeWidth(Float.valueOf(lineStroke.getText().toString()));
                            featureTiles.setLinePaint(linePaint);

                            Paint polygonPaint = featureTiles.getPolygonPaintCopy();
                            polygonPaint.setColor(GeoPackageUtils.parseColor(polygonColor.getText().toString()));
                            polygonPaint.setAlpha(Integer.valueOf(polygonAlpha
                                    .getText().toString()));
                            polygonPaint.setStrokeWidth(Float.valueOf(polygonStroke.getText().toString()));
                            featureTiles.setPolygonPaint(polygonPaint);

                            featureTiles.setFillPolygon(polygonFill.isChecked());
                            if (featureTiles.isFillPolygon()) {
                                Paint polygonFillPaint = featureTiles.getPolygonFillPaintCopy();
                                polygonFillPaint.setColor(GeoPackageUtils.parseColor(polygonFillColor.getText().toString()));
                                polygonFillPaint.setAlpha(Integer.valueOf(polygonFillAlpha
                                        .getText().toString()));
                                featureTiles.setPolygonFillPaint(polygonFillPaint);
                            }

                            featureTiles.calculateDrawOverlap();

                            GeoPackageTable table = new GeoPackageTileTable(database, tableName, 0);
                            active.addTable(table);

                            TileScaling scaling = GeoPackageUtils.getTileScaling(tileScalingInput, tileScalingZoomOutInput, tileScalingZoomInInput);

                            LoadTilesTask.loadTiles(getActivity(),
                                    GeoPackageMapFragment.this, active,
                                    geoPackage, tableName, featureTiles, minZoom,
                                    maxZoom, compressFormat,
                                    compressQuality, googleTiles,
                                    boundingBox, scaling,
                                    ProjectionConstants.AUTHORITY_EPSG,
                                    String.valueOf(ProjectionConstants.EPSG_WEB_MERCATOR));
                        } catch (Exception e) {
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_create_tiles_label),
                                            e.getMessage());
                        }
                    }
                }).setNegativeButton(getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();

    }

    /**
     * Get feature selection dialog
     *
     * @param editFeaturesSelectionView
     * @param featuresInput
     * @param geoPackageInput
     * @return
     */
    private AlertDialog.Builder getFeatureSelectionDialog(View editFeaturesSelectionView,
                                                          final Spinner geoPackageInput,
                                                          final Spinner featuresInput) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setView(editFeaturesSelectionView);

        boolean searchForActive = true;
        int defaultDatabase = 0;
        int defaultTable = 0;

        List<String> databases = getDatabases();
        List<String> featureDatabases = new ArrayList<String>();
        if (databases != null) {
            for (String database : databases) {
                GeoPackage geoPackage = manager.open(database, false);
                try {
                    List<String> featureTables = geoPackage.getFeatureTables();
                    if (!featureTables.isEmpty()) {
                        featureDatabases.add(database);

                        if (searchForActive) {
                            for (int i = 0; i < featureTables.size(); i++) {
                                String featureTable = featureTables.get(i);
                                boolean isActive = active.exists(database, featureTable, GeoPackageTableType.FEATURE);
                                if (isActive) {
                                    defaultDatabase = featureDatabases.size() - 1;
                                    defaultTable = i;
                                    searchForActive = false;
                                    break;
                                }
                            }
                        }
                    }
                } finally {
                    if (geoPackage != null) {
                        geoPackage.close();
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
        ArrayAdapter<String> geoPackageAdapter = new ArrayAdapter<String>(
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

        return dialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesCancelled(String result) {
        loadTilesFinished();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadTilesPostExecute(String result) {
        if (result != null) {
            GeoPackageUtils.showMessage(getActivity(),
                    getString(R.string.geopackage_create_tiles_label), result);
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
        if (active.isModified()) {
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

    /**
     * Get the GeoPackage databases. If external storage permissions granted get all, if not get only internal
     *
     * @return
     */
    private List<String> getDatabases() {
        List<String> databases = null;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            databases = manager.databases();
        } else {
            databases = manager.internalDatabases();
        }

        return databases;
    }

}

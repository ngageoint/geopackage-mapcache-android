package mil.nga.mapcache;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.listeners.DetailLayerClickListener;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.ShareTask;
import mil.nga.mapcache.view.LayerSwitchListener;
import mil.nga.mapcache.view.LayerViewAdapter;
import mil.nga.mapcache.view.LayerViewObject;
import mil.nga.mapcache.listeners.RecyclerViewClickListener;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.sf.GeometryType;

/**
 *
 * Fragment to hold a GeoPackage's details.  Shown when a GeoPackage is clicked on in the list.
 *
 * Activities that contain this fragment must implement the
 * {@link GeoPackageDetailDrawer.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GeoPackageDetailDrawer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeoPackageDetailDrawer extends Fragment implements
        ILoadTilesTask, IIndexerTask {

    private static final String GEO_NAME = "geoPackageName";
    private OnFragmentInteractionListener mListener;
    private static View view;
    private GeoPackage selectedGeo;
    private String geoPackageName;
    private List<LayerViewObject> layers = new ArrayList<>();
    private RecyclerView layerRecyclerView;
    private LayerViewAdapter layerAdapter;
    private GeoPackageViewModel geoPackageViewModel;
    private ImageButton backArrow;
    private List<String> tileTables = new ArrayList<>();
    private List<String> featureTables = new ArrayList<>();
    private FloatingActionButton newLayer;
    private Switch allLayers;
    private boolean allChecked = false;

    // ignoreStateChange gives us a way to change the state of a switch without kicking off the listener
    // actions.  Set this to true before changing the state in order for it to be ignored, then
    // revert it back to false
    boolean ignoreStateChange = false;
    public void setIgnoreStateChange(boolean ignore){
        ignoreStateChange = ignore;
    }




    /**
     *  With the new geoPackageViewModel, we won't have to reference the manager from this class anymore
     */
    //    private GeoPackageManager manager;


    public GeoPackageDetailDrawer() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided GeoPackage name
     *
     * @param geoName The name of the GeoPackage that was clicked on
     * @return A new instance of fragment GeoPackageDetailDrawer for the given GeoPackage name
     */
    public static GeoPackageDetailDrawer newInstance(String geoName) {
        GeoPackageDetailDrawer fragment = new GeoPackageDetailDrawer();
        Bundle args = new Bundle();
        args.putString(GEO_NAME, geoName);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Get the GeoPackage object from the viewmodel
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        geoPackageViewModel = ViewModelProviders.of(getActivity()).get(GeoPackageViewModel.class);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            geoPackageName = getArguments().getString(GEO_NAME);
             selectedGeo = geoPackageViewModel.getGeoPackageByName(geoPackageName);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_geo_package_detail_drawer, container, false);
        allLayers = (Switch)  view.findViewById(R.id.header_all_switch);

        // Set listener for leaving this view
        backArrow = view.findViewById(R.id.detailPageBackButton);
        setBackArrowListener();

        // update for the first time to load initial data
        update();

        // Create listeners for the row of action buttons
        createButtonListeners();

        // Create all switch listener
        createAllSwitchListener();

        // Create floating action button
        setFLoatingActionButton();

        // Subscribe to the geopackage list to detect when a layer is deleted
        geoPackageViewModel.getGeoPackages().observe(this, newGeoPackages ->{
            if(selectedGeo != null && layerAdapter != null) {
               layerAdapter.clear();
               layers.clear();
               update();
             }
        });

        // Observe Active Tables - Redraw when a layer is set to active outside this class
        geoPackageViewModel.getActiveTables().observe(this, newTables ->{
            // Create the layer recycle view adapter
            createLayerListView();
        });

        return view;

    }


    /**
     * Listener for the enable/disable all layers button
     */
    public void createAllSwitchListener(){
        allLayers.setOnCheckedChangeListener(allCheckListener);
    }


    /**
     * Set Floating action button to open the create new geopackage wizard
     */
    private void setFLoatingActionButton(){
        newLayer = view.findViewById(R.id.new_layer_fab);
        newLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              createNewLayer();
            }
        });
    }


    public void createNewLayer(){
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
                createTileOption();
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

                            geoPackageViewModel.createFeatureTable(geoPackageName, boundingBox, geometryType, tableName);
                            update();


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
     * Create tile layer menu
     */
    private void createTileOption(){

    }


    /**
     *  Create listeners for the row of buttons (Rename, Share, Copy, Delete)
     */
    private void createButtonListeners(){

        Button infoButton = (Button) view.findViewById(R.id.detail_info);
        infoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                viewDatabaseOption(geoPackageName);
            }
        });
        // Set listeners for geopackage action buttons
        Button renameButton = (Button) view.findViewById(R.id.detail_rename);
        renameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                renameDatabaseOption(geoPackageName);
            }
        });
        Button deleteButton = (Button) view.findViewById(R.id.detail_delete);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteDatabaseOption(geoPackageName);
            }
        });
        Button copyButton = (Button) view.findViewById(R.id.detail_copy);
        copyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                copyDatabaseOption(geoPackageName);
            }
        });
        Button shareButton = (Button) view.findViewById(R.id.detail_share);
        shareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Get permissions to write to external storage first.  If success, it'll forward
                // the request to the shareGeopackage() method below
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.DETAIL_FRAGMENT_PERMISSIONS_REQUEST_ACCESS_EXPORT_DATABASE);
            }
        });
    }






    /**
     * View database information
     *
     * @param database
     */
    private void viewDatabaseOption(final String database) {
        AlertDialog viewDialog = geoPackageViewModel.getGeoPackageDetailDialog(database, getActivity());
        viewDialog.show();
    }





    /**
     * Create a share task to export a GeoPackage
     */
    public void shareGeopackage(){
        if(geoPackageName != null && !geoPackageName.isEmpty()) {
            ShareTask shareTask = new ShareTask(getActivity());
            shareTask.shareDatabaseOption(geoPackageName);
        }
    }



    /**
     * Get the GeoPackage's layers and create the list view
     */
    private void createLayerListView(){
        layers.clear();
        DetailLayerClickListener layerListener = new DetailLayerClickListener() {
            @Override
            public void onClick(DetailPageLayerObject layerObject) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_from_right, 0, 0, R.anim.slide_out_to_right);
                LayerDetailFragment layerDetailView = LayerDetailFragment.newInstance(geoPackageName, layerObject.getName());
                transaction.replace(R.id.layout_gp_detail, layerDetailView, "layerDetail");
                transaction.addToBackStack("layerDetail");  // if written, this transaction will be added to backstack
                transaction.commit();
            }
        };

        // Listener to show or hide layers when the layer switch is changed.  Map fragment is subscribed
        // to the view model's active table list.
        LayerSwitchListener switchListener = new LayerSwitchListener() {
            @Override
            public void setChecked(String name, boolean checked) {
                if(checked) {
                    // Enable the selected layer
                    iterateAllTables(false);
                    boolean added = geoPackageViewModel.addTableByName(name, selectedGeo.getName());
                }else{
                    // Disable selected layer
                    iterateAllTables(false);
                    boolean removed = geoPackageViewModel.removeActiveTableByName(name, selectedGeo.getName());
                }
            }
        };

        // Goes through all tables to repopulate the layers list, setting active as necessary
        iterateAllTables(true);

        layerRecyclerView = (RecyclerView) view.findViewById(R.id.layer_recycler_view);
        layerAdapter = new LayerViewAdapter(layers, view.getContext(), layerListener, switchListener);
        layerRecyclerView.setAdapter(layerAdapter);
        layerRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }


    /**
     * Iterates through all feature and tile tables, and compares them to the currently active tables list
     * @param addToLayers - if true, repopulate the layers list as it iterates through
     */
    private void iterateAllTables(boolean addToLayers){
        // If the layer is active, make sure the LayerViewObject is created with that layer switch set to checked
        List<String> activeTables = generateBasicActiveList();
        Iterator<String> featureIterator = featureTables.iterator();
        int activeCount = 0;
        while(featureIterator.hasNext()){
            String table = featureIterator.next();
            boolean isActive = false;
            if(activeTables.contains(table)){
                isActive = true;
                activeCount++;
            }
            if(addToLayers) {
                LayerViewObject layerObject = new LayerViewObject(R.drawable.material_feature, table, isActive);
                layers.add(layerObject);
            }
        }
        Iterator<String> tileIterator = tileTables.iterator();
        while(tileIterator.hasNext()){
            String table = tileIterator.next();
            boolean isActive = false;
            if(activeTables.contains(table)){
                isActive = true;
                activeCount++;
            }
            if(addToLayers) {
                LayerViewObject layerObject = new LayerViewObject(R.drawable.material_tile, table, isActive);
                layers.add(layerObject);
            }
        }
        // If active matches total count, and > 0 active, then all are checked.  Set the all layers switch to on
        if((featureTables.size() + tileTables.size() == activeCount) && (activeCount > 0)){
            if(activeCount > 0) {
                allChecked = true;
//                temporarily have the change listener ignore while we update the switch
                setIgnoreStateChange(true);
                allLayers.setChecked(true);
                setIgnoreStateChange(false);

            }
        } else {
            // If all switches aren't set to on, then the all layers switch needs to be set to off
            // remove the oncheck change listener because the user didn't click the all layer button
            allChecked = false;
            // temporarily have the change listener ignore while we update the switch
            setIgnoreStateChange(true);
            allLayers.setChecked(false);
            setIgnoreStateChange(false);

        }

    }


    /**
     *  Get list of active layer names belonging to the currently selected geopackage
     * @return
     */
    public List<String> generateBasicActiveList(){
        List<String> activeTables = new ArrayList<>();
        if(geoPackageViewModel.getActiveTables().getValue() != null && selectedGeo != null)
        {
            for(GeoPackageTable table : geoPackageViewModel.getActiveTables().getValue()){
                if(table.getDatabase().equalsIgnoreCase(selectedGeo.getName())){
                    activeTables.add(table.getName());
                }
            }
        }
        return activeTables;
    }


    /**
     * Update the currently loaded geopackage object and refresh page display
     */
    private void update(){
        selectedGeo = geoPackageViewModel.getGeoPackageByName(geoPackageName);
        if(selectedGeo != null) {
            TextView nameText = (TextView) view.findViewById(R.id.geoPackageName);
            TextView sizeText = (TextView) view.findViewById(R.id.text_size);
            TextView tileText = (TextView) view.findViewById(R.id.text_tiles);
            TextView featureText = (TextView) view.findViewById(R.id.text_features);

            nameText.setText(selectedGeo.getName());
            sizeText.setText(geoPackageViewModel.getGeoPackageSize(geoPackageName));
            tileTables = geoPackageViewModel.getTileTables(selectedGeo.getName());
            featureTables = geoPackageViewModel.getFeatureTables(selectedGeo.getName());

            int tileCount = tileTables.size();
            int featureCount = featureTables.size();
            tileText.setText(tileCount + " " + pluralize(tileCount, "Tile layer"));
            featureText.setText(featureCount + " " + pluralize(featureCount, "Feature layer"));

            // Recreate the layer recycle view adapter
            createLayerListView();
        }
    }




    /**
     * Rename database dialog window
     *
     * @param database
     */
    private void renameDatabaseOption(final String database) {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_edit);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Rename GeoPackage");
        // GeoPackage name
        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        inputName.setHint(database);
        inputName.setText(database);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialogBuilder.setView(alertView);
        dialogBuilder.setPositiveButton(R.string.button_ok_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = inputName.getText().toString();
                if (value != null && !value.isEmpty()
                        && !value.equals(database)) {
                    try{
                        if(geoPackageViewModel.setGeoPackageName(database, value)){
                            geoPackageName = value;
                            update();
//                            Toast.makeText(GeoPackageDetail.this,"Renamed " + database, Toast.LENGTH_SHORT).show();
                        } else{
                            GeoPackageUtils
                                    .showMessage(
                                            getActivity(),
                                            getString(R.string.geopackage_rename_label),
                                            "Rename from "
                                                    + database
                                                    + " to "
                                                    + value
                                                    + " was not successful");
                        };
                    } catch (Exception e){
                        GeoPackageUtils
                                .showMessage(
                                        getActivity(),
                                        getString(R.string.geopackage_rename_label),
                                        e.getMessage());
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.button_cancel_label, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }





    /**
     * Copy database option
     *
     * @param database
     */
    private void copyDatabaseOption(final String database) {

        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_copy);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Copy GeoPackage");

        final TextInputEditText input = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
        input.setText(database + getString(R.string.geopackage_copy_suffix));
        input.setHint("GeoPackage Name");

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setPositiveButton(getString(R.string.button_ok_label),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                if (value != null && !value.isEmpty()
                                        && !value.equals(database)) {
                                    try {
                                        if (geoPackageViewModel.copyGeoPackage(database, value)) {
                                            backArrow.callOnClick();
                                        } else {
                                            GeoPackageUtils
                                                    .showMessage(
                                                            getActivity(),
                                                            getString(R.string.geopackage_copy_label),
                                                            "Copy from "
                                                                    + database
                                                                    + " to "
                                                                    + value
                                                                    + " was not successful");
                                        }
                                    } catch (Exception e) {
                                        GeoPackageUtils
                                                .showMessage(
                                                        getActivity(),
                                                        getString(R.string.geopackage_copy_label),
                                                        e.getMessage());
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
     * Alert window to confirm then call to delete a GeoPackage
     *
     * @param database
     */
    private void deleteDatabaseOption(final String database) {
        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_label_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_delete);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Delete this GeoPackage?");
        TextView actionLabel = (TextView) alertView.findViewById(R.id.action_label);
        actionLabel.setText(database);
        actionLabel.setVisibility(View.INVISIBLE);

        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setIcon(getResources().getDrawable(R.drawable.material_delete))
                .setPositiveButton(getString(R.string.geopackage_delete_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // remove any active layers drawn on map
                                geoPackageViewModel.removeActiveTableLayers(database);
                                // Delete the geopackage
                                if(geoPackageViewModel.deleteGeoPackage(database)){
                                    // Leave this detail fragment once deleted
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    fragmentManager.popBackStack();
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
     * Click listener to destroy this fragment when the back arrow is pressed by popping it off the stack
     */
    private void setBackArrowListener(){
        backArrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });
    }




    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public boolean testString(){
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onIndexerCancelled(String result) {

    }

    @Override
    public void onIndexerPostExecute(String result) {

    }

    @Override
    public void onLoadTilesCancelled(String result) {

    }

    @Override
    public void onLoadTilesPostExecute(String result) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * makes a string plural based on the count
     * @param count
     * @param text singular word
     * @return
     */
    private String pluralize(int count, String text){
        if(count == 1){
            return text;
        } else {
            return text + "s";
        }
    }


    /**
     *      Listener for the all layers switch.  Will turn off all layers or turn on all layers
     */
    private CompoundButton.OnCheckedChangeListener allCheckListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

            // Only run listener if it's not set to ignore
            if(!ignoreStateChange) {
                // Update adapter so the recycleview can update the list for the gui
                boolean layersActivated = layerAdapter.checkAllLayers(checked);
                if (layersActivated) {
                    layerAdapter.notifyDataSetChanged();
                    // Update the viewmodel to show those active layers
                    if (checked) {
                        // Enable all
                        boolean added = geoPackageViewModel.enableAllLayers(selectedGeo.getName());
                    } else {
                        // Disable all
                        boolean removed = geoPackageViewModel.removeActiveTableLayers(selectedGeo.getName());
                    }
                }
            }
        }
    };




}

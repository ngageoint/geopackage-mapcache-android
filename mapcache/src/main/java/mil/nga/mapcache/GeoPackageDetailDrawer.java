package mil.nga.mapcache;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.load.ShareTask;
import mil.nga.mapcache.view.LayerSwitchListener;
import mil.nga.mapcache.view.LayerViewAdapter;
import mil.nga.mapcache.view.LayerViewObject;
import mil.nga.mapcache.view.RecyclerViewClickListener;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

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

        // Set listener for leaving this view
        backArrow = view.findViewById(R.id.detailPageBackButton);
        setBackArrowListener();

        // update for the first time to load initial data
        update();

        // Create listeners for the row of action buttons
        createButtonListeners();

        // Create the layer recycle view adapter
        createLayerListView();

        // Create all switch listener
        createAllSwitchListener();

        // Create floating action button
        setFLoatingActionButton();

        return view;

    }


    /**
     * Listener for the enable/disable all layers button
     */
    public void createAllSwitchListener(){
        Switch onOffSwitch = (Switch)  view.findViewById(R.id.allSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                // Update adapter so the recycleview can update the list for the gui
                boolean layersActivated = layerAdapter.checkAllLayers(checked);
                if(layersActivated) {
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
        });
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

        alertDialog.show();

    }


    /**
     *  Create listeners for the row of buttons (Rename, Share, Copy, Delete)
     */
    private void createButtonListeners(){
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
                ShareTask shareTask = new ShareTask(getActivity());
                shareTask.shareDatabaseOption(geoPackageName);
            }
        });
    }



    /**
     * Get the GeoPackage's layers and create the list view
     */
    private void createLayerListView(){
        layers.clear();
        RecyclerViewClickListener layerListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position, String name) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_from_right, 0, 0, R.anim.slide_out_to_right);
                LayerDetailFragment layerDetailView = LayerDetailFragment.newInstance(geoPackageName, name);
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
                    boolean added = geoPackageViewModel.addTableByName(name, selectedGeo.getName());
                }else{
                    // Disable selected layer
                    boolean removed = geoPackageViewModel.removeTableByName(name, selectedGeo.getName());
                }
            }
        };


        // If the layer is active, make sure the LayerViewObject is created with that layer switch set to checked
        List<String> activeTables = generateBasicActiveList();
        Iterator<String> featureIterator = featureTables.iterator();
        while(featureIterator.hasNext()){
            String table = featureIterator.next();
            boolean isActive = false;
            if(activeTables.contains(table)){
                isActive = true;
            }
            LayerViewObject layerObject = new LayerViewObject(R.drawable.material_feature, table, isActive);
            layers.add(layerObject);
        }
        Iterator<String> tileIterator = tileTables.iterator();
        while(tileIterator.hasNext()){
            String table = tileIterator.next();
            boolean isActive = false;
            if(activeTables.contains(table)){
                isActive = true;
            }
            LayerViewObject layerObject = new LayerViewObject(R.drawable.material_tile, table, isActive);
            layers.add(layerObject);
        }
        layerRecyclerView = (RecyclerView) view.findViewById(R.id.layer_recycler_view);
        layerAdapter = new LayerViewAdapter(layers, view.getContext(), layerListener, switchListener);
        layerRecyclerView.setAdapter(layerAdapter);
        layerRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }


    /**
     *  Get list of active layer names belonging to the currently selected geopackage
     * @return
     */
    public List<String> generateBasicActiveList(){
        List<String> activeTables = new ArrayList<>();
        if(geoPackageViewModel.getActiveTables().getValue() != null)
        {
            for(GeoPackageTable table : geoPackageViewModel.getActiveTables().getValue()){
                if(table.getDatabase() == selectedGeo.getName()){
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
//                        if(manager.rename(database, value)) {
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





}

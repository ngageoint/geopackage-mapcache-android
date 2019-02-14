package mil.nga.mapcache;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTableType;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LayerDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LayerDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LayerDetailFragment extends Fragment {
    private static final String GEO_NAME = "geoPackageName";
    private static final String GEO_LAYER_NAME = "geoPackageLayerName";

    private String geoPackageName;
    private String geoPackageLayerName;
    private OnFragmentInteractionListener mListener;
    private GeoPackageViewModel geoPackageViewModel;
    private GeoPackage selectedGeo;
    private GeoPackageTable selectedLayer;
    private ImageButton backArrow;
    private static View view;
    private boolean isActive = false;
    private TextView dataCountText;
    private TextView layerTypeText;
    private TextView layerCountLbl;
    private ImageView layerTypeIcon;
    //    private Button enableButton;
//    private Drawable disableIcon;
//    private Drawable enableIcon;
    private Switch layerOn;



    public LayerDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LayerDetailFragment.
     */
    public static LayerDetailFragment newInstance(String geoPackageName, String layerName) {
        LayerDetailFragment fragment = new LayerDetailFragment();
        Bundle args = new Bundle();
        args.putString(GEO_NAME, geoPackageName);
        args.putString(GEO_LAYER_NAME, layerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        geoPackageViewModel = ViewModelProviders.of(getActivity()).get(GeoPackageViewModel.class);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            geoPackageName = getArguments().getString(GEO_NAME);
            geoPackageLayerName = getArguments().getString(GEO_LAYER_NAME);
            selectedGeo = geoPackageViewModel.getGeoPackageByName(geoPackageName);
            selectedLayer = geoPackageViewModel.getTableObjectActive(geoPackageName, geoPackageLayerName);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_layer_detail, container, false);

        dataCountText = (TextView) view.findViewById(R.id.layerTileCount);
        layerTypeText = (TextView) view.findViewById(R.id.layerType);
        layerCountLbl = (TextView) view.findViewById(R.id.layerTileCountLbl);
        layerTypeIcon = (ImageView) view.findViewById(R.id.layer_type_icon);
        layerOn = (Switch) view.findViewById(R.id.enableSwitch);
//        enableButton = (Button) view.findViewById(R.id.layer_enable);
//        enableIcon = getContext().getResources().getDrawable(R.drawable.ic_check_box_outline_blank_black_24dp);
//        disableIcon = getContext().getResources().getDrawable(R.drawable.ic_check_box_black_24dp);
//        disableIcon.setBounds(0, 0, 84, 84);
//        enableIcon.setBounds(0, 0, 84, 84);

        // Set the switch to true if it's active on load
        layerOn.setChecked(selectedLayer.isActive());

        // Set listener for leaving this view
        backArrow = view.findViewById(R.id.layerPageBackButton);
        setBackArrowListener();

        // Create listeners for the row of action buttons
        createButtonListeners();

        // Update data
        update();

        return view;
    }

    /**
     * update the currently loaded layer object
     */
    public void update(){
        selectedGeo = geoPackageViewModel.getGeoPackageByName(geoPackageName);
        selectedLayer = geoPackageViewModel.getTableObjectActive(geoPackageName, geoPackageLayerName);
        if(selectedLayer != null) {
            TextView nameText = (TextView) view.findViewById(R.id.layerName);
            nameText.setText(selectedLayer.getName());

            // Set descriptive text about the layer
            setDescriptiveText();

//            // Set the enable / disable button based on current active status
//            if (selectedLayer.isActive()) {
//                enableButton.setCompoundDrawables(null, disableIcon, null, null);
//                enableButton.setText("Disable");
//            } else{
//                enableButton.setCompoundDrawables(null, enableIcon, null, null);
//                enableButton.setText("Enable");
//            }
        }
    }


    /**
     * Set descriptive text
     */
    public void setDescriptiveText(){
        if(selectedLayer.getType().equals(GeoPackageTableType.FEATURE)){
            layerTypeText.setText("Feature Layer");
            layerCountLbl.setText("Features");
            layerTypeIcon.setImageResource(R.drawable.material_feature);
        } else if(selectedLayer.getType().equals(GeoPackageTableType.TILE)){
            layerTypeText.setText("Tile Layer");
            layerCountLbl.setText("Tiles");
            layerTypeIcon.setImageResource(R.drawable.material_tile);
        }

        String countText = "";
        if(selectedLayer.getType().equals(GeoPackageTableType.FEATURE)){
            countText = "" + selectedLayer.getCount();
//            countText = "Features: " + selectedLayer.getCount();
        } else if(selectedLayer.getType().equals(GeoPackageTableType.TILE)){
            countText = "" + selectedLayer.getCount();
//            countText = "Features: " + selectedLayer.getCount();
        } else if(selectedLayer.getType().equals(GeoPackageTableType.FEATURE_OVERLAY)){
            countText = "" + selectedLayer.getCount();
//            countText = "Features: " + selectedLayer.getCount();
        }
        dataCountText.setText(countText);

        // Description is inside the contents
        Contents contents = geoPackageViewModel.getTableContents(geoPackageName, geoPackageLayerName);
        TextView descriptionText = (TextView) view.findViewById(R.id.text_description);
        String descText = "None";
        if(!TextUtils.isEmpty(contents.getDescription()) ){
            descText = contents.getDescription();
        }
        descriptionText.setText(descText);
    }

    /**
     *  Create listeners for the row of buttons (Rename, Share, Copy, Delete)
     */
    private void createButtonListeners(){
        // Set listeners for geopackage action buttons

        // Not going to allow rename at the moment
//        Button renameButton = (Button) view.findViewById(R.id.layer_rename);
//        renameButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                renameLayerOption(selectedLayer.getName());
//            }
//        });
        TextView deleteText = (TextView) view.findViewById(R.id.layerDelete);
        deleteText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteLayerOption();
            }
        });
        layerOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                toggleEnabled(b);
            }
        });
//        Button enableToggle = (Button) view.findViewById(R.id.layer_enable);
//        enableToggle.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                toggleEnabled();
//            }
//        });
//        Button shareButton = (Button) view.findViewById(R.id.layer_share);
//        shareButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                ShareTask shareTask = new ShareTask(getActivity());
//                shareTask.shareDatabaseOption(geoPackageName);
//            }
//        });


    }


    /**
     *  Toggle layer enabled
     */
    private void toggleEnabled(boolean switchedOff){
        if (!switchedOff) {
            // Disable - switch was turned off
            boolean removed = geoPackageViewModel.removeActiveTableByName(geoPackageLayerName, geoPackageName);
            update();
        } else{
            // enable
            boolean added = geoPackageViewModel.addTableByName(geoPackageLayerName, geoPackageName);
            update();
        }
    }



//    /**
//     * Rename layer dialog window.  Rename will reset the identifier field.  Identifier should be
//     * displayed by default for the name if it exists
//     *
//     * @param database
//     */
//    private void renameLayerOption(final String layer) {
//
//        // Create Alert window with basic input text layout
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View alertView = inflater.inflate(R.layout.basic_edit_alert, null);
//        // Logo and title
//        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
//        alertLogo.setBackgroundResource(R.drawable.material_edit);
//        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
//        titleText.setText("Rename Layer");
//        // Layer name
//        final TextInputEditText inputName = (TextInputEditText) alertView.findViewById(R.id.edit_text_input);
//        inputName.setHint(layer);
//        inputName.setText(layer);
//
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
//        dialogBuilder.setView(alertView);
//        dialogBuilder.setPositiveButton(R.string.button_ok_label, new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String value = inputName.getText().toString();
//                if (value != null && !value.isEmpty()
//                        && !value.equals(layer)) {
//                    try{
////                        if(geoPackageViewModel.setGeoPackageName(layer, value)){
////                            geoPackageName = value;
////                            update();
//////                            Toast.makeText(GeoPackageDetail.this,"Renamed " + database, Toast.LENGTH_SHORT).show();
////                        } else{
////                            GeoPackageUtils
////                                    .showMessage(
////                                            getActivity(),
////                                            getString(R.string.geopackage_rename_label),
////                                            "Rename from "
////                                                    + layer
////                                                    + " to "
////                                                    + value
////                                                    + " was not successful");
////                        };
//                    } catch (Exception e){
//                        GeoPackageUtils
//                                .showMessage(
//                                        getActivity(),
//                                        getString(R.string.geopackage_rename_label),
//                                        e.getMessage());
//                    }
//                }
//            }
//        });
//        dialogBuilder.setNegativeButton(R.string.button_cancel_label, new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog alertDialog = dialogBuilder.create();
//        alertDialog.show();
//    }




    /**
     * Alert window to confirm then call to delete a layer from a geopackage
     *
     */
    private void deleteLayerOption() {
        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.basic_label_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_delete);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText("Delete this Layer?");
        TextView actionLabel = (TextView) alertView.findViewById(R.id.action_label);
        actionLabel.setText(selectedLayer.getName());
        actionLabel.setVisibility(View.INVISIBLE);

        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setIcon(getResources().getDrawable(R.drawable.material_delete))
                .setPositiveButton(getString(R.string.layer_delete_label),

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // remove this layer from active layers drawn on map
                                geoPackageViewModel.removeActiveTableByName(selectedLayer.getName(), selectedGeo.getName());

                                // Delete the layer from the geopacket
                                try{
                                    geoPackageViewModel.removeLayerFromGeo(selectedGeo.getName(), selectedLayer.getName());
                                } catch(Exception e){
                                    GeoPackageUtils.showMessage(getActivity(),
                                            "Delete " + selectedLayer.getName()
                                                    + " Table", e.getMessage());
                                }

                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.popBackStack();
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
}

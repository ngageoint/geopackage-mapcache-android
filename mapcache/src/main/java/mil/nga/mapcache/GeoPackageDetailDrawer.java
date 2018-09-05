package mil.nga.mapcache;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.view.LayerViewAdapter;
import mil.nga.mapcache.view.LayerViewObject;
import mil.nga.mapcache.view.RecyclerViewClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GeoPackageDetailDrawer.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GeoPackageDetailDrawer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeoPackageDetailDrawer extends Fragment implements
        ILoadTilesTask, IIndexerTask {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String GEO_NAME = "geoPackageName";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private GeoPackage mParam2;

    private OnFragmentInteractionListener mListener;

    private static View view;

    private GeoPackage selectedGeo;
    private GeoPackageManager manager;
    private String geoPackageName;
    private List<LayerViewObject> layers = new ArrayList<>();
    private RecyclerView layerRecyclerView;
    private LayerViewAdapter layerAdapter;



    public GeoPackageDetailDrawer() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param geoName Parameter 2.
     * @return A new instance of fragment GeoPackageDetailDrawer.
     */
    // TODO: Rename and change types and number of parameters
    public static GeoPackageDetailDrawer newInstance(String geoName) {
        GeoPackageDetailDrawer fragment = new GeoPackageDetailDrawer();
        Bundle args = new Bundle();
        args.putString(GEO_NAME, geoName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = GeoPackageFactory.getManager(getActivity());
        if (getArguments() != null) {
            geoPackageName = getArguments().getString(GEO_NAME);
            selectedGeo = manager.open(geoPackageName, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_geo_package_detail_drawer, container, false);

        // Inflate the layout for this fragment
        ImageButton backArrow = view.findViewById(R.id.detailPageBackButton);
        backArrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        // update for the first time to load initial data
        update();

        // Set listeners for geopackage action buttons
        Button renameButton = (Button) view.findViewById(R.id.detail_rename);
        renameButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                renameDatabaseOption(geoPackageName);
            }
        });

        // Create the layer recycle view adapter
        createLayerListView();

        return view;

    }

    /**
     * Get the GeoPackage's layer data and create the list view
     */
    private void createLayerListView(){
        RecyclerViewClickListener layerListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {

            }
        };
        Iterator<String> featureIterator = selectedGeo.getFeatureTables().iterator();
        while(featureIterator.hasNext()){
            LayerViewObject layerObject = new LayerViewObject(R.drawable.material_feature, featureIterator.next());
            layers.add(layerObject);
        }
        Iterator<String> tileIterator = selectedGeo.getTileTables().iterator();
        while(tileIterator.hasNext()){
            LayerViewObject layerObject = new LayerViewObject(R.drawable.material_tile, tileIterator.next());
            layers.add(layerObject);
        }
        layerRecyclerView = (RecyclerView) view.findViewById(R.id.layer_recycler_view);
        layerAdapter = new LayerViewAdapter(layers, view.getContext(), layerListener);
        layerRecyclerView.setAdapter(layerAdapter);
        layerRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }


    /**
     * Update the currently loaded geopackage data for page display
     */
    private void update(){
        TextView nameText = (TextView) view.findViewById(R.id.geoPackageName);
        TextView sizeText = (TextView) view.findViewById(R.id.text_size);
        TextView tileText = (TextView) view.findViewById(R.id.text_tiles);
        TextView featureText = (TextView) view.findViewById(R.id.text_features);

        nameText.setText(selectedGeo.getName());
        sizeText.setText(manager.readableSize(geoPackageName));
        int tileCount = selectedGeo.getTileTables().size();
        int featureCount = selectedGeo.getFeatureTables().size();
        tileText.setText(tileCount + " " + pluralize(tileCount, "Tile layer"));
        featureText.setText(featureCount + " " + pluralize(featureCount, "Feature layer"));
    }

    /**
     * Set the currently selected GeoPackage for this page
     * @param newGeo
     */
    public void setSelectedGeo(GeoPackage newGeo){
        selectedGeo = newGeo;
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
                        if(manager.rename(database, value)) {
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

package mil.nga.mapcache;

import androidx.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.load.ILoadTilesTask;
import mil.nga.mapcache.view.LayerViewAdapter;
import mil.nga.mapcache.view.LayerViewObject;
import mil.nga.mapcache.view.detail.DetailPageAdapter;
import mil.nga.mapcache.view.detail.DetailPageHeaderObject;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

public class GeoPackageDetailView extends Fragment implements
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
     * Arraylist to populate the recyclerview.  First object will be a DetailPageHeaderObject,
     * followed by a DetailPageLayerObject for every layer in the geopackage.  The DetailPageHeaderObject
     * is used to populate the entire header section of the view, which is actually just the first
     * element of the recyclerview
     */
    ArrayList<Object> items = new ArrayList<>();


    /**
     * Required empty constructor
     */
    public GeoPackageDetailView(){
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided GeoPackage name
     */
    public static GeoPackageDetailView newInstance(String geoName){
        GeoPackageDetailView fragment = new GeoPackageDetailView();
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


    /**
     * Inflate the view, set up data, add listeners
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the view
        view = inflater.inflate(R.layout.fragment_gp_detail_view, container, false);

        // Populate the recycler view with a header, and layer names
        populateRecyclerView();

        return view;
    }


    /**
     *  Set up a recyclerview with a header (containing basic geopackage info and action buttons),
     *  followed by all the layers of that geopackage
     */
    private void populateRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_recycler_view);
        // Create a header object to be first in the recyclerview
        DetailPageHeaderObject headerObject = new DetailPageHeaderObject(new GeoPackageDatabase(geoPackageName));
        items.add(headerObject);

        // Add a Layer Object for every layer in this geopackage
        for(int i=0; i<20; i++){
            items.add(new DetailPageLayerObject("Layer " + i, geoPackageName, false, null));
        }

        // Create and populate the RecyclerView's adapter
        DetailPageAdapter adapter = new DetailPageAdapter(items, null, null, null, null, null, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
}

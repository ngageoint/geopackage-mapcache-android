package mil.nga.mapcache;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.data.GeoPackageTable;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String GEO_NAME = "geoPackageName";
    private static final String GEO_LAYER_NAME = "geoPackageLayerName";


    // TODO: Rename and change types of parameters
    private String geoPackageName;
    private String geoPackageLayerName;
    private OnFragmentInteractionListener mListener;
    private GeoPackageViewModel geoPackageViewModel;
    private GeoPackage selectedGeo;
    private GeoPackageTable selectedLayer;
    private ImageButton backArrow;
    private static View view;





    public LayerDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LayerDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
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
            selectedLayer = geoPackageViewModel.getTableObject(geoPackageName, geoPackageLayerName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_layer_detail, container, false);

        // Set listener for leaving this view
        backArrow = view.findViewById(R.id.layerPageBackButton);
        setBackArrowListener();

        update();

        return view;
    }

    /**
     * update the currently loaded layer object
     * @param uri
     */
    public void update(){
        TextView nameText = (TextView) view.findViewById(R.id.layerName);
        nameText.setText(selectedLayer.getName());
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

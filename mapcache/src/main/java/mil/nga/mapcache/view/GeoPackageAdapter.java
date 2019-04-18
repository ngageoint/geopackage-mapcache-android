package mil.nga.mapcache.view;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;

/**
 *  Adapter to power the main RecyclerView containing our GeoPackages.  Creates a list with 2
 *  possible row types:
 *    - GeoPackageHeader - header object to populate top of the view
 *    - GeoPackageViewHolder - a row item to hold an individual GeoPackage
 *
 *    This should be populated from the GeoPackageMapFragment
 *
 *  Also contains the spec for GeoPackageHeaderViewHolder, a Header View type for the main list
 */
public class GeoPackageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * List of GeoPackage objects.  Two types:
     *   - A String to represent the header object
     *   - A GeoPackageDatabase object for a single geopackage
     */
    private List<Object> mGeoPackages = new ArrayList<>();

    /**
     * Click listener for clicking on a GeoPackage row
     */
    GeoPackageClickListener mListener;

    /**
     * Constants to identify the type of view to be inflated
     */
    private static final int GEOPACKAGE_HEADER = 1;
    private static final int GEOPACKAGE_ITEM = 2;


    /**
     * Constructor
     * @param geos - List of GeoPackages for display by name
     * @param listener - Click listener for clicking on a GeoPackage
     */
    public GeoPackageAdapter(List<Object> geos, GeoPackageClickListener listener){
        mGeoPackages = geos;
        mListener = listener;
    }

    /**
     * Constructor - optional with no list of geoPackages to pass in
     * @param listener - Click listener for clicking on a GeoPackage
     */
    public GeoPackageAdapter(GeoPackageClickListener listener){
        mListener = listener;
    }

    /**
     * Creates the appropriate ViewHolder object bases on the viewType.  ViewType comes from
     * getItemViewType.
     * @param parent ViewGroup
     * @param viewType automatically given by getItemViewType
     * @return a ViewHolder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        RecyclerView.ViewHolder holder;
        if(viewType == GEOPACKAGE_HEADER){
            View v = LayoutInflater.from(context).inflate(R.layout.geopackage_list_header_layout, parent, false);
            holder = new GeoPackageHeaderViewHolder(v);
        } else{
            View v = LayoutInflater.from(context).inflate(R.layout.row_layout, parent, false);
            holder = new GeoPackageViewHolder(v, mListener);
        }
        return holder;
    }

    /**
     * Bind either a header view or row view, depending on what's given
     * @param holder - the view to bind
     * @param position - current position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof GeoPackageHeaderViewHolder){
            bindHeader(holder, position);
        } else if(holder instanceof GeoPackageViewHolder){
            bindRow(holder, position);
        }
    }

    /**
     * Bind the Header object of a geopackage view
     * @param holder - GeoPackageHeaderViewHolder
     * @param position - position to bind (should be 0)
     */
    private void bindHeader(RecyclerView.ViewHolder holder, int position){
        if(holder instanceof GeoPackageHeaderViewHolder){
            GeoPackageHeaderViewHolder viewHolder = (GeoPackageHeaderViewHolder)holder;
                viewHolder.getTitle().setText("GeoPackages");
        }
    }

    /**
     * Bind a geopackage row out of the given position.  Populates:
     *   - GeoPackage Name
     *   - Number of features
     *   - Number of tiles
     *   - Active color if the GeoPackage has active layers drawn on the map
     * @param holder viewholder for a geopackage
     * @param position position in the list
     */
    private void bindRow(RecyclerView.ViewHolder holder, int position){
        // Start with type checking to avoid issues
        if(holder instanceof GeoPackageViewHolder){
            GeoPackageViewHolder viewHolder = (GeoPackageViewHolder)holder;
            if(mGeoPackages.get(position) instanceof GeoPackageDatabase) {
                // The ViewHolder knows how to populate itself when given a GeoPackageDatabase object
                viewHolder.setData((GeoPackageDatabase)mGeoPackages.get(position));
            }
        }
    }

    /**
     * Called by default on every row when constructing the list.  This will tell the create
     * method what type of ViewHolder to construct
     * @param position - position in the row we're creating
     * @return type of view
     */
    @Override
    public int getItemViewType(int position){
        if(mGeoPackages.get(position) instanceof GeoPackageDatabase){
            return GEOPACKAGE_ITEM;
        } else if(mGeoPackages.get(position) instanceof String){
            return GEOPACKAGE_HEADER;
        }
        return -1;
    }

    /**
     * Get the current number of items in the list
     * @return number of items in the mGeoPackages list
     */
    @Override
    public int getItemCount() {
        return mGeoPackages.size();
    }


    /**
     * Empties the mGeoPackages list
     */
    public void clear(){
        mGeoPackages.clear();
    }


    /**
     * Inserts a string to the front of the list, so that the adapter populates the first object
     * as a GeoPackageHeaderViewHolder
     */
    public void insertDefaultHeader(){
        if(mGeoPackages.isEmpty()){
            mGeoPackages.add("Header");
        }
    }

    /**
     * Inserts a GeoPackageDatabase object to the mGeoPackages list object
     */
    public void insertToEnd(GeoPackageDatabase data){
        mGeoPackages.add(data);
    }

    /**
     * Updates the mGeoPackage list to set tables to active if they're found in the given list.  Then
     * calls the adapter's notify method to update the row
     * @param activeTables List of table names that should be set to active
     */
    public void updateActiveTables(List<String> activeTables){
        int position = 0;
        for(Object db : mGeoPackages){
            if(db instanceof GeoPackageDatabase) {
                GeoPackageDatabase geoPackage = (GeoPackageDatabase)db;
                String currentName = geoPackage.getDatabase();
                boolean found = false;
                for (String newName : activeTables) {
                    if (currentName.equalsIgnoreCase(newName)) {
                        // Current table should be active
                        found = true;
                        geoPackage.setActiveTables(true);
                    }
                }
                if (!found) {
                    // Should not be active
                    geoPackage.setActiveTables(false);
                }
                notifyItemChanged(position);
            }
            position++;
        }
    }



    /**
     * Getters and setters
     */
    public List<Object> getmGeoPackages() {
        return mGeoPackages;
    }
    public void setmGeoPackages(List<Object> mGeoPackages) {
        this.mGeoPackages = mGeoPackages;
    }
    public GeoPackageClickListener getmListener() {
        return mListener;
    }
    public void setmListener(GeoPackageClickListener mListener) {
        this.mListener = mListener;
    }



    /**
     * View holder for a GeoPackage header object
     * -----------------------------------------------------------------------------------
     */
    public class GeoPackageHeaderViewHolder extends RecyclerView.ViewHolder{

        /**
         * Name of the state
         */
        private TextView title;

        /**
         * Constructor
         * @param itemView
         */
        public GeoPackageHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.geopackage_header_title);
        }

        /**
         * Getters and setters
         */
        public TextView getTitle() {
            return title;
        }

        public void setTitle(TextView title) {
            this.title = title;
        }
    }
}

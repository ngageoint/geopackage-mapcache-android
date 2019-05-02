package mil.nga.mapcache.view.detail;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.listeners.DetailLayerClickListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.view.LayerViewHolder;

/**
 * This adapter will power the RecyclerView to hold details of a selected GeoPackage.  It will
 * populate 2 types of views:
 *  - One DetailPageHeader, which contains basic info about the geopackage (Name, size,
 *      number of layers)
 *  - Multiple DetailPageLayer rows, which will be a row for every layer name in the
 *      geopackage.
 */
public class DetailPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * List of 1 DetailPageHeaderObject followed by multiple DetailPageLayerObjects
     */
    private List<Object> mItems;

    /**
     * Click listener for clicking on a Layer row
     */
    private DetailLayerClickListener mListener;

    /**
     * Click listener for the back button in the header
     */
    private View.OnClickListener mBackArrowListener;

    /**
     * Click listener for the buttons in the detail header
     */
    private DetailActionListener mActionListener;

    /**
     * Click listener for clicking a layer's active switch
     */
    private LayerActiveSwitchListener mLayerSwitchListener;

    /**
     * GeoPackageDatabase object that this Adapter was created with
     */
    private GeoPackageDatabase mGeoPackageDatabase;

    /**
     * Two types of objects to be inflated, Headers and Rows
     */
    private final int HEADER = 0, LAYER = 1;


    /**
     * Constructor
     * @param items - list of DetailPageHeaderObject and DetailPageLayerObject
     * @param listener - row click listener
     */
    public DetailPageAdapter(List<Object> items, DetailLayerClickListener listener,
                             View.OnClickListener backArrowListener, DetailActionListener actionListener,
                             LayerActiveSwitchListener activeLayerListener, GeoPackageDatabase db){
        mItems = items;
        mListener = listener;
        mBackArrowListener = backArrowListener;
        mActionListener = actionListener;
        mLayerSwitchListener = activeLayerListener;
        mGeoPackageDatabase = db;
    }


    /**
     * Use the viewType to determing what type of ViewHolder to create
     * @param viewGroup Parent viewGroup
     * @param viewType viewType will either be Header or Layer based on getItemViewType
     * @return The appropriate viewHolder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch(viewType) {
            case HEADER:
                View headerView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_header_layout, viewGroup, false);
                return new HeaderViewHolder(headerView, mBackArrowListener, mActionListener);
            case LAYER:
                View layerView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layer_row_layout, viewGroup, false);
                return new LayerViewHolder(layerView, mListener, mLayerSwitchListener);
        }
        return null;
    }

    /**
     * Bind the ViewHolder depending on the type given
     * @param holder The ViewHolder to bind
     * @param position passing the position of the ViewHolder through
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerViewHolder) {
            bindLayer(holder, position);
        } else if (holder instanceof HeaderViewHolder){
            bindHeader(holder, position);
        }
    }

    /**
     * Return the size of the item list
     * @return
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Bind a view for an Layer type
     * @param holder a LayerViewHolder
     * @param position position in the list to build
     */
    private void bindLayer(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerViewHolder){
            LayerViewHolder layerViewHolder = (LayerViewHolder)holder;
            layerViewHolder.setData(mItems.get(position));
        }
    }

    /**
     * Bind a view for a Header type
     * @param holder a HeaderViewHolder
     * @param position position in the list to build
     */
    private void bindHeader(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder)holder;
            headerViewHolder.setData(mItems.get(position));
        }
    }

    /**
     * You can either get the item type by finding out what kind of object it is, or put it
     * first in the list if you know you'll always put the header there
     * @param position position in the list to evaluate
     * @return the type of object in the list
     */
    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position) instanceof DetailPageHeaderObject){
            return HEADER;
        } else if(mItems.get(position) instanceof DetailPageLayerObject){
            return LAYER;
        }
        return -1;
    }

    /**
     * Finds the geopackage name from the header object in the mItems list
     * @return Name of the geopackage currently populated in this adapter
     */
    private String getGeoPackageName(){
        if(!mItems.isEmpty()){
            if(mItems.get(0) instanceof DetailPageHeaderObject){
                DetailPageHeaderObject header = (DetailPageHeaderObject)mItems.get(0);
                return header.getGeopackageName();
            }
        }
        return null;
    }

    /**
     * Sets every row to inactive state
     */
    public void clearActive(){
        int position = 0;
        for (Object detailLayer : mItems) {
            if (detailLayer instanceof DetailPageLayerObject) {
                DetailPageLayerObject layer = (DetailPageLayerObject) detailLayer;
                layer.setChecked(false);
                notifyItemChanged(position);
            }
            position++;
        }
    }


    /**
     * Updates the mItems list to set DetailPageLayerObjects to active if they're found in the given list.  Then
     * calls the adapter's notify method to update the row.  This is done when a user changes the
     * active state on a layer, or the clear all active button
     * @param active List of tables that should be set to active
     */
    public void updateActiveTables(GeoPackageDatabases active){
        if(active.isEmpty()){
            clearActive();
        }
        if(getGeoPackageName() != null) {
            GeoPackageDatabase db = active.getDatabase(getGeoPackageName());
            if(db != null){
                int position = 0;

                for(Object detailLayer : mItems) {
                    if (detailLayer instanceof DetailPageLayerObject) {
                        DetailPageLayerObject layer = (DetailPageLayerObject) detailLayer;
                        layer.setChecked(false);
                        if(db.getAllTableNames().contains(layer.getName())){
                            layer.setChecked(true);
                        }
                        notifyItemChanged(position);
                    }
                    position++;
                }
            } else{
                // set all to null
                int position = 0;
                for (Object detailLayer : mItems) {
                    if (detailLayer instanceof DetailPageLayerObject) {
                        DetailPageLayerObject layer = (DetailPageLayerObject) detailLayer;
                        layer.setChecked(false);
                        notifyItemChanged(position);
                    }
                    position++;
                }
            }
        }
    }


    /**
     * Getters and setters
     */
    public List<Object> getmItems() {
        return mItems;
    }

    public void setmItems(List<Object> mItems) {
        this.mItems = mItems;
    }

    public DetailLayerClickListener getmListener() {
        return mListener;
    }

    public void setmListener(DetailLayerClickListener mListener) {
        this.mListener = mListener;
    }

    public GeoPackageDatabase getmGeoPackageDatabase() {
        return mGeoPackageDatabase;
    }

    public void setmGeoPackageDatabase(GeoPackageDatabase mGeoPackageDatabase) {
        this.mGeoPackageDatabase = mGeoPackageDatabase;
    }
}

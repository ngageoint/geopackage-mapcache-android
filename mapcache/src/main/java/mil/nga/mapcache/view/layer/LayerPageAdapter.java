package mil.nga.mapcache.view.layer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.listeners.FeatureColumnListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 * This adapter will power the RecyclerView to hold the details of a selected Layer from the Detail
 * page.  It will populate the following:
 *  Back arrow and Layer Name
 *  Layer type and icon
 *  Delete, copy, and rename buttons
 *  Number of features
 *  Description
 *  Enable/Disable switch
 *
 */
public class LayerPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * List of 1 DetailPageLayerObject followed by multiple FeatureColumnObjects
     */
    private final List<Object> mItems;

    /**
     * Click listener for the back button in the header
     */
    private final View.OnClickListener mBackArrowListener;

    /**
     * Listener for clicking the active switch
     */
    private final LayerActiveSwitchListener mActiveLayerListener;

    /**
     * Listener for clicking the delete button
     */
    private final DetailActionListener mDetailActionListener;

    /**
     * Listener for the feature columns
     */
    private final FeatureColumnListener mFeatureColumnListener;

    /**
     * Two types of objects to be inflated, Headers and Feature Columns
     */
    private final int HEADER = 0, COLUMN = 1;

    /**
     * Constructor
     * @param items - List of  DetailPageLayerObject and FeatureColumnObjects
     */
    public LayerPageAdapter(List<Object> items, View.OnClickListener backArrowListener,
                            LayerActiveSwitchListener activeLayerListener,
                            DetailActionListener detailActionListener,
                            FeatureColumnListener featureColumnListener){
        mItems = items;
        mBackArrowListener = backArrowListener;
        mActiveLayerListener = activeLayerListener;
        mDetailActionListener = detailActionListener;
        mFeatureColumnListener = featureColumnListener;
    }


    /**
     * Create a LayerDetailViewHolder for binding
     * @param parent parent view
     * @param viewType view type
     * @return a LayerDetailViewHolder object
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType) {
            case HEADER:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_layer_detail, parent, false);
                return new LayerDetailViewHolder(view, mBackArrowListener, mActiveLayerListener,
                    mDetailActionListener);
            case COLUMN:
                View columnView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_colum_type_layout, parent, false);
                return new LayerFeatureHolder(columnView, mFeatureColumnListener);
        }
        return null;
    }

    /**
     * call the view holder's setData() method with the Layer Detail object to populate the view
     * @param holder either LayerDetailViewHolder, or LayerFeatureHolder
     * @param position position in the list of items
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerDetailViewHolder){
            bindHeader(holder, position);
        } else if (holder instanceof LayerFeatureHolder){
            bindColumn(holder, position);
        }
    }

    /**
     * Bind a view for a Header type
     * @param holder a LayerDetailViewHolder
     * @param position position in the list to build
     */
    private void bindHeader(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerDetailViewHolder){
            LayerDetailViewHolder viewHolder = (LayerDetailViewHolder)holder;
            viewHolder.setData(mItems.get(position));
        }
    }

    /**
     * Bind a view for a Feature Column type
     * @param holder a LayerFeatureHolder
     * @param position position in the list to build
     */
    private void bindColumn(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerFeatureHolder){
            LayerFeatureHolder viewHolder = (LayerFeatureHolder)holder;
            viewHolder.setData(mItems.get(position));
        }
    }



    /**
     * Takes the list of active GeoPackageDatabases and modifies the active state if found
     * @param active populated GeoPackageDatabases object with active layers
     */
    public void updateActiveTables(GeoPackageDatabases active){
        if(active.isEmpty()){
            clearActive();
        } else {
            GeoPackageDatabase db = active.getDatabase(getGeoPackageName());
            int position = 0;
            if (db != null) {
                List<String> allTables = db.getAllTableNames();
                for (Object layerObject : mItems) {
                    if (layerObject instanceof DetailPageLayerObject) {
                        DetailPageLayerObject detailPageObject = (DetailPageLayerObject) layerObject;
                        detailPageObject.setChecked(allTables.contains(detailPageObject.getName()));
                        notifyItemChanged(position);
                    }
                    position++;
                }
            } else {
                clearActive();
            }
        }
    }

    /**
     * Clear active state in the header object
     */
    private void clearActive(){
        int position = 0;
        for (Object layerObject : mItems) {
            if (layerObject instanceof DetailPageLayerObject) {
                DetailPageLayerObject detailPageObject = (DetailPageLayerObject) layerObject;
                detailPageObject.setChecked(false);
                notifyItemChanged(position);
            }
            position++;
        }
    }

    /**
     * For now we only have 1 row
     * @return return number of items
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * You can either get the item type by finding out what kind of object it is, or put it
     * first in the list if you know you'll always put the header there
     * @param position position in the list to evaluate
     * @return the type of object in the list
     */
    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position) instanceof DetailPageLayerObject){
            return HEADER;
        } else if(mItems.get(position) instanceof FeatureColumnDetailObject){
            return COLUMN;
        }
        return -1;
    }

    /**
     * Get the name of the GeoPackage currently populating the view
     * @return name of the geopackage
     */
    public String getGeoPackageName(){
        if(!mItems.isEmpty()){
            if(mItems.get(0) instanceof DetailPageLayerObject){
                DetailPageLayerObject header = (DetailPageLayerObject)mItems.get(0);
                return header.getGeoPackageName();
            }
        }
        return null;
    }
}

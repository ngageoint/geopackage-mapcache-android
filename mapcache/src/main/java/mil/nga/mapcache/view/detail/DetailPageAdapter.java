package mil.nga.mapcache.view.detail;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import mil.nga.mapcache.R;
import mil.nga.mapcache.view.LayerViewHolder;
import mil.nga.mapcache.view.RecyclerViewClickListener;

/**
 * This adapter will power the recyclerview on the GeoPackageDetailView page.  It will populate 2
 * types of views:
 *  - first will be a DetailPageHeader, which contains basic info about the geopackage
 *  - all others will be DetailPageLayer rows, which will be a row for every layer name in the
 *      geopackage.
 */
public class DetailPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // List of DetailPageHeaderObject and DetailPageLayerObjects
    private List<Object> mItems;
    // Click listener
    private RecyclerViewClickListener mListener;
    // Two types of objects to be inflated, Headers and Rows
    private final int HEADER = 0, LAYER = 1;


    /**
     * Constructor
     * @param items - list of DetailPageHeaderObject and DetailPageLayerObject
     * @param listener - row click listener
     */
    public DetailPageAdapter(List<Object> items, RecyclerViewClickListener listener){
        mItems = items;
        mListener = listener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch(viewType) {
            case HEADER:
                View headerView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_header_layout, viewGroup, false);
                return new HeaderViewHolder(headerView);
            case LAYER:
                View layerView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layer_row_layout, viewGroup, false);
                return new LayerViewHolder(layerView, mListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerViewHolder) {
            bindLayer(holder, position);
        } else if (holder instanceof HeaderViewHolder){
            bindHeader(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Bind a view for an Layer type
     * @param holder
     * @param position
     */
    private void bindLayer(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LayerViewHolder){
            LayerViewHolder layerViewHolder = (LayerViewHolder)holder;
            layerViewHolder.setData(mItems.get(position));
        }
    }

    /**
     * Bind a view for a Header type
     * @param holder
     * @param position
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
     * @param position
     * @return
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
     * Getters and setters
     */
    public List<Object> getmItems() {
        return mItems;
    }

    public void setmItems(List<Object> mItems) {
        this.mItems = mItems;
    }

    public RecyclerViewClickListener getmListener() {
        return mListener;
    }

    public void setmListener(RecyclerViewClickListener mListener) {
        this.mListener = mListener;
    }
}

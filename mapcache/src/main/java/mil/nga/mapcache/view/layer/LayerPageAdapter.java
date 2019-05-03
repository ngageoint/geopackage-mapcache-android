package mil.nga.mapcache.view.layer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 * This adapter will power the RecyclerView to hold the details of a selected Layer from the Detail
 * page.  It will populate the following:
 *  Back arrow and Layer Name
 *  Layer type and icon
 *  Delete button
 *  Number of features
 *  Description
 *  Enable/Disable switch
 *
 */
public class LayerPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * DetailPageLayerObject containing details for the selected layer
     */
    private DetailPageLayerObject mLayerObject;

    /**
     * Click listener for the back button in the header
     */
    private View.OnClickListener mBackArrowListener;

    /**
     * Listener for clicking the active switch
     */
    private LayerActiveSwitchListener mActiveLayerListener;

    /**
     * Listener for clicking the delete button
     */
    private DetailActionListener mDetailActionListener;

    /**
     * Constructor
     * @param layerObject DetailPageLayerObject
     */
    public LayerPageAdapter(DetailPageLayerObject layerObject, View.OnClickListener backArrowListener,
                            LayerActiveSwitchListener activeLayerListener,
                            DetailActionListener detailActionListener){
        mLayerObject = layerObject;
        mBackArrowListener = backArrowListener;
        mActiveLayerListener = activeLayerListener;
        mDetailActionListener = detailActionListener;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_layer_detail, parent, false);
        return new LayerDetailViewHolder(view, mBackArrowListener, mActiveLayerListener,
                                        mDetailActionListener);
    }

    /**
     * call the view holder's setData() method with the Layer Detail object to populate the view
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LayerDetailViewHolder viewHolder = (LayerDetailViewHolder)holder;
        viewHolder.setData(mLayerObject);
    }

    /**
     * Takes the list of active GeoPackageDatabases and modifies the active state if found
     * @param active populated GeoPackageDatabases object with active layers
     */
    public void updateActiveTables(GeoPackageDatabases active){
        GeoPackageDatabase db = active.getDatabase(mLayerObject.getGeoPackageName());
        if(db != null){
            List<String> allTables = db.getAllTableNames();
            if(allTables.contains(mLayerObject.getName())){
                mLayerObject.setChecked(true);
            } else{
                mLayerObject.setChecked(false);
            }
        } else {
            mLayerObject.setChecked(false);
        }
        notifyItemChanged(0);
    }

    /**
     * For now we only have 1 row
     * @return
     */
    @Override
    public int getItemCount() {
        return 1;
    }

    /**
     * Getters and setters
     */
    public DetailPageLayerObject getmLayerObject() {
        return mLayerObject;
    }

    public void setmLayerObject(DetailPageLayerObject mLayerObject) {
        this.mLayerObject = mLayerObject;
    }
}

package mil.nga.mapcache.view.map.feature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mil.nga.mapcache.R;

/**
 *  Inflate a recyclerview based on a list of FeatureColumns from a feature.  This is embedded in the
 *  PointView popup window when a user clicks on a feature on the map
 */
class FeatureColumnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * List of FeatureColumn objects associated with a single feature
     */
    private List<FcColumnDataObject> mItems;

    /**
     * Constructor
     */
    public FeatureColumnAdapter(List<FcColumnDataObject> items){
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fcView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_column_row_layout, parent, false);
        return new FeatureColumnViewHolder(fcView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof FeatureColumnViewHolder){
            FeatureColumnViewHolder fcHolder = (FeatureColumnViewHolder)holder;
            fcHolder.setData(mItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}

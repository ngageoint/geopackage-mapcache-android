package mil.nga.mapcache.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mil.nga.mapcache.R;

/**
 *  LayerViewAdapter: Adapter class to hold data to bind to the GeoPackage's Layer List Recycler view
 *  Maintains a list of Layers.  Creates a LayerViewHolder for each layer
 */

public class LayerViewAdapter extends RecyclerView.Adapter<LayerViewHolder> {

    /**
     * List of objects that hold layer names and icon types
     */
    List<LayerViewObject> layers = new ArrayList<>();

    /**
     * Click listener to give to each ViewHolder
     */
    private RecyclerViewClickListener mListener;

    Context context;

    public LayerViewAdapter(List<LayerViewObject> list, Context context, RecyclerViewClickListener listener){
        this.layers = list;
        this.context = context;
        this.mListener = listener;
    }



    @NonNull
    @Override
    public LayerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(context).inflate(R.layout.layer_row_layout, viewGroup, false);
        LayerViewHolder holder = new LayerViewHolder(v, mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LayerViewHolder holder, int position) {
        // Set defaults to avoid null issues
        holder.title.setText("Layer");
        holder.icon.setImageResource(R.drawable.material_feature);

        holder.title.setText(layers.get(position).getName());
        holder.icon.setImageResource(layers.get(position).getIconType());
    }

    @Override
    public int getItemCount() {
        return layers.size();
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, LayerViewObject data) {
        layers.add(position, data);
        notifyItemInserted(position);
    }

    public void clear(){
        if (!this.layers.isEmpty()) {
            this.layers.clear();
        }
    }

    public LayerViewObject getPosition(int position){ return layers.get(position);}
}

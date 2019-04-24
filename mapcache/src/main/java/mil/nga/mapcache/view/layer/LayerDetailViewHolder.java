package mil.nga.mapcache.view.layer;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import mil.nga.mapcache.R;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 * View holder for the Layer Detail page
 */
public class LayerDetailViewHolder extends RecyclerView.ViewHolder{

    /**
     * Layer name text view
     */
    private TextView nameText;

    /**
     * Back button to take the RecyclerView back to the GeoPackage Detail page
     */
    private ImageButton backArrow;

    /**
     * DetailPageLayerObject containing details for the selected layer
     */
    private DetailPageLayerObject mLayerObject;


    /**
     * Constructor
     * @param view view to inflate
     */
    public LayerDetailViewHolder(View view, View.OnClickListener backListener){
        super(view);
        nameText = (TextView) view.findViewById(R.id.layerName);
        backArrow = (ImageButton) itemView.findViewById(R.id.layerPageBackButton);
        backArrow.setOnClickListener(backListener);
    }

    /**
     * Sets the view to hold data given in the LayerDetail object
     */
    public void setData(DetailPageLayerObject layerObject){
        mLayerObject = layerObject;
        nameText.setText(mLayerObject.getName());
    }
}

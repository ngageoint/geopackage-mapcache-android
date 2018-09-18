package mil.nga.mapcache.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import mil.nga.mapcache.GeoPackageDetail;
import mil.nga.mapcache.R;

/**
 *  ViewHolder to show a GeoPackage layer name and icon corresponding to the layer type
 */

public class LayerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView title;
    ImageView icon;
    private RecyclerViewClickListener mListener;

    public LayerViewHolder(View itemView, RecyclerViewClickListener listener){
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.layer_label);
        icon = (ImageView) itemView.findViewById(R.id.layer_icon);

        mListener = listener;
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mListener.onClick(view, getAdapterPosition(), title.getText().toString());
    }
}

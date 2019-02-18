package mil.nga.mapcache.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import mil.nga.mapcache.R;

/**
 * ViewHolder to show a GeoPackage name, and the number of feature and tile tables.  Also binds a
 * click listener.  There will be one GeoPackageViewHolder for each row in the GeoPackage list
 */
public class GeoPackageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    TextView title;
    TextView featureTables;
    TextView tileTables;
    LinearLayout activeLayout;
    Resources res;
    private RecyclerViewClickListener mListener;

    public GeoPackageViewHolder(View itemView, RecyclerViewClickListener listener) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.geopackage_title);
        featureTables = (TextView) itemView
                .findViewById(R.id.feature_tables);
        tileTables = (TextView) itemView
                .findViewById(R.id.tile_tables);
        mListener = listener;
        activeLayout = itemView.findViewById(R.id.active_color);
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
        res = itemView.getContext().getResources();

    }

    /**
     * Set the background color of the side isle based on the active state
     */
    public void setActiveColor(boolean active){
        if(active){
            activeLayout.setBackgroundColor(res.getColor(R.color.nga_accent_light));
        } else{
            activeLayout.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void onClick(View view) {
        mListener.onClick(view, getAdapterPosition(), title.getText().toString());
    }
}

package mil.nga.mapcache.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    private RecyclerViewClickListener mListener;

    public GeoPackageViewHolder(View itemView, RecyclerViewClickListener listener) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.geopackage_title);
        featureTables = (TextView) itemView
                .findViewById(R.id.feature_tables);
        tileTables = (TextView) itemView
                .findViewById(R.id.tile_tables);
        mListener = listener;
        itemView.setClickable(true);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        mListener.onClick(view, getAdapterPosition());
    }
}

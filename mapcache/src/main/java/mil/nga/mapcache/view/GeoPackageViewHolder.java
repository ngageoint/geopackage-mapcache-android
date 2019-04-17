package mil.nga.mapcache.view;

import android.content.res.Resources;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTileTable;

/**
 * ViewHolder to show a GeoPackage name, and the number of feature and tile tables.  Also binds a
 * click listener.  There will be one GeoPackageViewHolder for each row in the GeoPackage list
 */
public class GeoPackageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    /**
     * GeoPackage name
     */
    private TextView title;

    /**
     * Text to hold number of feature tables
     */
    private TextView featureTables;

    /**
     * Text to hold number of tile tables
     */
    private TextView tileTables;

    /**
     * Layout to hold the color of the active state (if the geopackage has active layers on the map
     */
    private LinearLayout activeLayout;

    /**
     * Context resources
     */
    private Resources res;

    /**
     * Click listener to be attached to the layer
     */
    private RecyclerViewClickListener mListener;

    /**
     * Constructor
     * @param itemView View to attach
     * @param listener Click listener for clicking on a GeoPackage row
     */
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

    /**
     * Sets the ViewHolder's data based on the given GeoPackageDatabase object
     * @param db a GeoPackageDatabase object
     */
    public void setData(GeoPackageDatabase db){
        // Get the count of tile tables and feature tables associated with each geopackage list to set counts
        int tileTables = 0;
        int featureTables = 0;
        Iterator<GeoPackageFeatureTable> featureIterator = db.getFeatures().iterator();
        boolean active = false;
        while (featureIterator.hasNext()) {
            GeoPackageFeatureTable current = featureIterator.next();
            if(current.isActive()){
                active = true;
            }
            // GeoPackage title
            this.title.setText(current.getDatabase());
            if(!current.getName().equalsIgnoreCase(""))
                featureTables++;
        }

        Iterator<GeoPackageTileTable> tileIterator = db.getTiles().iterator();
        while (tileIterator.hasNext()) {
            GeoPackageTileTable current = tileIterator.next();
            if(current.isActive()){
                active = true;
            }
            // GeoPackage title
            this.title.setText(current.getDatabase());
            if(current instanceof GeoPackageTileTable)
                tileTables++;
        }
        this.featureTables.setText("Feature Tables: " + featureTables);
        this.tileTables.setText("Tile Tables: " + tileTables);
        setActiveColor(active);
    }

    /**
     * Sets up the click listener
     * @param view
     */
    @Override
    public void onClick(View view) {
        mListener.onClick(view, getAdapterPosition(), title.getText().toString());
    }

    /**
     * Getters and Setters
     */
    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getFeatureTables() {
        return featureTables;
    }

    public void setFeatureTables(TextView featureTables) {
        this.featureTables = featureTables;
    }

    public TextView getTileTables() {
        return tileTables;
    }

    public void setTileTables(TextView tileTables) {
        this.tileTables = tileTables;
    }

    public RecyclerViewClickListener getmListener() {
        return mListener;
    }

    public void setmListener(RecyclerViewClickListener mListener) {
        this.mListener = mListener;
    }
}

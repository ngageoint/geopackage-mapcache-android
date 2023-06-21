package mil.nga.mapcache.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.listeners.GeoPackageClickListener;

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
    private final LinearLayout activeLayout;

    /**
     * Context resources
     */
    private final Resources res;

    /**
     * Click listener to be attached to the layer
     */
    private GeoPackageClickListener mListener;

    /**
     * GeoPackageDatabase object to represent this geopackage to pass back in the click listener
     */
    private GeoPackageDatabase mDatabase;

    /**
     * Active state of the geopackage
     */
    private boolean active;

    /**
     * Card item's context for getting resources
     */
    private Context itemContext;

    /**
     * Constructor
     * @param itemView View to attach
     * @param listener Click listener for clicking on a GeoPackage row
     */
    public GeoPackageViewHolder(View itemView, GeoPackageClickListener listener) {
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
        itemContext = itemView.getContext();
    }

    /**
     * Set the background color of the side isle based on the active state, or use system theme
     */
    public void setActiveColor(boolean active){
        this.active = active;
        if(active){
            activeLayout.setBackgroundColor(res.getColor(R.color.nga_accent_light, itemContext.getTheme()));
        } else{
            activeLayout.setBackgroundColor(res.getColor(R.color.backgroundSecondaryColor, itemContext.getTheme()));
        }
    }

    /**
     * Sets the ViewHolder's data based on the given GeoPackageDatabase object
     * @param db a GeoPackageDatabase object
     */
    public void setData(GeoPackageDatabase db){
        mDatabase = db;
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
            if(!current.getName().equalsIgnoreCase(""))
                featureTables++;
        }

        for (GeoPackageTileTable current : db.getTiles()) {
            if (current.isActive()) {
                active = true;
            }
            // GeoPackage title
            if (current instanceof GeoPackageTileTable)
                tileTables++;
        }
        if(db.isActiveTables()){
            active = true;
        }
        this.title.setText(db.getDatabase());
        this.featureTables.setText(res.getString(R.string.feature_tables_label, featureTables));
        this.tileTables.setText(res.getString(R.string.tile_tables_label, tileTables));
        setActiveColor(active);
    }

    /**
     * Sets up the click listener
     * @param view geopackage click listener
     */
    @Override
    public void onClick(View view) {
        mListener.onClick(view, getBindingAdapterPosition(), mDatabase);
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

    public GeoPackageClickListener getmListener() {
        return mListener;
    }

    public void setmListener(GeoPackageClickListener mListener) {
        this.mListener = mListener;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public GeoPackageDatabase getmDatabase() {
        return mDatabase;
    }

    public void setmDatabase(GeoPackageDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }
}

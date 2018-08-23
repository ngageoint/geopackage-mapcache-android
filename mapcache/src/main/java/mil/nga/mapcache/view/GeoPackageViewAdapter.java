package mil.nga.mapcache.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.GeoPackageManagerFragment;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;

/**
 * GeoPackageViewAdapter : Adapter class to hold data to bind to a GeoPackage Recycler View.
 * Maintains a list of GeoPackageTable lists.  Each list is grouped together under the same
 * database (geopackage) name.  Creates a GeoPackageViewHolder for each row.
 *
 */
public class GeoPackageViewAdapter extends RecyclerView.Adapter<GeoPackageViewHolder>{
    /**
     * Lists of tables grouped together.  Each list has the same parent database name (geopackage)
     */
    List<List<GeoPackageTable>> list = new ArrayList<List<GeoPackageTable>>();

    /**
     * Click listener to give to each ViewHolder
     */
    private RecyclerViewClickListener mListener;

    Context context;

    /**
     * Create the adapter
     * @param list
     * @param context
     * @param listener
     */
    public GeoPackageViewAdapter(List<List<GeoPackageTable>> list , Context context, RecyclerViewClickListener listener){
        this.list = list;
        this.context = context;
        this.mListener = listener;
    }


    public GeoPackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        GeoPackageViewHolder holder = new GeoPackageViewHolder(v, mListener);

        return holder;
    }

    /**
     * Sets all the fields in the View Holder when created
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(GeoPackageViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText("GeoPackage Name Not Found");

        // Get the count of tile tables and feature tables associated with each geopackage list to set counts
        int tileTables = 0;
        int featureTables = 0;
        Iterator<GeoPackageTable> tableIterator = list.get(position).iterator();
        while (tableIterator.hasNext()) {
            GeoPackageTable current = tableIterator.next();
            holder.title.setText(current.getDatabase());

            if(current instanceof GeoPackageFeatureTable && !current.getName().equalsIgnoreCase(""))
                featureTables++;
            if(current instanceof GeoPackageTileTable)
                tileTables++;

        }
        holder.featureTables.setText("Feature Tables: " + featureTables);
        holder.tileTables.setText("Tile Tables: " + tileTables);

        //animate(holder);
    }

    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, List<GeoPackageTable> data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public void insertToEnd(List<GeoPackageTable> data) {
        list.add(getItemCount(), data);
        notifyItemInserted(getItemCount()-1);
    }


    public void clear(){
        if (!this.list.isEmpty()) {
            this.list.clear();
        }
    }

    public List<GeoPackageTable> getPosition(int position){
        return list.get(position);
    }

}

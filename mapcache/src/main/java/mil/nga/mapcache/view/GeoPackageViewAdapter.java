package mil.nga.mapcache.view;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;

/**
 * GeoPackageViewAdapter : Adapter class to hold data to bind to a GeoPackage Recycler View.
 * Maintains a list of List of GeoPackage tables.  Each list is grouped together under the same
 * database (geopackage) name.  Creates a GeoPackageViewHolder for each row.
 *
 */
public class GeoPackageViewAdapter extends RecyclerView.Adapter<GeoPackageViewHolder>{


    /**
     * Lists of tables grouped together.  Each list has the same parent database name (geopackage)
     */
    List<List<GeoPackageTable>> list = new ArrayList<List<GeoPackageTable>>();

    /**
     *  List of GeoPackage objects
     */
    List<GeoPackage> geoPackageList = new ArrayList<>();

    /**
     * Name of the GeoPackage
     */
    TextView name;

    /**
     * Click listener to give to each ViewHolder
     */
    private RecyclerViewClickListener mListener;


    Context context;

    /**
     * Create the adapter
     * @param context
     * @param listener
     */
    public GeoPackageViewAdapter(Context context, RecyclerViewClickListener listener){
        this.context = context;
        this.mListener = listener;
    }


    public GeoPackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(context).inflate(R.layout.row_layout, parent, false);
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
        holder.getTitle().setText("GeoPackage Name Not Found");

        // Get the count of tile tables and feature tables associated with each geopackage list to set counts
        int tileTables = 0;
        int featureTables = 0;

        // A couple different attempts at using GeoPackage objects instead, however, when getting tables,
        // it has to reopen the file.  I don't want to do that here.
//        GeoPackage geo = geoPackageList.get(position);
//        holder.title.setText(geo.getName());
//        holder.featureTables.setText("Feature Tables: " + geo.getFeatureTables().size());
//        holder.tileTables.setText("Tile Tables: " + geo.getTileTables().size());

//        Iterator<GeoPackage> packageIterator = geoPackageList.iterator();
//        while (packageIterator.hasNext()){
//            GeoPackage geo = packageIterator.next();
//            holder.title.setText(geo.getName());
////            holder.featureTables.setText("Feature Tables: " + geo.getFeatureTables().size());
////            holder.tileTables.setText("Tile Tables: " + geo.getTileTables().size());
//        }


        Iterator<GeoPackageTable> tableIterator = list.get(position).iterator();
        boolean active = false;
        while (tableIterator.hasNext()) {
            GeoPackageTable current = tableIterator.next();
            if(current.isActive()){
                active = true;
            }
            holder.title.setText(current.getDatabase());

            if(current instanceof GeoPackageFeatureTable && !current.getName().equalsIgnoreCase(""))
                featureTables++;
            if(current instanceof GeoPackageTileTable)
                tileTables++;

        }
        holder.getFeatureTables().setText("Feature Tables: " + featureTables);
        holder.getTileTables().setText("Tile Tables: " + tileTables);
        holder.setActiveColor(active);
        //animate(holder);
    }

    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    public int getGeoPackageListSize(){
        return geoPackageList.size();
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

    /**
     *  Clear out the list of GeoPackges.  Usually in prep for an update
     */
    public void clearGeoPackages(){
        if(!this.geoPackageList.isEmpty()){
            this.geoPackageList.clear();
        }
    }

    /**
     * Set a new list of GeoPackages
     * @param newGeos
     */
    public void setGeoPackageList(List<GeoPackage> newGeos){
        this.geoPackageList = newGeos;
    }

    public List<GeoPackageTable> getPosition(int position){
        return list.get(position);
    }



    public boolean updateActive(List<String> newTables){
        int position = 0;
        for(List<GeoPackageTable> dbList : list){
            String currentName = dbList.get(0).getDatabase();
            boolean found = false;
            for(String newName : newTables){
                if(currentName.equalsIgnoreCase(newName)){
                    // Current table should be active
                    found = true;
                    dbList.get(0).setActive(true);
                }
            }
            if(!found){
                // Should not be active
                dbList.get(0).setActive(false);
            }
            notifyItemChanged(position);
            position++;
        }
        return false;
    }





}

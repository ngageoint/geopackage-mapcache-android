package mil.nga.mapcache.listeners;

import android.view.View;

import mil.nga.mapcache.data.GeoPackageDatabase;

/**
 * Listener for clicking on a GeoPackage object in the main RecyclerView
 */
public interface GeoPackageClickListener {
    /**
     * Implement on click
     * @param view View clicked
     * @param position position in the RecyclerView
     * @param db GeoPackageDatabase object of the row that was clicked on
     */
    void onClick(View view, int position, GeoPackageDatabase db);
}

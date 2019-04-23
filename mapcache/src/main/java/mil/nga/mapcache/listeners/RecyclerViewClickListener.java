package mil.nga.mapcache.listeners;

import android.view.View;

/**
 * Click listener to be used inside the GeoPackage Recycler View
 */

public interface RecyclerViewClickListener {
    void onClick(View view, int position, String name);
}
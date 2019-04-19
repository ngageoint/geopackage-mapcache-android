package mil.nga.mapcache.view;

import android.view.View;

/**
 * Listener for the action buttons on the GeoPackage detail page
 */
public interface DetailActionListener {

    /**
     * Enum to describe the button clicked on the detail page
     */
    public final int DELETE_GP = 0, SHARE_GP = 1, DETAIL_GP = 2, RENAME_GP = 3, COPY_GP = 4;

    /**
     * Button click on the detail page
     * @param view HeaderView
     * @param actionType - DELETE_GP, SHARE_GP, RENAME_GP, COPY_GP
     * @param name Name of the clicked GeoPackage
     */
    void onClick(View view, int actionType, String name);

}

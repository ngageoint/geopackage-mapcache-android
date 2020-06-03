package mil.nga.mapcache.listeners;

import android.view.View;

/**
 * Listener for the action buttons on the GeoPackage detail page
 */
public interface DetailActionListener {

    /**
     * Enum to describe the button clicked on the detail page
     */
    public final int DELETE_GP = 0, SHARE_GP = 1, DETAIL_GP = 2, RENAME_GP = 3, COPY_GP = 4,
                    DELETE_LAYER = 5, RENAME_LAYER = 6, COPY_LAYER = 7, EDIT_FEATURES = 8;

    /**
     * Button click on the detail page
     * @param view HeaderView
     * @param actionType - DELETE_GP, SHARE_GP, RENAME_GP, COPY_GP, DELETE_LAYER, RENAME_LAYER, COPY_LAYER
     * @param name Name of the clicked GeoPackage
     * @param layer Name of the Layer to delete, if applicable (only on the layer detail page)
     */
    void onClick(View view, int actionType, String name, String layer);

}

package mil.nga.mapcache.listeners;

import android.view.View;

import mil.nga.mapcache.view.layer.FeatureColumnDetailObject;

/**
 * Listener for the layer detail page's feature columns
 */

public interface FeatureColumnListener {

    /**
     * Enum to describe the button clicked on the detail page
     */
    int DELETE_FEATURE_COLUMN = 0;

    /**
     * Button click on the detail page
     * @param view LayerDetail view
     * @param actionType - DELETE_FEATURE_COLUMN
     * @param columnDetailObject - object containing feature column details
     */
    void onClick(View view, int actionType, FeatureColumnDetailObject columnDetailObject);
}

package mil.nga.mapcache.listeners;

import android.view.View;

/**
 * Listener for deleting an image from a feature point
 */
public interface DeleteImageListener {
    int DELETE_IMAGE = 0;

    /**
     * Delete button click
     * @param view - current view
     * @param actionType - delete image action
     * @param rowId - id of the image we're currently looking at
     */
    void onClick(View view, int actionType, long rowId);
}

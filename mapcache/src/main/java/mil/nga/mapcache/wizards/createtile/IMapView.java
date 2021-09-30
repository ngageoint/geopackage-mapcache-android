package mil.nga.mapcache.wizards.createtile;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import mil.nga.mapcache.GeoPackageMapFragment;

/**
 * Interface to the view that shows the map.
 */
public interface IMapView {

    /**
     * Hides the map icons.
     */
    public void hideMapIcons();

    /**
     * Shows the map icons.
     */
    public void showMapIcons();

    /**
     * Show/Hides the zoom level indicator.
     *
     * @param zoomVisible True if shown, false if hidden.
     */
    public void setZoomLevelVisible(boolean zoomVisible);

    /**
     * Indicates if the zoom level is visible.
     *
     * @return True if show, false if hidden.
     */
    public boolean isZoomLevelVisible();

    /**
     * Gets the transparent box view that shows the tiles bounding box.
     *
     * @return The transparent box view.
     */
    public View getTransBox();

    /**
     * Gets the touchable map.
     *
     * @return The touchable map.
     */
    public GeoPackageMapFragment.TouchableMap getTouchableMap();

    /**
     * Gets the base map.
     *
     * @return The map.
     */
    public GoogleMap getMap();
}

package mil.nga.mapcache.view.map.grid;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Observable;

/**
 * Contains the created grids, polylines and labels that represent the user specified grid to be
 * overlayed on top of the map.
 */
public class GridModel extends Observable {

    /**
     * The grids property.
     */
    public static String GRIDS_PROPERTY = "gridProperty";

    /**
     * The polylines property.
     */
    public static String POLYLINES_PROPERTY = "polylnesProperty";

    /**
     * The labels property.
     */
    public static String LABELS_PROPERTY = "labelsProperty";

    /**
     * The grids containing their locations and an associated label.
     */
    private Grid[] grids;

    /**
     * The polylines to add directly to the map.
     */
    private PolylineOptions[] polylines;

    /**
     * The labels to add directly to the map.
     */
    private MarkerOptions[] labels;

    /**
     * Gets the grids.
     *
     * @return The grids containing their locations and an associated label.
     */
    public Grid[] getGrids() {
        return grids;
    }

    /**
     * Sets a new set of grids for the map.
     *
     * @param grids The grids containing their locations and an associated label.
     */
    public void setGrids(Grid[] grids) {
        this.grids = grids;
        setChanged();
        notifyObservers(GRIDS_PROPERTY);
    }

    /**
     * Gets the polylines to put on the map.
     *
     * @return The polylines.
     */
    public PolylineOptions[] getPolylines() {
        return polylines;
    }

    /**
     * Sets a new set of polylines to be placed on map.
     *
     * @param polylines The polylines.
     */
    public void setPolylines(PolylineOptions[] polylines) {
        this.polylines = polylines;
        setChanged();
        notifyObservers(POLYLINES_PROPERTY);
    }

    /**
     * Gets the labßels to add directly to map.
     *
     * @return The labels for the grids.
     */
    public MarkerOptions[] getLabels() {
        return labels;
    }

    /**
     * Sets a new set of grid labels for the map.
     *
     * @param labels The new labels.
     */
    public void setLabels(MarkerOptions[] labels) {
        this.labels = labels;
        setChanged();
        notifyObservers(LABELS_PROPERTY);
    }
}

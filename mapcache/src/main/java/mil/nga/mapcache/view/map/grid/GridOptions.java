package mil.nga.mapcache.view.map.grid;

/**
 * Class that contains certain options unique to various zoom levels.
 */
public class GridOptions {

    /**
     * The minimum zoom level this option applies to.
     */
    private int minZoom;

    /**
     * The maximum zoom level this option applies to.
     */
    private int maxZoom;

    /**
     * The precision for the grid at the specified zoom levels.
     */
    private int precision;

    /**
     * Indicates if the labels should be shown at the specified zoom levels.
     */
    private boolean showLabel;

    /**
     * Constructs new grid options.
     *
     * @param minZoom   The minimum zoom level this option applies to.
     * @param maxZoom   The maximum zoom level this option applies to.
     * @param showLabel Indicates if the labels should be shown at the specified zoom levels.
     */
    public GridOptions(int minZoom, int maxZoom, boolean showLabel) {
        this(minZoom, maxZoom, showLabel, 0);
    }

    /**
     * Constructs new grid options.
     *
     * @param minZoom   The minimum zoom level this option applies to.
     * @param maxZoom   The maximum zoom level this option applies to.
     * @param showLabel Indicates if the labels should be shown at the specified zoom levels.
     * @param precision The precision for the grid at the specified zoom levels.
     */
    public GridOptions(int minZoom, int maxZoom, boolean showLabel, int precision) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.showLabel = showLabel;
        this.precision = precision;
    }

    /**
     * Gets the minimum zoom level this option applies to.
     *
     * @return The minimum zoom level.
     */
    public int getMinZoom() {
        return minZoom;
    }

    /**
     * Gets the maximum zoom level this option applies to.
     *
     * @return The maximum zoom level this option applies to.
     */
    public int getMaxZoom() {
        return maxZoom;
    }

    /**
     * Gets the precision for the grid at the specified zoom levels.
     *
     * @return The precision for the grid at the specified zoom levels.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Indicates if the labels should be shown at the specified zoom levels.
     *
     * @return True if the labels should be shown, false otherwise.
     */
    public boolean isShowLabel() {
        return showLabel;
    }
}

package mil.nga.mapcache.view.map.grid.mgrs;

import android.app.Activity;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.view.map.grid.Grid;
import mil.nga.mapcache.view.map.grid.GridCreator;
import mil.nga.mapcache.view.map.grid.GridModel;
import mil.nga.mapcache.view.map.grid.GridOptions;

/**
 * Creates MGRS grids to be overlayed on the map.
 */
public class MGRSGridCreator extends GridCreator {

    /**
     * The gzd grid options.
     */
    private GridOptions gzd = new GridOptions(0, 20, true, 0, Color.RED);

    /**
     * Grid options for the one hundred kilometers scale.
     */
    private GridOptions one_hundred_km = new GridOptions(5, 8, true, 100000);

    /**
     * Grid options for the ten kilometer scale.
     */
    private GridOptions ten_km = new GridOptions(9, 11, false, 10000);

    /**
     * Grid options for the one kilometer scale.
     */
    private GridOptions one_km = new GridOptions(12, 14, false, 1000);

    /**
     * Grid options for the one hundred meter scale.
     */
    private GridOptions one_hundred_meter = new GridOptions(15, 17, false, 100);

    /**
     * Grid options for the ten meter scale.
     */
    private GridOptions ten_meter = new GridOptions(18, 20, false, 10);

    /**
     * Calculates the zones in the visible bounds and zoom level.
     */
    private GZDZones zonesCalc = new GZDZones();

    /**
     * Constructor.
     *
     * @param model    The grid model.
     * @param map      The map to display the grids on.
     * @param activity The activity.
     */
    public MGRSGridCreator(GridModel model, GoogleMap map, Activity activity) {
        super(model, map, activity);
    }

    @Override
    protected Grid[] createGrid(BoundingBox bounds, int zoom) {
        List<Grid> blocks = new ArrayList<>();
        List<GridZoneDesignator> zones = zonesCalc.zonesWithin(bounds, false);

        GridOptions[] grids = {ten_meter, one_hundred_meter, one_km, ten_km};

        for (GridOptions grid : grids) {
            if (zoom >= grid.getMinZoom() && zoom <= grid.getMaxZoom()) {
                blocks.addAll(this.getGridZoneDesignatorPolygons(zones, bounds, grid.getPrecision(), grid, grid.isShowLabel()));
            }
        }

        if (zoom >= one_hundred_km.getMinZoom() && zoom <= one_hundred_km.getMaxZoom()) {
            blocks.addAll(this.getGridZoneDesignatorPolygons(zones, bounds, one_hundred_km.getPrecision(), one_hundred_km, zoom > 5));
        }

        // handle GZD zones
        if (zoom >= gzd.getMinZoom() && zoom <= gzd.getMaxZoom()) {
            blocks.addAll(this.getGridZoneDesignatorPolygons(zones, bounds, 0, gzd, zoom > 2));
        }

        return blocks.toArray(new Grid[0]);
    }

    /**
     * Gets all the grids from the grid zones.
     *
     * @param zones     The grid zones to get the grids from.
     * @param bounds    The bounds.
     * @param precision The precision of the grids.
     * @param options   The grid options.
     * @param showLabel True if label should be shown, false otherwise.
     * @return The grids for the zone.
     */
    private List<Grid> getGridZoneDesignatorPolygons(List<GridZoneDesignator> zones, BoundingBox bounds, int precision, GridOptions options, boolean showLabel) {
        List<Grid> ret = new ArrayList<>();
        for (GridZoneDesignator zone : zones) {
            List<Grid> grids = zone.polygonsAndLabelsInBounds(bounds, precision);
            ret.addAll(grids);
        }

        for(Grid grid : ret) {
            grid.setColor(options.getColor());
            if(!showLabel) {
                grid.setText(null);
            }
        }

        return ret;
    }
}

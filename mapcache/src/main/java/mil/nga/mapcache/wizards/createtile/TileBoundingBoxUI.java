package mil.nga.mapcache.wizards.createtile;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.layersprovider.LayersModel;
import mil.nga.mapcache.layersprovider.LayersView;
import mil.nga.mapcache.layersprovider.LayersViewDialog;
import mil.nga.mapcache.load.ILoadTilesTask;

/**
 * The UI that allows the user to specify the tile bounding box of the area they want to export
 * to a geopackage.
 */
public class TileBoundingBoxUI {

    /**
     * RecyclerView that will hold our GeoPackages.
     */
    private RecyclerView geoPackageRecycler;

    /**
     * The map.
     */
    private IMapView mapView;

    /**
     * Bounding box manager.
     */
    private IBoundingBoxManager boxManager;

    /**
     * The model containing available layers to select if the server has that available.
     */
    private LayersModel layers;

    /**
     * Constructor.
     *
     * @param geoPackageRecycler RecyclerView that will hold our GeoPackages.
     * @param mapView            The map.
     * @param boxManager         The bounding box manager.
     */
    public TileBoundingBoxUI(RecyclerView geoPackageRecycler, IMapView mapView,
                             IBoundingBoxManager boxManager, LayersModel layers) {
        this.geoPackageRecycler = geoPackageRecycler;
        this.mapView = mapView;
        this.boxManager = boxManager;
        this.layers = layers;
    }

    /**
     * Shows the tile bounding box UI, allowing the user to specify the area to export their tiles
     * to a geopackage.
     *
     * @param activity       Use The app context.
     * @param fragment       The fragment this UI is apart of, used to get resource strings.
     * @param active         The active GeoPackages
     * @param callback       The callback to pass to LoadTilesTask.
     * @param geoPackageName The name of the geopackage.
     * @param layerName      The name of the layer.
     * @param url            The base url to the tile layer.
     */
    public void show(FragmentActivity activity, Context context, Fragment fragment,
                     GeoPackageDatabases active, ILoadTilesTask callback, String geoPackageName,
                     String layerName, String url) {
        // prepare the screen by shrinking bottom sheet, hide fab and map buttons, show zoom level
        BottomSheetBehavior behavior = BottomSheetBehavior.from(geoPackageRecycler);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mapView.hideMapIcons();
        mapView.setZoomLevelVisible(true);

        // Make sure the transparent box is visible, and add it to the mapview
        View transBox = mapView.getTransBox();
        transBox.setVisibility(View.VISIBLE);
        mapView.getTouchableMap().addView(mapView.getTransBox());

        View layersButton = transBox.findViewById(R.id.layersButton);
        if (layers.getLayers() != null && layers.getLayers().length > 0) {
            layersButton.setVisibility(View.VISIBLE);
            layersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapView.getTouchableMap().removeView(mapView.getTransBox());
                    LayersView layersView = new LayersViewDialog(context, layers);
                    layersView.show();
                }
            });
        }

        // Cancel
        Button cancelTile = (Button) mapView.getTransBox().findViewById(
                R.id.tile_area_select_cancel);
        cancelTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove transparent box and show the fab and map buttons again
                mapView.getTouchableMap().removeView(mapView.getTransBox());
                mapView.showMapIcons();
            }
        });

        // Next
        Button tileDrawNext = (Button) mapView.getTransBox().findViewById(
                R.id.tile_area_select_next);
        tileDrawNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View transBoxView = (View) mapView.getTransBox().findViewById(
                        R.id.transparent_measurement);
                Point point = new Point(transBoxView.getLeft(), transBoxView.getTop());
                boxManager.setBoundingBoxStartCorner(
                        mapView.getMap().getProjection().fromScreenLocation(point));
                Point endPoint = new Point(transBoxView.getRight(), transBoxView.getBottom());
                boxManager.setBoundingBoxEndCorner(
                        mapView.getMap().getProjection().fromScreenLocation(endPoint));
                boolean drawBoundingBox = boxManager.drawBoundingBox();
                if (!mapView.isZoomLevelVisible()) {
                    mapView.setZoomLevelVisible(false);
                }
                mapView.showMapIcons();
                mapView.getTouchableMap().removeView(mapView.getTransBox());
                // continue to create layer
                createTileFinal(activity, context, fragment, active, callback, geoPackageName,
                        layerName, url);
            }
        });
    }

    /**
     * Final step for creating a tile layer after the bounding box has been drawn
     *
     * @param activity       Use The app context.
     * @param fragment       The fragment this UI is apart of, used to get resource strings.
     * @param active         The active GeoPackages
     * @param callback       The callback to pass to LoadTilesTask.
     * @param geoPackageName The name of the geopackage.
     * @param layerName      The name of the layer.
     * @param url            The base url to the tile layer.
     */
    private void createTileFinal(FragmentActivity activity, Context context, Fragment fragment,
                                 GeoPackageDatabases active, ILoadTilesTask callback,
                                 String geoPackageName, String layerName, String url) {
        LayerOptionsUI layerOptions = new LayerOptionsUI(activity, context,
                fragment, active, callback, boxManager,
                geoPackageName, layerName, url, layers);
        layerOptions.show();
    }
}

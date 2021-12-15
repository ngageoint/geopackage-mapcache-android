package mil.nga.mapcache.view.map.grid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.utils.PolygonUtils;

/**
 * Creates labels that will be visible in the center of the grid.
 */
public class LabelMaker {

    /**
     * The grid model to update.
     */
    private GridModel gridModel;

    /**
     * Constructor.
     *
     * @param gridModel The grid model to update.
     */
    public LabelMaker(GridModel gridModel) {
        this.gridModel = gridModel;
    }

    /**
     * Creates the labels for each grid to be placed at the center of each grid.
     */
    public void createLabels() {
        List<MarkerOptions> labels = new ArrayList<>();

        for (Grid grid : gridModel.getGrids()) {
            if(grid.getText() != null) {
                Polygon box = grid.getBounds();
                LatLng[] corners = PolygonUtils.getInstance().getBounds(box);
                double centerLat = (corners[2].latitude + corners[0].latitude) / 2;
                double centerLon = (corners[2].longitude + corners[0].longitude) / 2;
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(centerLat, centerLon));

                BitmapDescriptor textIcon = createLabel(grid);
                marker.icon(textIcon);
                labels.add(marker);
            }
        }

        if(!labels.isEmpty()) {
            MarkerOptions[] newLabels = labels.toArray(new MarkerOptions[0]);
            gridModel.setLabels(newLabels);
        } else {
            gridModel.setLabels(new MarkerOptions[0]);
        }
    }

    /**
     * Create a bitmap containing the text to be used for the marker.
     *
     * @param grid The grid to put a label for.
     * @return The marker's text image.
     */
    private BitmapDescriptor createLabel(Grid grid) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(20);
        textPaint.setColor(grid.getColor());

        float textWidth = textPaint.measureText(grid.getText());
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight);

        Bitmap image = Bitmap.createBitmap(width, height + 15, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height);

        canvas.drawText(grid.getText(), 0, 0, textPaint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }
}

package mil.nga.mapcache.view.map.grid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;

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
            Polygon box = grid.getBounds();
            double centerLat = (box.getCoordinates()[2].y + box.getCoordinates()[0].y) / 2;
            double centerLon = (box.getCoordinates()[1].x + box.getCoordinates()[0].x) / 2;
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(centerLat, centerLon));

            BitmapDescriptor textIcon = createLabel(grid.getText());
            marker.icon(textIcon);
            labels.add(marker);
        }

        MarkerOptions[] newLabels = labels.toArray(new MarkerOptions[0]);
        gridModel.setLabels(newLabels);
    }

    /**
     * Create a bitmap containing the text to be used for the marker.
     *
     * @param text The text.
     * @return The marker's text image.
     */
    private BitmapDescriptor createLabel(String text) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(20);

        float textWidth = textPaint.measureText(text);
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight);

        Bitmap image = Bitmap.createBitmap(width, height + 15, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height);

        canvas.drawText(text, 0, 0, textPaint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }
}

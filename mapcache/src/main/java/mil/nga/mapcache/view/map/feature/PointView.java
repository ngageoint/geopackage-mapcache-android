package mil.nga.mapcache.view.map.feature;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.schema.columns.DataColumns;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.mapcache.GeoPackageMapFragment;
import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.SaveFeatureColumnListener;
import mil.nga.sf.GeometryType;

/**
 * Create a new view which holds a single point's data.  Opened after clicking a feature on the map
 */
public class PointView {

    /**
     * Context from where this was called
     */
    private final Context context;

    /**
     * Geometry type
     */
    private final GeometryType geometryType;

    /**
     * Feature row data
     */
    private final FeatureRow featureRow;

    /**
     * DataColumnsDao
     */
    private final DataColumnsDao dataColumnsDao;

    /**
     * GeoPackage name
     */
    private final String geoName;

    /**
     * GeoPackage layer name
     */
    private final String layerName;

    /**
     * Save button listener
     */
    private SaveFeatureColumnListener saveListener;

    /**
     * We'll generate a list of FCObjects to hold our data for the recycler
     */
    private final List<FcColumnDataObject> fcObjects = new ArrayList<>();

    private FeatureColumnAdapter fcAdapter;

    /**
     * If we want the user to be able to save changes
     */
    private final boolean enableSaves;


    public PointView(Context context, GeometryType geometryType, FeatureRow featureRow,
                     DataColumnsDao dataColumnsDao, String geoName, String layerName,
                     boolean enableSaves){
        this.context = context;
        this.geometryType = geometryType;
        this.featureRow = featureRow;
        this.dataColumnsDao = dataColumnsDao;
        this.geoName = geoName;
        this.layerName = layerName;
        this.enableSaves = enableSaves;
    }

    public void showPointData(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.feature_detail_popup, null);

        // Logo and title
        ImageView closeLogo = (ImageView) alertView.findViewById(R.id.feature_detail_close);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        TextView titleText = (TextView) alertView.findViewById(R.id.feature_detail_title);
        titleText.setText(geometryType.toString());

        // Save button
        MaterialButton saveButton = (MaterialButton) alertView.findViewById(R.id.feature_detail_save);
        if (!this.enableSaves) {
            saveButton.setEnabled(false);
        }

        // Open the dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setView(alertView);
        final AlertDialog alertDialog = dialog.create();

        // GeoPackage and layer
        TextView geoNameText = (TextView) alertView.findViewById(R.id.fc_geo_name);
        geoNameText.setText(geoName);
        TextView layerNameText = (TextView) alertView.findViewById(R.id.fc_layer_name);
        layerNameText.setText(layerName);


        // Feature Column recycler
        StringBuilder message = new StringBuilder();
        int geometryColumn = featureRow.getGeometryColumnIndex();
        for (int i = 0; i < featureRow.columnCount(); i++) {
            if (i != geometryColumn) {
                Object value = featureRow.getValue(i);

                FeatureColumn featureColumn = featureRow.getColumn(i);

                String columnName = featureColumn.getName();
                if (dataColumnsDao != null) {
                    try {
                        DataColumns dataColumn = dataColumnsDao.getDataColumn(featureRow.getTable().getTableName(), columnName);
                        if (dataColumn != null) {
                            columnName = dataColumn.getName();
                        }
                    } catch (SQLException e) {
                        Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                "Failed to search for Data Column name for column: " + columnName
                                        + ", Feature Table: " + featureRow.getTable().getTableName(), e);
                    }
                }

                if (value == null) {
                    if(featureColumn.getDataType().equals(GeoPackageDataType.TEXT)){
                        FcColumnDataObject fcRow = new FcColumnDataObject(columnName, "");
                        fcObjects.add(fcRow);
                    } else if(featureColumn.getDataType().equals(GeoPackageDataType.DOUBLE)){
                        FcColumnDataObject fcRow = new FcColumnDataObject(columnName, 0.0);
                        fcObjects.add(fcRow);
                    } else if(featureColumn.getDataType().equals(GeoPackageDataType.BOOLEAN)){
                        FcColumnDataObject fcRow = new FcColumnDataObject(columnName, false);
                        fcObjects.add(fcRow);
                    } else if(featureColumn.getDataType().equals(GeoPackageDataType.INTEGER)){
                        FcColumnDataObject fcRow = new FcColumnDataObject(columnName, 0);
                        fcObjects.add(fcRow);
                    }
                } else{
                    FcColumnDataObject fcRow = new FcColumnDataObject(columnName, value);
                    fcObjects.add(fcRow);
                }

                    message.append(columnName).append(": ");
                    message.append(value);
                    message.append("\n");

            }
        }
        /**
         * recycler to hold each feature column object in a viewholder
         */
        RecyclerView fcRecycler = alertView.findViewById(R.id.fc_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(alertView.getContext());
        fcRecycler.setLayoutManager(layoutManager);
        fcAdapter = new FeatureColumnAdapter(fcObjects, context);
        fcRecycler.setAdapter(fcAdapter);

        //  attachments slider gallery
        ViewPager2 viewPager2 = alertView.findViewById(R.id.attachmentPager);
        List<SliderItem> sliderItems = new ArrayList<>();
        // Attachment sample images
//        sliderItems.add(new SliderItem(R.drawable.flood4));
//        sliderItems.add(new SliderItem(R.drawable.flood1));
//        sliderItems.add(new SliderItem(R.drawable.flood2));
//        sliderItems.add(new SliderItem(R.drawable.flood3));
        viewPager2.setAdapter(new SliderAdapter(sliderItems, viewPager2));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPager2.setPageTransformer(compositePageTransformer);



        /**
         * Save the data by calling back to write to the geopackage
         */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveListener.onClick(view, fcAdapter.getmItems());
            }
        });


        /**
         * Click listener for close button
         */
        closeLogo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        alertDialog.show();
        // Allow the keyboard to pop up in front of the alert dialog
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }



    public void setSaveListener(SaveFeatureColumnListener listener){
        this.saveListener = listener;
    }

    /**
     * Update the fcObjects list with the text inputs on the page, tehn return
     * @return feature column data objects
     */
    public List<FcColumnDataObject> getFcObjects() {
        return fcObjects;
    }
}

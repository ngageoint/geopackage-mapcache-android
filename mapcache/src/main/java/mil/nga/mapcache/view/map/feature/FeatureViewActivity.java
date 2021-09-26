package mil.nga.mapcache.view.map.feature;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.schema.columns.DataColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.mapcache.GeoPackageMapFragment;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

public class FeatureViewActivity extends AppCompatActivity {


    /**
     * Permission check
     */
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 204;

    /**
     * We'll generate a list of FCObjects to hold our data for the recycler
     */
    private final List<FcColumnDataObject> fcObjects = new ArrayList<>();

    private FeatureColumnAdapter fcAdapter;

    /**
     * If we want the user to be able to save changes
     */
    private boolean enableSaves = true;

    /**
     * Button for adding image from camera
     */
    private MaterialButton cameraButton;

    /**
     * Button for adding image from the phone's gallery
     */
    private MaterialButton galleryButton;

    /**
     * Save button
     */
    private MaterialButton saveButton;

    /**
     * Close image
     */
    private ImageView closeLogo;

    /**
     * ViewPager to hold image thumbnails
     */
    private ViewPager2 imageGalleryPager;

    /**
     * Slider adapter to be attached to the viewpager for holding images
     */
    private SliderAdapter sliderAdapter;

    /**
     * List of images to be put into the sliderAdapter
     */
    private List<SliderItem> sliderItems;

    /**
     * Marker feature that was selected
     */
    private MarkerFeature markerFeature;

    /**
     * ViewModel for accessing data from the repository
     */
    private GeoPackageViewModel geoPackageViewModel;

    /**
     * Contains all objects from the geopackage that we need to generate this view
     */
    private FeatureViewObjects featureViewObjects;

    /**
     * Recyclerview to hold the feature columns
     */
    private RecyclerView fcRecycler;


    /**
     * On Create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_detail_popup);
        if(this.getSupportActionBar()!=null){
            this.getSupportActionBar().hide();
        }
        // Get intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            markerFeature = (MarkerFeature)extras.getSerializable(String.valueOf(R.string.marker_feature_param));
        }

        // Pull needed data out of the geopackage
        geoPackageViewModel = new ViewModelProvider(this).get(GeoPackageViewModel.class);
        geoPackageViewModel.init();
        if(markerFeature != null){
            featureViewObjects = geoPackageViewModel.getFeatureViewObjects(markerFeature);
        }

        // Set up all buttons
        cameraButton = (MaterialButton) findViewById(R.id.add_from_camera);
        galleryButton = (MaterialButton) findViewById(R.id.add_from_gallery);
        saveButton = (MaterialButton) findViewById(R.id.feature_detail_save);
        closeLogo = (ImageView) findViewById(R.id.feature_detail_close);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        createButtonListeners();

        // Set up data from retreived geopackage
        setFieldData();

        // set up the image gallery
        imageGalleryPager = findViewById(R.id.attachmentPager);
        sliderItems = new ArrayList<>();
        sliderAdapter = new SliderAdapter(sliderItems, imageGalleryPager);
        createImageGallery();
    }


    /**
     * Set up the viewpager as a scrolling image gallery
     */
    private void createImageGallery(){
        if(imageGalleryPager != null && featureViewObjects != null){
            // Attachment sample images
//            Bitmap flood1 = BitmapFactory.decodeResource(getResources(),R.drawable.flood1);
//            sliderItems.add(new SliderItem(flood1));
//            sliderItems.add(new SliderItem(flood1));
            for(Bitmap bitmap : featureViewObjects.getBitmaps()){
                sliderItems.add(new SliderItem(bitmap));
            }
            imageGalleryPager.setAdapter(sliderAdapter);
            imageGalleryPager.setClipToPadding(false);
            imageGalleryPager.setClipChildren(false);
            imageGalleryPager.setOffscreenPageLimit(3);
            imageGalleryPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
            compositePageTransformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            });
            imageGalleryPager.setPageTransformer(compositePageTransformer);
        }
    }


    /**
     * Add the given image to the gallery view
     * @param image - Bitmap from either camera or phone's gallery
     */
    private void addImageToGallery(Bitmap image){
        if(image != null){
            featureViewObjects.getAddedBitmaps().add(image);
            sliderItems.add(new SliderItem(image));
            sliderAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Create click listeners for all buttons: cameraButton, galleryButton, closeLogo
     */
    private void createButtonListeners(){
        if(cameraButton != null) {
            cameraButton.setOnClickListener(v -> {
                if (checkAndRequestPermissions(FeatureViewActivity.this)) {
                    takePicture();
                }
            });
        }
        if(galleryButton != null) {
            galleryButton.setOnClickListener(v -> {
                if (checkAndRequestPermissions(FeatureViewActivity.this)) {
                    addFromGallery();
                }
            });
        }
        if(closeLogo != null){
            closeLogo.setOnClickListener(v -> finish());
        }
        if(saveButton != null){
            saveButton.setOnClickListener(v -> {
                if(enableSaves) {
                    saveFeatureColumnChanges();
                }
            });
        }
    }


    /**
     * Set the remaining data on the page after getting data from the geopackage
     */
    private void setFieldData(){
        if(featureViewObjects.isValid()){
            TextView titleText = (TextView) findViewById(R.id.feature_detail_title);
            titleText.setText(featureViewObjects.getGeometryType().toString());
            TextView geoNameText = (TextView) findViewById(R.id.fc_geo_name);
            geoNameText.setText(markerFeature.getDatabase());
            TextView layerNameText = (TextView) findViewById(R.id.fc_layer_name);
            layerNameText.setText(markerFeature.getTableName());

            // Feature Column recycler
            StringBuilder message = new StringBuilder();
            int geometryColumn = featureViewObjects.getFeatureRow().getGeometryColumnIndex();
            for (int i = 0; i < featureViewObjects.getFeatureRow().columnCount(); i++) {
                if (i != geometryColumn) {
                    Object value = featureViewObjects.getFeatureRow().getValue(i);

                    FeatureColumn featureColumn = featureViewObjects.getFeatureRow().getColumn(i);

                    String columnName = featureColumn.getName();
                    if (featureViewObjects.getDataColumnsDao() != null) {
                        try {
                            DataColumns dataColumn = featureViewObjects.getDataColumnsDao()
                                            .getDataColumn(featureViewObjects.getFeatureRow().getTable()
                                            .getTableName(), columnName);
                            if (dataColumn != null) {
                                columnName = dataColumn.getName();
                            }
                        } catch (SQLException e) {
                            Log.e(GeoPackageMapFragment.class.getSimpleName(),
                                    "Failed to search for Data Column name for column: " + columnName
                                            + ", Feature Table: " + featureViewObjects.getFeatureRow()
                                            .getTable().getTableName(), e);
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
            fcRecycler = findViewById(R.id.fc_recycler);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            fcRecycler.setLayoutManager(layoutManager);
            fcAdapter = new FeatureColumnAdapter(fcObjects, this);
            fcRecycler.setAdapter(fcAdapter);
        }
    }



    /**
     * Open the camera to take a picture and return the image
     */
    private void takePicture(){
        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }



    /**
     * Open the phone's image gallery to add an image
     */
    private void addFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }



    /**
     * Update our local copy of the featureRow data, then send it to the repository for a save
     */
    private void saveFeatureColumnChanges(){
        // Pull all current values from the recyclerview and save them into our featureViewObjects
        for(int i=0;i<fcAdapter.getmItems().size();i++){
            FcColumnDataObject fc = fcAdapter.getmItems().get(i);
            if(!fc.getmName().equalsIgnoreCase("id")) {
                if (fc.getmValue() instanceof String) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), fc.getmValue());
                } else if (fc.getmValue() instanceof Double) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), Double.parseDouble(fc.getmValue().toString()));
                } else if (fc.getmValue() instanceof Boolean) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), (Boolean)fc.getmValue());
                } else if (fc.getmValue() instanceof Date){
                    // don't save dates yet
                }
            }
        }
        // Call the repository to Save the data
        boolean updated = geoPackageViewModel.saveFeatureObjectValues(featureViewObjects);
        if(!updated) {
            Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
        } else{
            // Update our local arrays to merge the newly added images to the existing image list
            featureViewObjects.getBitmaps().addAll(featureViewObjects.getAddedBitmaps());
            featureViewObjects.getAddedBitmaps().clear();
        }
    }



    /**
     * Check permissions for camera use
     */
    public static boolean checkAndRequestPermissions(final Activity context) {
        int writeExternalPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writeExternalPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[0]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }



    /**
     * If permissions are granted, allow access to the camera
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(FeatureViewActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT)
                        .show();

            } else if (ContextCompat.checkSelfPermission(FeatureViewActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "FlagUp Requires Access to Your Storage.",
                        Toast.LENGTH_SHORT).show();

            } else {
                takePicture();
            }
        }
    }


    /**
     * Result from the intents to get images from either camera or gallery
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        addImageToGallery(selectedImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                addImageToGallery(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }
}

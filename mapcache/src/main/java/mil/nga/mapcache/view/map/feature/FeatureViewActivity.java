package mil.nga.mapcache.view.map.feature;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.schema.columns.DataColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.mapcache.GeoPackageMapFragment;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.listeners.DeleteImageListener;
import mil.nga.mapcache.utils.ImageUtils;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;

public class FeatureViewActivity extends AppCompatActivity {


    /**
     * Permission check for multiple
     */
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 204;

    /**
     * Permission check for gallery usage
     */
    public static final int REQUEST_ID_GALLERY_PERMISSIONS = 205;

    /**
     * We'll generate a list of FCObjects to hold our data for the recycler
     */
    private List<FcColumnDataObject> fcObjects = new ArrayList<>();

    /**
     * recycler adapter for feature column data
     */
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
    private List<SliderItem> sliderItems = new ArrayList<SliderItem>();

    /**
     * Marker feature that was selected
     */
    private MarkerFeature markerFeature;

    /**
     * ViewModel for accessing data from the repository
     */
    private GeoPackageViewModel geoPackageViewModel;

    /**
     * ViewModel for accessing data from the repository
     */
    private FeatureViewModel featureViewModel;

    /**
     * Contains all objects from the geopackage that we need to generate this view
     */
    private FeatureViewObjects featureViewObjects;

    /**
     * Recyclerview to hold the feature columns
     */
    private RecyclerView fcRecycler;

    /**
     * Listener for clicking the delete button on the images
     */
    private DeleteImageListener deleteImageListener;

    /**
     * Result listener for selecting images from the gallery.
     * Registers a photo picker activity launcher in single-select mode.
     */
    ActivityResultLauncher<PickVisualMediaRequest> getImageFromGallery =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    Bitmap image = getImageResult(uri);
                    addImageToGallery(image);
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

    /**
     * Uri for camera
     */
    private Uri cameraAppUri;

    /**
     * Result listener for taking an image from the camera
     */
    private ActivityResultLauncher<Uri> getImageFromCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        if(cameraAppUri != null) {
                            Bitmap image = getImageResult(cameraAppUri);
                            if(image != null) {
                                addImageToGallery(image);
                            }
                        }
                    }
                }
            });



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

        // Set up all buttons
        cameraButton = (MaterialButton) findViewById(R.id.add_from_camera);
        galleryButton = (MaterialButton) findViewById(R.id.add_from_gallery);
        saveButton = (MaterialButton) findViewById(R.id.feature_detail_save);
        closeLogo = (ImageView) findViewById(R.id.feature_detail_close);
        closeLogo.setBackgroundResource(R.drawable.ic_clear_grey_800_24dp);
        createButtonListeners();

        // Create recycler for feature column data
        setColumnRecycler();
        setImageRecycler();


        // Pull needed data out of the geopackage
        geoPackageViewModel = new ViewModelProvider(this).get(GeoPackageViewModel.class);
        geoPackageViewModel.init();
        if(markerFeature != null){
            featureViewObjects = geoPackageViewModel.getFeatureViewObjects(markerFeature);
        }

        // Get data about this feature point from the view model and subscribe to changes
        featureViewModel = new ViewModelProvider(this).get(FeatureViewModel.class);
        featureViewModel.init(markerFeature);
        if(featureViewModel != null){
            featureViewModel.getFeatureViewObjects().observe(this,
                    featureViewObjects -> {
                        this.featureViewObjects = featureViewObjects;
                        setFieldData();
                        updateImages();
                    });
        }

        // set up the image gallery
        createImageGallery();
    }


    /**
     * Set up the viewpager as a scrolling image gallery
     */
    private void createImageGallery(){
        if(imageGalleryPager != null && featureViewObjects != null){
            for(Map.Entry<Long, Bitmap> map  :  featureViewObjects.getBitmaps().entrySet() ){
                sliderItems.add(new SliderItem(map.getKey(),map.getValue()));
            }
            imageGalleryPager.setAdapter(sliderAdapter);
            imageGalleryPager.setClipToPadding(false);
            imageGalleryPager.setClipChildren(false);
            imageGalleryPager.setOffscreenPageLimit(3);

            // Formatting how the view pager looks
            float pageMarginPx = getResources().getDimension(R.dimen.pageMargin);
            float offsetPx = getResources().getDimension(R.dimen.offset);
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer((page, position) -> {
                ViewParent viewPager = page.getParent().getParent();
                float offset = position * -(2 * offsetPx + pageMarginPx);
                if(ViewCompat.getLayoutDirection((View) viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL){
                    page.setTranslationX(-offset);
                } else{
                    page.setTranslationX(offset);
                }
            });
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
            // We can put negative values for the id since it's assigned during saving.  Make it negative so
            // we can tell later if it's been assigned by us or created in the gpkg
            long newMediaId = -1;
            if(!sliderAdapter.getSliderItems().isEmpty()) {
                newMediaId = sliderAdapter.getNewUniqueKey();
            }
            sliderItems.add(new SliderItem(newMediaId,image));
            sliderAdapter.setData(sliderItems);
        }
    }


    /**
     * Create click listeners for all buttons: cameraButton, galleryButton, closeLogo, deleteImage
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
                if (checkAndRequestGalleryPermissions(FeatureViewActivity.this)) {
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
        // Remove image from our local copy in featureViewObjects and tell the slider adapter
        // to delete as well
        deleteImageListener = (view, actionType, rowId) -> {
            if(rowId >= 0) {
                featureViewModel.deleteImageFromFeature(featureViewObjects, rowId, markerFeature);
            } else {
                featureViewObjects.getAddedBitmaps().remove(rowId);
                int itemIndex = -1;
                for(SliderItem item : sliderItems){
                    if(item.getMediaId() == rowId){
                        itemIndex = sliderItems.indexOf(item);
                    }
                }
                if(itemIndex >= 0) {
                    sliderItems.remove(itemIndex);
                }
            }
            sliderAdapter.remove(rowId);
        };
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
            if(fcAdapter != null){
                fcAdapter.setData(featureViewObjects.getFcObjects());
            }
        }
    }


    /**
     * Create the Feature Column Ryclerview
     */
    private void setColumnRecycler(){
        fcRecycler = findViewById(R.id.fc_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        fcRecycler.setLayoutManager(layoutManager);
        fcAdapter = new FeatureColumnAdapter(this);
        fcRecycler.setAdapter(fcAdapter);
    }

    /**
     * Create the image gallery recyclerview
     */
    private void setImageRecycler(){
        imageGalleryPager = findViewById(R.id.attachmentPager);
        sliderAdapter = new SliderAdapter(imageGalleryPager, deleteImageListener);
        if(imageGalleryPager != null){
            imageGalleryPager.setAdapter(sliderAdapter);
            imageGalleryPager.setClipToPadding(false);
            imageGalleryPager.setClipChildren(false);
            imageGalleryPager.setOffscreenPageLimit(3);

            // Formatting how the view pager looks
            float pageMarginPx = getResources().getDimension(R.dimen.pageMargin);
            float offsetPx = getResources().getDimension(R.dimen.offset);
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer((page, position) -> {
                ViewParent viewPager = page.getParent().getParent();
                float offset = position * -(2 * offsetPx + pageMarginPx);
                if(ViewCompat.getLayoutDirection((View) viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL){
                    page.setTranslationX(-offset);
                } else{
                    page.setTranslationX(offset);
                }
            });
            compositePageTransformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            });
            imageGalleryPager.setPageTransformer(compositePageTransformer);
        }
    }

    /**
     * Updates the images in the gallery view pager
     */
    private void updateImages(){
        if(featureViewObjects != null){
            sliderItems.clear();
            featureViewObjects.getAddedBitmaps().clear();
            for(Map.Entry<Long, Bitmap> map  :  featureViewObjects.getBitmaps().entrySet() ){
                sliderItems.add(new SliderItem(map.getKey(),map.getValue()));
            }
        }
        sliderAdapter.setData(sliderItems);
    }



    /**
     * Open the camera to take a picture and return the image
     */
    private void takePicture(){
        // Open the camera and get the photo
        String fileName = "image_gallery_";
        File outputDir = getCacheDir();
        File file;
        try{
            file = File.createTempFile( fileName, ".jpg", outputDir );
            String pkg = this.getApplicationContext().getPackageName();
            cameraAppUri = FileProvider.getUriForFile(
                    Objects.requireNonNull(getApplicationContext()),
                    this.getApplicationContext().getPackageName()
                            + ".fileprovider", file );
            getImageFromCamera.launch(cameraAppUri);
        } catch( Exception e ) {
            Log.e("Error saving image: ", e.toString());
        }
    }



    /**
     * Open the phone's image gallery to add an image
     */
    private void addFromGallery(){
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
        getImageFromGallery.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(mediaType)
                .build());
    }



    /**
     * Update our local copy of the featureRow data, then send it to the repository for a save
     */
    private void saveFeatureColumnChanges(){
        // Pull all current values from the recyclerview and save them into our featureViewObjects
        for(int i=0;i<fcAdapter.getmItems().size();i++){
            FcColumnDataObject fc = fcAdapter.getmItems().get(i);
            if(!fc.getmName().equalsIgnoreCase("id")) {
                if (String.class.equals(fc.getFormat())) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), fc.getmValue());
                } else if (Double.class.equals(fc.getFormat())) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), Double.parseDouble(fc.getmValue().toString()));
                } else if (Integer.class.equals(fc.getFormat())) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), Integer.parseInt(fc.getmValue().toString()));
                } else if (Long.class.equals(fc.getFormat())) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), Long.parseLong(fc.getmValue().toString()));
                } else if (Boolean.class.equals(fc.getFormat())) {
                    featureViewObjects.getFeatureRow().setValue(fc.getmName(), (Boolean) fc.getmValue());
                } else if (Date.class.equals(fc.getFormat())) {
                    // Don't save dates yet
                }
            }
        }

        // When saving, the only images that will be added to the geopackage will come from
        // featureViewObjects.getAddedBitmaps.  First add all "new" images to that list from
        // the slider adapter
        featureViewObjects.getAddedBitmaps().clear();
        for(SliderItem imageItem : sliderAdapter.getSliderItems()){
            if(imageItem.getMediaId() < 0){
                featureViewObjects.getAddedBitmaps().put(imageItem.getMediaId(), imageItem.getImage());
            }
        }
        // Write to the geopackage from the repository
        featureViewModel.saveFeatureObjectValues(featureViewObjects, markerFeature);

    }


    /**
     * Gets a correctly oriented Bitmap image from the Uri
     * @param uri - Uri for an image
     * @return - Bitmap, rotated if it needs to be
     */
    private Bitmap getImageResult(Uri uri){
        if(uri != null) {
            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                Bitmap rotatedImage = ImageUtils.rotateFromUri(uri, getBaseContext(), image);
                parcelFileDescriptor.close();
                return rotatedImage;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    /**
     * Check permissions for camera use.  Note, only check write_external if < android 10
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
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            if (writeExternalPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded
                        .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[0]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static boolean checkAndRequestGalleryPermissions(final Activity context) {
        int writeExternalPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            if (writeExternalPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded
                        .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[0]),
                    REQUEST_ID_GALLERY_PERMISSIONS);
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
        boolean flagged = false;
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(FeatureViewActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                                "Camera permissions required", Toast.LENGTH_SHORT)
                        .show();
                flagged = true;
            } else {
                takePicture();
            }
        }
        if (requestCode == REQUEST_ID_GALLERY_PERMISSIONS) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(FeatureViewActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Storage permissions required",
                            Toast.LENGTH_SHORT).show();
                    flagged = true;
                }
            }
            if(!flagged){
                addFromGallery();
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
                        try {
                            Bitmap rotatedImage = ImageUtils.rotateBitmap(selectedImage);
                            addImageToGallery(rotatedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
                                try {
                                    Bitmap rotatedImage = ImageUtils.rotateBitmapFromPath(picturePath);
                                    addImageToGallery(rotatedImage);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }
}

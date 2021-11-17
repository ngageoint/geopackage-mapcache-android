package mil.nga.mapcache.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mil.nga.mapcache.R;

/**
 * Image handling utilities
 */
public class ImageUtils {

    /**
     * Take an image path, load bitmap, and rotate it to correct orientation if needed
     * @param picturePath Image location
     */
    public static Bitmap rotateBitmapFromPath(String picturePath) throws IOException {
        Bitmap newImage = BitmapFactory.decodeFile(picturePath);
        try {
            ExifInterface exif = new ExifInterface(picturePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(newImage, 0, 0, newImage.getWidth(), newImage.getHeight(), matrix, true);
                return rotatedBitmap;
            } else {
                return newImage;
            }
        } catch (Exception e) {
            Log.e("Error getting image: ", e.toString());
        }
        return newImage;
    }


    /**
     * Take a bitmap image and rotate it to correct orientation if needed
     * @param newImage Bitmap image
     */
    public static Bitmap rotateBitmap(Bitmap newImage) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(newImage, 0, 0, newImage.getWidth(), newImage.getHeight(), matrix, true);
                return rotatedBitmap;
            } else {
                return newImage;
            }
        } catch (Exception e) {
            Log.e("Error getting image: ", e.toString());
        }
        return newImage;
    }

    /**
     * Pull exif data from Uri, and rotate the given image based on the exif data
     */
    public static Bitmap rotateFromUri(Uri uri, Context context, Bitmap image) throws IOException {
        int orientation;
        ExifInterface exif;
        if(uri != null) {
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is != null) {
                    exif = new ExifInterface(is);
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                        return rotatedBitmap;
                    }
                }
            } catch (Exception e) {
                Log.e("Error loading exif: ", e.toString());
            }
        }
        return image;
    }
}

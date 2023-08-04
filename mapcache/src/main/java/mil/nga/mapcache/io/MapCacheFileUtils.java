package mil.nga.mapcache.io;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.io.File;

/**
 * MapCache File Utilities
 */
public class MapCacheFileUtils {

    /**
     * Get the display name from the URI and path
     *
     * @param context
     * @param uri
     * @param path
     * @return
     */
    public static String getDisplayName(Context context, Uri uri, String path) {

        // Try to get the GeoPackage name
        String name = null;
        if (path != null) {
            name = new File(path).getName();
        } else {
            name = getDisplayName(context, uri);
        }

        // Remove the extension
        if (name != null) {
            int extensionIndex = name.lastIndexOf(".");
            if (extensionIndex > -1) {
                name = name.substring(0, extensionIndex);
            }
        }

        return name;
    }

    /**
     * Get display name from the uri
     *
     * @param context
     * @param uri
     * @return
     */
    private static String getDisplayName(Context context, Uri uri) {

        String name = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor nameCursor = resolver.query(uri, null, null, null, null);
        try {
            if (nameCursor.getCount() > 0) {
                int displayNameIndex = nameCursor
                        .getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                if (displayNameIndex >= 0 && nameCursor.moveToFirst()) {
                    name = nameCursor.getString(displayNameIndex);
                }
            }
        } finally {
            nameCursor.close();
        }

        if (name == null) {
            name = uri.getPath();
            int index = name.lastIndexOf('/');
            if (index != -1) {
                name = name.substring(index + 1);
            }
        }

        return name;
    }

}

package mil.nga.mapcache.utils;

import android.content.Context;

import mil.nga.mapcache.R;

/**
 *  Validate tile URLs for geopackage format by checking for xyz and bounding box
 */

public class UrlValidator {

    /**
     * Determine if the url has x, y, or z variables
     *
     * @param url
     * @return true if it's a valid xyz url
     */
    public static boolean hasXYZ(Context context, String url) {

        String replacedUrl = replaceXYZ(context, url, 0, 0, 0);
        boolean hasXYZ = !replacedUrl.equals(url);

        return hasXYZ;
    }


    /**
     * Replace x, y, and z in the url
     *
     * @param url
     * @param z
     * @param x
     * @param y
     * @return
     */
    private static String replaceXYZ(Context context, String url, int z, long x, long y) {

        url = url.replaceAll(
                context.getResources().getString(R.string.tile_generator_variable_z),
                String.valueOf(z));
        url = url.replaceAll(
                context.getString(R.string.tile_generator_variable_x),
                String.valueOf(x));
        url = url.replaceAll(
                context.getString(R.string.tile_generator_variable_y),
                String.valueOf(y));
        return url;
    }
}

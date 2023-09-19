package mil.nga.mapcache.utils;

import android.content.Context;

import mil.nga.mapcache.R;

/**
 *  Validate tile URLs for geopackage format by checking for xyz
 */

public class UrlValidator {

    /**
     * A URL is valid if it has either xyz or wms params
     * @param context - context for grabbing string resources
     * @param url url to validate
     * @return true if the url has xyz or wms info
     */
    public static boolean isValidTileUrl(Context context, String url){
        if(hasXYZ(context, url) || hasWms(url)){
            return true;
        }
        return false;
    }

    /**
     * Determine if the url is valid based on whether it has proper wms or not
     * @param url url to validate
     * @return true if the url has xyz info
     */
    public static boolean hasWms(String url){
        if (url.toLowerCase().contains("wms")) {
            return true;
        }
        return false;
    }


    /**
     * Determine if the url has x, y, or z variables
     *
     * @param url url to look for xyz data
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
     * @param url url to modify
     * @param z integer to replace the Z param with
     * @param x integer to replace the X param with
     * @param y integer to replace the Y param with
     * @return string with the xyz portion replaced with the given int values
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

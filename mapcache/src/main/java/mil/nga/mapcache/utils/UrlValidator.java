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
     * Determine if the url is valid based on whether it has proper wms or not.  Looking at required
     * variables: https://enterprise.arcgis.com/en/server/latest/publish-services/windows/communicating-with-a-wms-service-in-a-web-browser.htm#GUID-90AAB875-0337-451B-86BD-FC5E30F6990A
     * @param url url to validate
     * @return true if the url has proper wms variables
     */
    public static boolean hasWms(String url){
        url = url.toLowerCase();
        boolean valid = true;
        if (!url.contains("request")) {
            valid = false;
        }
        // GetCapabilities request
        if(url.contains("capabilities")){
            if(!url.contains("service=")){
                valid = false;
            }
        }
        // GetMap request
        if(url.contains("request=getmap") || url.contains("request=map")){
            String[] getMapVars = {"layers", "styles", "crs", "bbox", "width", "height", "format"};
            for (String item : getMapVars) {
                if (!url.contains(item)) {
                    valid = false;
                    break;
                }
            }
            if(!(url.contains("version") || url.contains("wmtver"))){
                valid = false;
            }
            if(!(url.contains("crs") || url.contains("srs"))){
                valid = false;
            }
        }
        // GetFeatureInfo request
        if(url.contains("request=getfeatureinfo") || url.contains("request=feature_info")){
            if(!(url.contains("version") || url.contains("wmtver"))){
                valid = false;
            }
            if(!url.contains("query_layers")){
                valid = false;
            }
            if(!(url.contains("&i=") || url.contains("&x="))){
                valid = false;
            }
            if(!(url.contains("j=") || url.contains("y="))){
                valid = false;
            }
        }
        // GetStyles  request
        if(url.contains("request=getstyles")) {
            if (!url.contains("version=")) {
                valid = false;
            }
            if (!url.contains("layers=")) {
                valid = false;
            }
        }
        // GetLegendGraphic  request
        if(url.contains("request=getlegendgraphic")) {
            if (!url.contains("version=")) {
                valid = false;
            }
            if (!url.contains("layer=")) {
                valid = false;
            }
        }
            return valid;
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

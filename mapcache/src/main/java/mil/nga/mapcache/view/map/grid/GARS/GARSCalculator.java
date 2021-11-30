package mil.nga.mapcache.view.map.grid.GARS;

/**
 * Calculates the GARs labels based on latitude and longitude.
 */
public class GARSCalculator {

    /**
     * The array of GARs letters.
     */
    private static char[] letter_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * The array of GARs grid numbers.
     */
    private static char[][] five_minute_array = {{'7', '4', '1'}, {'8', '5', '2'}, {'9', '6', '3'}};

    /**
     * Calculates a basic latitude longitude string.
     *
     * @param lat      The latitude to get the label for.
     * @param lng      The longitude to get the label for.
     * @param rounding The rounding increment.
     * @return The latitude longitude string.
     */
    public String latLng2Name(double lat, double lng, double rounding) {
        double latitude = Math.floor(Math.abs(lat));
        latitude -= latitude % rounding;
        double longitude = Math.floor(lng);
        longitude -= longitude % rounding;

        longitude = fixLongitude((longitude));
        char longitudeCardinal = longitude >= 0 && longitude < 180.0 ? 'E' : 'W';
        char latitudeCardinal = lat >= 0 ? 'N' : 'S';
        return String.valueOf(Math.abs(longitude)) + String.valueOf(longitudeCardinal)
                + String.valueOf(latitude) + String.valueOf(latitudeCardinal);
    }

    /**
     * Pass in a latitude longitude and get a GARs grid label.
     *
     * @param lat The latitude.
     * @param lng The longitude.
     * @return A GARs grid label.
     */
    public String latLng2GARS(double lat, double lng) {
        double latitude = lat;
        double longitude = fixLongitude(lng);
        /* North pole is an exception, read over and down */
        if (latitude == 90.0) {
            latitude = 89.99999999999;
        }
        // Check for valid lat/lon range
        if (latitude < -90 || latitude > 90) {
            return "0";
        }
        if (longitude < -180 || longitude > 180) {
            return "0";
        }
        // Get the longitude band ==============================================
        double longBand = longitude + 180;
        // Normalize to 0.0 <= longBand < 360
        while (longBand < 0) {
            longBand = longBand + 360;
        }
        while (longBand > 360) {
            longBand = longBand - 360;
        }
        longBand = Math.floor(longBand * 2.0);
        int intLongBand = (int) (longBand + 1); // Start at 001, not 000
        String strLongBand = String.valueOf(intLongBand);
        // Left pad the string with 0's so X becomes 00X
        while (strLongBand.length() < 3) {
            strLongBand = '0' + strLongBand;
        }

        // Get the latitude band ===============================================
        double offset = latitude + 90;
        // Normalize offset to 0 < offset <90
        while (offset < 0) {
            offset = offset + 180;
        }
        while (offset > 180) {
            offset = offset - 180;
        }
        offset = Math.floor(offset * 2.0);
        int firstOffest = (int) Math.floor(offset / letter_array.length);
        int secondOffest = (int) Math.floor(offset % letter_array.length);
        String strLatBand = String.valueOf(letter_array[firstOffest]) + String.valueOf(letter_array[secondOffest]);

        // Get the quadrant ====================================================
        double latBand = (Math.floor((latitude + 90.0) * 4.0) % 2.0);
        longBand = (Math.floor((longitude + 180.0) * 4.0) % 2.0);
        String quadrant = "0";
        // return "0" if error occurs
        if (latBand < 0 || latBand > 1) {
            return "0";
        }
        if (longBand < 0 || longBand > 1) {
            return "0";
        }
        // Otherwise get the quadrant
        if (latBand == 0.0 && longBand == 0.0) {
            quadrant = "3";
        } else if (latBand == 1.0 && longBand == 0.0) {
            quadrant = "1";
        } else if (latBand == 1.0 && longBand == 1.0) {
            quadrant = "2";
        } else if (latBand == 0.0 && longBand == 1.0) {
            quadrant = "4";
        }

        char keypad = five_minute_array[(int) Math.floor(((longitude + 180) * 60.0) % 30 % 15 / 5.0)]
                [(int) Math.floor(((latitude + 90) * 60.0) % 30 % 15 / 5.0)];

        return strLongBand + strLatBand + quadrant + keypad;
    }

    /**
     * Normalizes the longitude.
     *
     * @param lng The longitude to normalize.
     * @return The normalized longitude.
     */
    private double fixLongitude(double lng) {
        while (lng > 180.0) {
            lng -= 360.0;
        }
        while (lng < -180.0) {
            lng += 360.0;
        }
        return lng;
    }
}

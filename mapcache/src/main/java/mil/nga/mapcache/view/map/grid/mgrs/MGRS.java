package mil.nga.mapcache.view.map.grid.mgrs;

import com.google.android.gms.maps.model.LatLng;

import java.util.regex.Pattern;

import mil.nga.mapcache.utils.LatLonUtils;

/**
 * Represents an MGRS coordinate.
 */
public class MGRS {

    /*
     * Latitude bands C..X 8° each, covering 80°S to 84°N
     */
    private static String latBands = "CDEFGHJKLMNPQRSTUVWXX"; // X is repeated for 80-84°N

    /*
     * 100km grid square column (‘e’) letters repeat every third zone
     */
    private static String[] e100kLetters = {"ABCDEFGH", "JKLMNPQR", "STUVWXYZ"};

    /*
     * 100km grid square row (‘n’) letters repeat every other zone
     */
    private static String[] n100kLetters = {"ABCDEFGHJKLMNPQRSTUV", "FGHJKLMNPQRSTUVABCDE"};

    /**
     * An mgrs pattern.
     */
    private static Pattern mgrsPattern = Pattern.compile("^(\\d{1,2})([^ABIOYZabioyz])([A-Za-z]{2})([0-9][0-9]+$)");

    /**
     * The zone of this MGRS coordinate.
     */
    private int zone;

    /**
     * The band of this MGRS coordinate.
     */
    private char band;

    /**
     * The e100k character.
     */
    private char e100k;

    /**
     * The n100k character.
     */
    private char n100k;

    /**
     * The easting coordinate.
     */
    private double easting;

    /**
     * The northing coordinate.
     */
    private double northing;

    /**
     * Constructor.
     *
     * @param zone     The zone.
     * @param band     The band.
     * @param e100k    The e100k character.
     * @param n100k    The n100k character.
     * @param easting  The easting coordinate.
     * @param northing The northing coordinate.
     */
    public MGRS(int zone, char band, char e100k, char n100k, double easting, double northing) {
        this.zone = zone;
        this.band = band;
        this.e100k = e100k;
        this.n100k = n100k;
        this.easting = easting;
        this.northing = northing;
    }

    /**
     * Gets the zone.
     *
     * @return The zone.
     */
    private int getZone() {
        return this.zone;
    }

    /**
     * Gets the band.
     *
     * @return The band.
     */
    private char getBand() {
        return this.band;
    }

    /**
     * Gets the e100k character.
     *
     * @return The e100k character.
     */
    private char getE100k() {
        return this.e100k;
    }

    /**
     * Gets the n100k character.
     *
     * @return The n100k character.
     */
    private char getN100k() {
        return this.n100k;
    }

    /**
     * Gets the easting coordinate.
     *
     * @return The easting coordinate.
     */
    private double getEasting() {
        return this.easting;
    }

    /**
     * Gets the northing coordinate.
     *
     * @return The northing coordinate.
     */
    private double getNorthing() {
        return this.northing;
    }

    /**
     * Return whether the given string is valid MGRS string
     *
     * @param mgrs string to test
     * @return true if MGRS string is valid, false otherwise.
     */
    private boolean isMGRS(String mgrs) {
        return mgrsPattern.matcher(mgrs).matches();
    }

    /**
     * Encodes a latitude/longitude as MGRS string.
     *
     * @param latLngIn
     * @return mgrs.
     */
    private MGRS from(LatLng latLngIn) {
        LatLng latLng = new LatLng(latLngIn.latitude, LatLonUtils.getInstance().fixLongitude(latLngIn.longitude));
        UTM utm = UTM.from(latLng, null, null);

        // grid zones are 8° tall, 0°N is 10th band
        char band = latBands.charAt((int) Math.floor(latLng.latitude / 8.0 + 10.0)); // latitude band

        // columns in zone 1 are A-H, zone 2 J-R, zone 3 S-Z, then repeating every 3rd zone
        int column = (int) Math.floor(utm.getEasting() / 100000);
        char e100k = e100kLetters[(utm.getZoneNumber() - 1) % 3].charAt(column - 1); // col-1 since 1*100e3 -> A (index 0), 2*100e3 -> B (index 1), etc.

        // rows in even zones are A-V, in odd zones are F-E
        int row = (int) Math.floor(utm.getNorthing() / 100000) % 20;
        char n100k = n100kLetters[(utm.getZoneNumber() - 1) % 2].charAt(row);

        // truncate easting/northing to within 100km grid square
        long easting = Math.round(utm.getEasting() % 100000);
        long northing = Math.round(utm.getNorthing() % 100000);

        return new MGRS(utm.getZoneNumber(), band, e100k, n100k, easting, northing);
    }

    @Override
    public String toString() {
        return String.valueOf(this.zone) + this.band + " " + this.e100k + this.n100k + " "
                + this.easting + " " + this.northing;
    }

    /**
     * Converts the MGRS coordinate to a UTM coordinate.
     *
     * @return The MGRS coordinate as a UTM.
     */
    private UTM utm() {
        // get easting specified by e100k
        int col = e100kLetters[(this.zone - 1) % 3].indexOf(this.e100k) + 1; // index+1 since A (index 0) -> 1*100e3, B (index 1) -> 2*100e3, etc.
        int e100kNum = col * 100000; // e100k in meters

        // get northing specified by n100k
        int row = n100kLetters[(this.zone - 1) % 2].indexOf(this.n100k);
        int n100kNum = row * 100000; // n100k in meters

        // get latitude of (bottom of) band
        double latBand = (latBands.indexOf(this.band) - 10) * 8;

        // northing of bottom of band, extended to include entirety of bottommost 100km square
        // (100km square boundaries are aligned with 100km UTM northing intervals)

        UTM utm = UTM.from(new LatLng(latBand, 0), null, null);
        double nBand = Math.floor(utm.getNorthing() / 100000) * 100000;

        // 100km grid square row letters repeat every 2,000km north; add enough 2,000km blocks to get
        // into required band
        double n2M = 0; // northing of 2,000km block
        while (n2M + n100kNum + this.northing < nBand) {
            n2M += 2000000;
        }

        String hemisphere = this.band >= 'N' ? UTM.HEMISPHERE_NORTH : UTM.HEMISPHERE_SOUTH;

        return new UTM(this.zone, hemisphere, e100kNum + this.easting, n2M + n100kNum + this.northing);
    }

    /**
     * Get the two letter 100k designator for a given UTM easting,
     * northing and zone number value.
     *
     * @param {number} easting
     * @param {number} northing
     * @param {number} zoneNumber
     * @return the two letter 100k designator for the given UTM location.
     * @private
     */
    public static String get100KId(double easting, double northing, int zoneNumber) {
        // columns in zone 1 are A-H, zone 2 J-R, zone 3 S-Z, then repeating every 3rd zone
        int column = (int) Math.floor(easting / 100000);
        char e100k = e100kLetters[(zoneNumber - 1) % 3].charAt(column - 1); // col-1 since 1*100e3 -> A (index 0), 2*100e3 -> B (index 1), etc.

        // rows in even zones are A-V, in odd zones are F-E
        int row = (int) Math.floor(northing / 100000) % 20;
        char n100k = n100kLetters[(zoneNumber - 1) % 2].charAt(row);

        return String.valueOf(e100k) + n100k;
    }
}

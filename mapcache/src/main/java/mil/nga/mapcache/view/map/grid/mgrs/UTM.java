package mil.nga.mapcache.view.map.grid.mgrs;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class that represents the UTM coordinate system.
 */
public class UTM {
    /**
     * North hemisphere string.
     */
    public static String HEMISPHERE_NORTH = "NORTH";

    /**
     * South hemisphere string.
     */
    public static String HEMISPHERE_SOUTH = "SOUTH";

    /**
     * The zone number.
     */
    private int zoneNumber;

    /**
     * The hemisphere.
     */
    private String hemisphere;

    /**
     * The easting value.
     */
    private double easting;

    /**
     * The northing value.
     */
    private double northing;

    /**
     * Constructs a new UTM coordinate.
     *
     * @param zoneNumber The zone number.
     * @param hemisphere The hemisphere.
     * @param easting    The easting value.
     * @param northing   The northing value.
     */
    public UTM(int zoneNumber, String hemisphere, double easting, double northing) {
        this.zoneNumber = zoneNumber;
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }

    /**
     * Gets the zone number.
     *
     * @return The zone number.
     */
    public int getZoneNumber() {
        return this.zoneNumber;
    }

    /**
     * Gets the hemisphere.
     *
     * @return The hemisphere.
     */
    public String getHemisphere() {
        return this.hemisphere;
    }

    /**
     * Gets the easting value.
     *
     * @return The easting value.
     */
    public double getEasting() {
        return this.easting;
    }

    /**
     * Gets the northing value.
     *
     * @return The northing value.
     */
    public double getNorthing() {
        return this.northing;
    }

    /**
     * Given a latitude and longitude, converts them to a new UTM coordinate.
     *
     * @param latLng     The latitude and longitude.
     * @param zoneIn     Optional UTM zone.
     * @param hemisphere Optional hemisphere.
     * @return A new UTM coordinate.
     */
    public static UTM from(LatLng latLng, Integer zoneIn, String hemisphere) {
        int zone;
        if (zoneIn == null) {
            zone = (int) Math.floor(latLng.longitude / 6 + 31);
        } else {
            zone = zoneIn.intValue();
        }

        if (hemisphere == null) {
            hemisphere = latLng.latitude >= 0 ? HEMISPHERE_NORTH : HEMISPHERE_SOUTH;
        }
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        double easting = 0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2) / 3) + 500000;
        easting = Math.round(easting * 100) * 0.01;
        double northing = (Math.atan(Math.tan(latitude * Math.PI / 180) / Math.cos((longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) - latitude * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin((longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin((longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (latitude * Math.PI / 180 - 0.005054622556 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 4 + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 3);

        if (hemisphere == HEMISPHERE_SOUTH) {
            northing = northing + 10000000;
        }

        northing = Math.round(northing * 100) * 0.01;

        return new UTM(zone, hemisphere, easting, northing);
    }
}

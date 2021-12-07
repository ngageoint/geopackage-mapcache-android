package mil.nga.mapcache.view.map.grid.mgrs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.utils.LatLonUtils;

/**
 * The GZD zones.
 */
public class GZDZones {

    /**
     * Interesting zones.
     */
    private Map<String, int[][]> interestingZones = new HashMap<>();

    /**
     * Latitude zones.
     */
    private Map<String, double[]> latitudeGZDZones = new HashMap<>();

    /**
     * Longitude zones.
     */
    private double[][] longitudeGZDZones = {{-180.0, -174.0},
            {-174.0, -168.0},
            {-168.0, -162.0},
            {-162.0, -156.0},
            {-156.0, -150.0},
            {-150.0, -144.0},
            {-144.0, -138.0},
            {-138.0, -132.0},
            {-132.0, -126.0},
            {-126.0, -120.0},
            {-120.0, -114.0},
            {-114.0, -108.0},
            {-108.0, -102.0},
            {-102.0, -96.0},
            {-96.0, -90.0},
            {-90.0, -84.0},
            {-84.0, -78.0},
            {-78.0, -72.0},
            {-72.0, -66.0},
            {-66.0, -60.0},
            {-60.0, -54.0},
            {-54.0, -48.0},
            {-48.0, -42.0},
            {-42.0, -36.0},
            {-36.0, -30.0},
            {-30.0, -24.0},
            {-24.0, -18.0},
            {-18.0, -12.0},
            {-12.0, -6.0},
            {-6.0, 0.0},
            {0.0, 6.0},
            {6.0, 12.0},
            {12.0, 18.0},
            {18.0, 24.0},
            {24.0, 30.0},
            {30.0, 36.0},
            {36.0, 42.0},
            {42.0, 48.0},
            {48.0, 54.0},
            {54.0, 60.0},
            {60.0, 66.0},
            {66.0, 72.0},
            {72.0, 78.0},
            {78.0, 84.0},
            {84.0, 90.0},
            {90.0, 96.0},
            {96.0, 102.0},
            {102.0, 108.0},
            {108.0, 114.0},
            {114.0, 120.0},
            {120.0, 126.0},
            {126.0, 132.0},
            {132.0, 138.0},
            {138.0, 144.0},
            {144.0, 150.0},
            {150.0, 156.0},
            {156.0, 162.0},
            {162.0, 168.0},
            {168.0, 174.0},
            {174.0, 180.0}};


    /**
     * Constructor.
     */
    public GZDZones() {
        interestingZones.put("X", new int[][]{{9, 29}, {31, 31}, {33, 33}, {35, 35}, {37, 57}});
        interestingZones.put("W", new int[][]{{1, 60}});
        interestingZones.put("V", new int[][]{{1, 24}, {27, 60}});
        interestingZones.put("U", new int[][]{{1, 5}, {8, 22}, {29, 60}});
        interestingZones.put("T", new int[][]{{9, 22}, {29, 56}});
        interestingZones.put("S", new int[][]{{10, 20}, {25, 54}});
        interestingZones.put("R", new int[][]{{1, 3}, {11, 18}, {27, 54}, {56, 56}});
        interestingZones.put("Q", new int[][]{{2, 5}, {11, 20}, {26, 51}, {55}, {58}});
        interestingZones.put("P", new int[][]{{12, 12}, {14, 21}, {26, 40}, {42, 44}, {46, 59}});
        interestingZones.put("N", new int[][]{{1, 1}, {3, 4}, {15, 22}, {28, 39}, {43, 44}, {46, 60}});
        interestingZones.put("M", new int[][]{{1, 5}, {7, 7}, {15, 25}, {29, 29}, {31, 40}, {42, 43}, {47, 60}});
        interestingZones.put("L", new int[][]{{1, 8}, {17, 25}, {29, 30}, {32, 40}, {47, 60}});
        interestingZones.put("K", new int[][]{{1, 9}, {18, 26}, {29, 30}, {32, 41}, {49, 60}});
        interestingZones.put("J", new int[][]{{1, 1}, {6, 7}, {9, 10}, {12, 13}, {17, 23}, {33, 38}, {49, 57}, {59, 59}});
        interestingZones.put("H", new int[][]{{17, 22}, {28, 28}, {33, 36}, {43, 44}, {49, 56}, {59, 60}});
        interestingZones.put("G", new int[][]{{1, 2}, {18, 20}, {29, 29}, {37, 37}, {39, 40}, {54, 56}, {58, 60}});
        interestingZones.put("F", new int[][]{{18, 21}, {23, 25}, {31, 31}, {42, 43}, {57, 60}});
        interestingZones.put("E", new int[][]{{23, 23}, {26, 26}});

        latitudeGZDZones.put("C", new double[]{-80.0, -72.0});
        latitudeGZDZones.put("D", new double[]{-72.0, -64.0});
        latitudeGZDZones.put("E", new double[]{-64.0, -56.0});
        latitudeGZDZones.put("F", new double[]{-56.0, -48.0});
        latitudeGZDZones.put("G", new double[]{-48.0, -40.0});
        latitudeGZDZones.put("H", new double[]{-40.0, -32.0});
        latitudeGZDZones.put("J", new double[]{-32.0, -24.0});
        latitudeGZDZones.put("K", new double[]{-24.0, -16.0});
        latitudeGZDZones.put("L", new double[]{-16.0, -8.0});
        latitudeGZDZones.put("M", new double[]{-8.0, 0.0});
        latitudeGZDZones.put("N", new double[]{0.0, 8.0});
        latitudeGZDZones.put("P", new double[]{8.0, 16.0});
        latitudeGZDZones.put("Q", new double[]{16.0, 24.0});
        latitudeGZDZones.put("R", new double[]{24.0, 32.0});
        latitudeGZDZones.put("S", new double[]{32.0, 40.0});
        latitudeGZDZones.put("T", new double[]{40.0, 48.0});
        latitudeGZDZones.put("U", new double[]{48.0, 56.0});
        latitudeGZDZones.put("V", new double[]{56.0, 64.0});
        latitudeGZDZones.put("W", new double[]{64.0, 72.0});
        latitudeGZDZones.put("X", new double[]{72.0, 84.0});
    }

    /**
     * Tests if ranges overlap
     *
     * @param range1 First range to check.
     * @param range2 Second range to check.
     * @return True if they overlap, false if they dont.
     */
    private boolean rangesOverlap(double[] range1, double[] range2) {
        double x1 = Math.max(range1[0], range2[0]);
        double x2 = Math.min(range1[1], range2[1]);
        return x1 <= x2;
    }

    /**
     * Finds the various bounding boxes within the specified bounding box.
     *
     * @param bbox The overall bounding box.
     * @return The list of bounding boxes.
     */
    private List<BoundingBox> determineBoundingBoxesInRange(BoundingBox bbox) {
        List<BoundingBox> bboxes = new ArrayList<>();
        double minLon = LatLonUtils.getInstance().fixLongitude(bbox.getMinLongitude());
        double maxLon = LatLonUtils.getInstance().fixLongitude(bbox.getMaxLongitude());
        // cross antimeridian and needs to be split
        if (minLon > maxLon) {
            bboxes.add(new BoundingBox(maxLon, bbox.getMinLatitude(), 180.0, bbox.getMaxLatitude()));
            bboxes.add(new BoundingBox(-180.0, bbox.getMinLatitude(), minLon, bbox.getMaxLatitude()));
        } else {
            bboxes.add(new BoundingBox(minLon, bbox.getMinLatitude(), maxLon, bbox.getMaxLatitude()));
        }

        return bboxes;
    }

    /**
     * Checks to see if the zone is an interesting zone.
     *
     * @param zoneLetter The zone's letter.
     * @param zoneNumber The zone's number.
     * @return True if its an interesting zone, false if it is not.
     */
    private boolean isInteresting(String zoneLetter, int zoneNumber) {
        boolean interesting = false;
        int[][] interestingRanges = interestingZones.get(zoneLetter);
        if (interestingRanges != null) {
            for (int i = 0; i < interestingRanges.length && !interesting; i++) {
                int[] range = interestingRanges[i];
                interesting = zoneNumber >= range[0] && zoneNumber <= range[1];
            }
        }
        return interesting;
    }

    /**
     * determine zones within a given bounding box
     *
     * @param bbox            The overall bounding box.
     * @param interestingOnly True if we only want interesting zones.
     * @return A list of grid zones within the bounding box.
     */
    public List<GridZoneDesignator> zonesWithin(BoundingBox bbox, boolean interestingOnly) {
        List<BoundingBox> bboxes = determineBoundingBoxesInRange(bbox);
        List<GridZoneDesignator> zones = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : latitudeGZDZones.entrySet()) {
            double[] latRange = entry.getValue();
            boolean overlap = false;
            for (int i = 0; i < bboxes.size() && !overlap; i++) {
                BoundingBox abox = bboxes.get(i);
                if (rangesOverlap(latRange, new double[]{abox.getMinLatitude(), abox.getMaxLatitude()})) {
                    overlap = true;
                }
            }
            if (overlap) {
                String latitudeZone = entry.getKey();
                for (int i = 0; i < longitudeGZDZones.length; i++) {
                    int longitudeZone = i + 1;
                    double[] lngRange = longitudeGZDZones[i];
                    boolean skip = false;
                    if (latitudeZone.equals("V")) {
                        if (longitudeZone == 31) {
                            lngRange[0] = 0.0;
                            lngRange[1] = 3.0;
                        } else if (longitudeZone == 32) {
                            lngRange[0] = 3.0;
                            lngRange[1] = 12.0;
                        }
                    } else if (latitudeZone.equals("X")) {
                        if (longitudeZone == 31) {
                            lngRange[0] = 0.0;
                            lngRange[1] = 9.0;
                        } else if (longitudeZone == 32) {
                            skip = true;
                        } else if (longitudeZone == 33) {
                            lngRange[0] = 9.0;
                            lngRange[1] = 21.0;
                        } else if (longitudeZone == 34) {
                            skip = true;
                        } else if (longitudeZone == 35) {
                            lngRange[0] = 21.0;
                            lngRange[1] = 33.0;
                        } else if (longitudeZone == 36) {
                            skip = true;
                        } else if (longitudeZone == 37) {
                            lngRange[0] = 33.0;
                            lngRange[1] = 42.0;
                        }
                    }

                    overlap = false;
                    for (int j = 0; j < bboxes.size() && !overlap; j++) {
                        BoundingBox abox = bboxes.get(j);
                        if (rangesOverlap(lngRange, new double[]{abox.getMinLongitude(), abox.getMaxLongitude()})) {
                            overlap = true;
                        }
                    }
                    if (!skip && overlap && (!interestingOnly || isInteresting(latitudeZone, longitudeZone))) {
                        zones.add(new GridZoneDesignator(latitudeZone, longitudeZone, new BoundingBox(lngRange[0], latRange[0], lngRange[1], latRange[1])));
                    }
                }
            }
        }
        return zones;
    }
}

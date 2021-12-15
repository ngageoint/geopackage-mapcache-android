package mil.nga.mapcache.view.map.grid.mgrs;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.mapcache.view.map.grid.Grid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A GridZoneDesignator.
 */
public class GridZoneDesignator {
    /**
     * The zone letter.
     */
    private String zoneLetter;

    /**
     * The hemisphere.
     */
    private String hemisphere;

    /**
     * The zone number.
     */
    private int zoneNumber;

    /**
     * The bounding box.
     */
    private BoundingBox zoneBounds;

    /**
     * The utm bounds.
     */
    private double[] zoneUtmBounds;

    /**
     * The zones grid.
     */
    private Polygon zonePolygon;

    /**
     * Constructs a new grid zone designator.
     *
     * @param zoneLetter The zone's letter.
     * @param zoneNumber The zone's number.
     * @param zoneBounds The zones bounds.
     */
    public GridZoneDesignator(String zoneLetter, int zoneNumber, BoundingBox zoneBounds) {
        this.zoneLetter = zoneLetter;
        this.hemisphere = zoneLetter.compareTo("N") < 0 ? UTM.HEMISPHERE_SOUTH : UTM.HEMISPHERE_NORTH;
        this.zoneNumber = zoneNumber;
        this.zoneBounds = zoneBounds;

        UTM ll = UTM.from(new LatLng(zoneBounds.getMinLatitude(), zoneBounds.getMinLongitude()), zoneNumber, this.hemisphere);
        UTM lr = UTM.from(new LatLng(zoneBounds.getMinLatitude(), zoneBounds.getMaxLongitude()), zoneNumber, this.hemisphere);
        UTM ul = UTM.from(new LatLng(zoneBounds.getMaxLatitude(), zoneBounds.getMinLongitude()), zoneNumber, this.hemisphere);
        UTM ur = UTM.from(new LatLng(zoneBounds.getMaxLatitude(), zoneBounds.getMaxLongitude()), zoneNumber, this.hemisphere);
        this.zoneUtmBounds = new double[]{
                Math.min(ll.getEasting(), ul.getEasting()),
                Math.min(ll.getNorthing(), lr.getNorthing()),
                Math.max(lr.getEasting(), ur.getEasting()),
                Math.max(ul.getNorthing(), ur.getNorthing())
        };
        LatLng[] boundsLatLngs = {new LatLng(zoneBounds.getMinLatitude(), zoneBounds.getMinLongitude()),
                new LatLng(zoneBounds.getMinLatitude(), zoneBounds.getMaxLongitude()),
                new LatLng(zoneBounds.getMaxLatitude(), zoneBounds.getMaxLongitude()),
                new LatLng(zoneBounds.getMaxLatitude(), zoneBounds.getMinLongitude()),
                new LatLng(zoneBounds.getMinLatitude(), zoneBounds.getMinLongitude())};
        this.zonePolygon = this.generatePolygon(boundsLatLngs);
    }

    /**
     * Gets the zones label text.
     *
     * @return The zones label.
     */
    private String getLabelText() {
        return String.valueOf(zoneNumber) + this.zoneLetter;
    }

    /**
     * The center point of the zone.
     *
     * @return The center point.
     */
    private LatLng getCenter() {
        double centerLon = (this.zoneBounds.getMaxLongitude() + this.zoneBounds.getMinLongitude()) / 2.0;
        double centerLat = (this.zoneBounds.getMaxLatitude() + this.zoneBounds.getMinLatitude()) / 2.0;
        return new LatLng(centerLat, centerLon);
    }

    /**
     * The zone's letter.
     *
     * @return The zone's letter.
     */
    private String getZoneLetter() {
        return this.zoneLetter;
    }

    /**
     * The zone's number.
     *
     * @return The zone's number.
     */
    private int getZoneNumber() {
        return this.zoneNumber;
    }

    /**
     * The zone's bounds.
     *
     * @return The zone's bounds.
     */
    private BoundingBox getZoneBounds() {
        return this.zoneBounds;
    }

    /**
     * Indicates if the specified bounding box is within this zone's boundary.
     *
     * @param bbox The bounding box to check for containment.
     * @return True if the bbox is within, false otherwise.
     */
    private boolean within(BoundingBox bbox) {
        return zoneBounds.contains(bbox);
    }

    /**
     * Generates a polygon for the specified coordinates.
     *
     * @param latLngs The coordinates for the polygons.
     * @return The new polygon.
     */
    private Polygon generatePolygon(LatLng[] latLngs) {
        Coordinate[] coords = new Coordinate[latLngs.length];
        for (int i = 0; i < latLngs.length; i++) {
            coords[i] = new Coordinate(latLngs[i].longitude, latLngs[i].latitude);
        }

        GeometryFactory factory = new GeometryFactory();
        Polygon polygon = factory.createPolygon(coords);

        return polygon;
    }

    /**
     * Generates the polygon and label
     *
     * @param boundingBox The bounding box.
     * @param precision   The precision.
     * @param easting     The easting coordinate.
     * @param northing    The northing coordinate.
     * @param newEasting  The new easting coordinate.
     * @param newNorthing The new northing coordinate.
     * @param polygons    The list to add new grids to.
     */
    private void generatePolygonAndLabel(BoundingBox boundingBox, double precision, double easting, double northing, double newEasting, double newNorthing, List<Grid> polygons) {
        LatLng ll = new UTM(this.zoneNumber, this.hemisphere, easting, northing).toLatLng();
        LatLng ul = new UTM(this.zoneNumber, this.hemisphere, easting, newNorthing).toLatLng();
        LatLng ur = new UTM(this.zoneNumber, this.hemisphere, newEasting, newNorthing).toLatLng();
        LatLng lr = new UTM(this.zoneNumber, this.hemisphere, newEasting, northing).toLatLng();

        Polygon gridPoly = this.generatePolygon(new LatLng[]{ll, ul, ur, lr, ll});
        Geometry intersection = zonePolygon.intersection(gridPoly);
        if (!intersection.isEmpty() && intersection instanceof Polygon) {
            gridPoly = (Polygon) intersection;
            Grid newGrid = new Grid();
            newGrid.setBounds(gridPoly);
            polygons.add(newGrid);


            if (precision == 100000) {
                /*ll = new LatLng(intersection.getCoordinates()[0].getY(), intersection.getCoordinates()[0].getX());
                ul = new LatLng(intersection.getCoordinates()[1].getY(), intersection.getCoordinates()[1].getX());
                ur = new LatLng(intersection.getCoordinates()[2].getY(), intersection.getCoordinates()[2].getX());
                lr = new LatLng(intersection.getCoordinates()[3].getY(), intersection.getCoordinates()[3].getX());
                // determine center easting/northing given the bounds, then convert to lat/lng
                UTM utm1 = UTM.from(ll, this.zoneNumber, this.hemisphere);
                UTM utm2 = UTM.from(ul, this.zoneNumber, this.hemisphere);
                UTM utm3 = UTM.from(ur, this.zoneNumber, this.hemisphere);
                UTM utm4 = UTM.from(lr, this.zoneNumber, this.hemisphere);
                double minEasting = Math.min(utm1.getEasting(), utm2.getEasting());
                double maxEasting = Math.max(utm3.getEasting(), utm4.getEasting());
                double minNorthing = Math.min(utm1.getNorthing(), utm4.getNorthing());
                double maxNorthing = Math.max(utm2.getNorthing(), utm3.getNorthing());
                //const labelCenter = LatLng.from(new UTM(this.zoneNumber, this.hemisphere, minEasting + ((maxEasting - minEasting) / 2), minNorthing + ((maxNorthing - minNorthing) / 2)));

            /*const intersectionBounds = [
                    Math.min(ll.longitude, ul.longitude),
                            Math.min(ll.latitude, lr.latitude),
                            Math.max(lr.longitude, ur.longitude),
                            Math.max(ul.latitude, ur.latitude)
            ]*/
                String label = MGRS.get100KId(easting, northing, this.zoneNumber);
                newGrid.setText(label);
            }
        }
    }

    /**
     * Return zone grids at provided precision
     *
     * @param boundingBox The bounding box.
     * @param precision   The precision.
     * @return The list of grids for the zone.
     */
    public List<Grid> polygonsAndLabelsInBounds(BoundingBox boundingBox, double precision) {
        List<Grid> grids = new ArrayList<>();
        if (precision == 0) {
            Grid grid = new Grid();
            grid.setBounds(this.zonePolygon);
            grid.setText(getLabelText());
            grids.add(grid);
        } else {
            double minLat = Math.max(boundingBox.getMinLatitude(), this.zoneBounds.getMinLatitude());
            double maxLat = Math.min(boundingBox.getMaxLatitude(), this.zoneBounds.getMaxLatitude());
            double minLon = Math.max(boundingBox.getMinLongitude(), this.zoneBounds.getMinLongitude());
            double maxLon = Math.min(boundingBox.getMaxLongitude(), this.zoneBounds.getMaxLongitude());

            if (this.hemisphere.equals(UTM.HEMISPHERE_NORTH)) {
                UTM lowerLeftUTM = UTM.from(new LatLng(minLat, minLon), this.zoneNumber, this.hemisphere);
                double lowerLeftEasting = (Math.floor(lowerLeftUTM.getEasting() / precision) * precision);
                double lowerLeftNorthing = (Math.floor(lowerLeftUTM.getNorthing() / precision) * precision);

                UTM upperRightUTM = UTM.from(new LatLng(maxLat, maxLon), this.zoneNumber, this.hemisphere);
                double endEasting = (Math.ceil(upperRightUTM.getEasting() / precision) * precision);
                double endNorthing = (Math.ceil(upperRightUTM.getNorthing() / precision) * precision);

                double easting = lowerLeftEasting;
                while (easting <= endEasting) {
                    double newEasting = easting + precision;
                    double northing = lowerLeftNorthing;
                    while (northing <= endNorthing) {
                        double newNorthing = northing + precision;
                        this.generatePolygonAndLabel(boundingBox, precision, easting, northing, newEasting, newNorthing, grids);
                        northing = newNorthing;
                    }
                    easting = newEasting;
                }
            } else {
                UTM upperLeftUTM = UTM.from(new LatLng(maxLat, minLon), this.zoneNumber, this.hemisphere);
                double upperLeftEasting = (Math.floor(upperLeftUTM.getEasting() / precision) * precision);
                double upperLeftNorthing = (Math.ceil(upperLeftUTM.getNorthing() / precision + 1) * precision);
                if (this.zoneLetter.equals("M")) {
                    upperLeftNorthing = 10000000.0;
                    upperLeftUTM = new UTM(upperLeftUTM.getZoneNumber(), UTM.HEMISPHERE_SOUTH, upperLeftUTM.getEasting(), upperLeftUTM.getNorthing());
                }
                UTM lowerRightUTM = UTM.from(new LatLng(minLat, maxLon), this.zoneNumber, this.hemisphere);
                double lowerRightEasting = (Math.ceil(lowerRightUTM.getEasting() / precision) * precision);
                double lowerRightNorthing = (Math.floor(lowerRightUTM.getNorthing() / precision) * precision);
                for (double easting = upperLeftEasting; easting <= lowerRightEasting; easting += precision) {
                    double northing = upperLeftNorthing;
                    while (northing >= lowerRightNorthing) {
                        double newNorthing = northing - precision;
                        this.generatePolygonAndLabel(boundingBox, precision, easting, newNorthing, easting + precision, northing, grids);
                        northing = newNorthing;
                    }
                }
            }
        }

        return grids;
    }
}

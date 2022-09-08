package mil.nga.mapcache.utils;

import org.locationtech.proj4j.units.Units;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;

public class ProjUtils {

    private static ProjUtils instance = new ProjUtils();

    public static ProjUtils getInstance() {
        return instance;
    }

    /**
     * Transform the bounding box in the spatial reference to a WGS84 bounding box
     *
     * @param boundingBox bounding box
     * @param srs         spatial reference system
     * @return bounding box
     */
    public BoundingBox transformBoundingBoxToWgs84(BoundingBox boundingBox, SpatialReferenceSystem srs) {

        mil.nga.proj.Projection projection = srs.getProjection();
        if (projection.isUnit(Units.DEGREES)) {
            boundingBox = TileBoundingBoxUtils.boundDegreesBoundingBoxWithWebMercatorLimits(boundingBox);
        }
        ProjectionTransform transformToWebMercator = projection
                .getTransformation(
                        ProjectionConstants.EPSG_WEB_MERCATOR);
        BoundingBox webMercatorBoundingBox = boundingBox.transform(transformToWebMercator);
        ProjectionTransform transform = ProjectionFactory.getProjection(
                        ProjectionConstants.EPSG_WEB_MERCATOR)
                .getTransformation(
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        boundingBox = webMercatorBoundingBox.transform(transform);
        return boundingBox;
    }

    private ProjUtils() {

    }
}

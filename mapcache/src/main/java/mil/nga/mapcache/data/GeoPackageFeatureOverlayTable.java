package mil.nga.mapcache.data;

import java.io.Serializable;

import mil.nga.sf.GeometryType;

/**
 * GeoPackage Feature Overlay table information
 *
 * @author osbornb
 */
public class GeoPackageFeatureOverlayTable extends GeoPackageFeatureTable implements Serializable {

    /**
     * UID
     */
    private static final long serialVersionUID = 1;

    private String featureTable;

    private int minZoom;

    private int maxZoom;

    private Integer maxFeaturesPerTile;

    private double minLat;

    private double maxLat;

    private double minLon;

    private double maxLon;

    private String pointColor;

    private int pointAlpha;

    private float pointRadius;

    private String lineColor;

    private int lineAlpha;

    private float lineStrokeWidth;

    private String polygonColor;

    private int polygonAlpha;

    private float polygonStrokeWidth;

    private boolean polygonFill;

    private String polygonFillColor;

    private int polygonFillAlpha;

    /**
     * Create a new feature table
     *
     * @param database
     * @param name
     * @param featureTable
     * @param geometryType
     * @param count
     * @return
     */
    public GeoPackageFeatureOverlayTable(String database, String name, String featureTable,
                                         GeometryType geometryType, int count) {
        super(database, name, geometryType, count);
        this.featureTable = featureTable;
    }

    @Override
    public GeoPackageTableType getType() {
        return GeoPackageTableType.FEATURE_OVERLAY;
    }

    public String getFeatureTable() {
        return featureTable;
    }

    public void setFeatureTable(String featureTable) {
        this.featureTable = featureTable;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public Integer getMaxFeaturesPerTile() {
        return maxFeaturesPerTile;
    }

    public void setMaxFeaturesPerTile(Integer maxFeaturesPerTile) {
        this.maxFeaturesPerTile = maxFeaturesPerTile;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public void setMinLon(double minLon) {
        this.minLon = minLon;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(double maxLon) {
        this.maxLon = maxLon;
    }

    public String getPointColor() {
        return pointColor;
    }

    public void setPointColor(String pointColor) {
        this.pointColor = pointColor;
    }

    public int getPointAlpha() {
        return pointAlpha;
    }

    public void setPointAlpha(int pointAlpha) {
        this.pointAlpha = pointAlpha;
    }

    public float getPointRadius() {
        return pointRadius;
    }

    public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineAlpha() {
        return lineAlpha;
    }

    public void setLineAlpha(int lineAlpha) {
        this.lineAlpha = lineAlpha;
    }

    public float getLineStrokeWidth() {
        return lineStrokeWidth;
    }

    public void setLineStrokeWidth(float lineStrokeWidth) {
        this.lineStrokeWidth = lineStrokeWidth;
    }

    public String getPolygonColor() {
        return polygonColor;
    }

    public void setPolygonColor(String polygonColor) {
        this.polygonColor = polygonColor;
    }

    public int getPolygonAlpha() {
        return polygonAlpha;
    }

    public void setPolygonAlpha(int polygonAlpha) {
        this.polygonAlpha = polygonAlpha;
    }

    public float getPolygonStrokeWidth() {
        return polygonStrokeWidth;
    }

    public void setPolygonStrokeWidth(float polygonStrokeWidth) {
        this.polygonStrokeWidth = polygonStrokeWidth;
    }

    public boolean isPolygonFill() {
        return polygonFill;
    }

    public void setPolygonFill(boolean polygonFill) {
        this.polygonFill = polygonFill;
    }

    public String getPolygonFillColor() {
        return polygonFillColor;
    }

    public void setPolygonFillColor(String polygonFillColor) {
        this.polygonFillColor = polygonFillColor;
    }

    public int getPolygonFillAlpha() {
        return polygonFillAlpha;
    }

    public void setPolygonFillAlpha(int polygonFillAlpha) {
        this.polygonFillAlpha = polygonFillAlpha;
    }
}

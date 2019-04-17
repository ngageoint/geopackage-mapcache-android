package mil.nga.mapcache.view.detail;

/**
 * Holds values for the Header of the GeoPackage Detail View's recyclerview.  Contains: GeoPackage
 * name, size, number of feature layers, number of tile layers
 */
public class DetailPageHeaderObject {

    /**
     * GeoPackage name
     */
    private String geopackageName;

    /**
     * GeoPackage size (it comes as a string from the GP libraries)
     */
    private String size;

    /**
     * Number of feature layers
     */
    private int featureCount;

    /**
     * Number of tile layers
     */
    private int tileCount;


    /**
     * Constructor
     * @param gpName - GeoPackage Name
     * @param gpSize - GeoPackage size (as a string)
     */
    public DetailPageHeaderObject(String gpName, String gpSize, int gpFeatureCount, int gpTileCount){
        size = gpSize;
        geopackageName = gpName;
        featureCount = gpFeatureCount;
        tileCount = gpTileCount;
    }



    /**
     * Getters and setters
     */
    public String getGeopackageName() {
        return geopackageName;
    }

    public void setGeopackageName(String geopackageName) {
        this.geopackageName = geopackageName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }
}

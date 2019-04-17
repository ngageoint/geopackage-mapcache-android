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
     * Constructor
     * @param gpName - GeoPackage Name
     * @param gpSize - GeoPackage size (as a string)
     */
    public DetailPageHeaderObject(String gpName, String gpSize){
        size = gpSize;
        geopackageName = gpName;
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
}

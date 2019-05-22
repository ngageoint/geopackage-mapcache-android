package mil.nga.mapcache.view.detail;

import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageTable;

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
     * Are all layers of this gp set to active
     */
    private boolean allActive;


    /**
     * Constructor
     * @param gp - A GeoPackageDatabase object
     */
    public DetailPageHeaderObject(GeoPackageDatabase gp){
        size = gp.getSize();
        geopackageName = gp.getDatabase();
        featureCount = gp.getFeatureCount();
        tileCount = gp.getTileCount();
        allActive = gp.isEveryTableActive();
    }



    /**
     * Getters and setters
     */

    public boolean isAllActive() {
        return allActive;
    }

    public void setAllActive(boolean allActive) {
        this.allActive = allActive;
    }

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

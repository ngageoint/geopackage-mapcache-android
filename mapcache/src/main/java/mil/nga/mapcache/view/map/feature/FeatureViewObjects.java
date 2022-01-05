package mil.nga.mapcache.view.map.feature;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.GeometryType;

/**
 * Holds objects from a database object retrieved via the manager (allows us to not keep a geopackage
 * open within our context)
 */
public class FeatureViewObjects {
    private FeatureRow featureRow;
    private GeometryType geometryType;
    private DataColumnsDao dataColumnsDao;
    private HashMap<Long,Bitmap> bitmaps = new HashMap<>();
    // Save images that were added by the user after loading the menu separately
    private HashMap<Long,Bitmap> addedBitmaps = new HashMap<>();
    private boolean hasExtension;
    private String geopackageName;
    private String layerName;
    private List<FcColumnDataObject> fcObjects = new ArrayList<>();


    /**
     * Allows us to validate that this object has all fields set, meaning, when the geopackage was
     * opened, it was successfully able to find all needed fields
     * @return true if all fields were set
     */
    public boolean isValid(){
        if(featureRow == null || geometryType == null || geopackageName == null || layerName == null){
            return false;
        }
        return true;
    }

    public List<FcColumnDataObject> getFcObjects() {
        return fcObjects;
    }

    public void setFcObjects(List<FcColumnDataObject> fcObjects) {
        this.fcObjects = fcObjects;
    }

    public HashMap<Long,Bitmap> getAddedBitmaps() {
        return addedBitmaps;
    }

    public void setAddedBitmaps(HashMap<Long,Bitmap> addedBitmaps) {
        this.addedBitmaps = addedBitmaps;
    }

    public HashMap<Long,Bitmap> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(HashMap<Long,Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public FeatureRow getFeatureRow() {
        return featureRow;
    }

    public void setFeatureRow(FeatureRow featureRow) {
        this.featureRow = featureRow;
    }

    public GeometryType getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
    }

    public DataColumnsDao getDataColumnsDao() {
        return dataColumnsDao;
    }

    public void setDataColumnsDao(DataColumnsDao dataColumnsDao) {
        this.dataColumnsDao = dataColumnsDao;
    }

    public boolean isHasExtension() {
        return hasExtension;
    }

    public void setHasExtension(boolean hasExtension) {
        this.hasExtension = hasExtension;
    }

    public String getGeopackageName() {
        return geopackageName;
    }

    public void setGeopackageName(String geopackageName) {
        this.geopackageName = geopackageName;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}

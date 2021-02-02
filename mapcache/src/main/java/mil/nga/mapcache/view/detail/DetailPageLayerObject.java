package mil.nga.mapcache.view.detail;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;

/**
 * Holds data for a single layer belonging to a GeoPackage.  When the GeoPackage Detail View's recycler
 * view is created, it will make a row containing these objects in order to display the layer names,
 * layer type icon, and active/inactive switch
 */
public class DetailPageLayerObject {
    // Icon to show if it's a feature layer or tile layer
    private int iconType;
    // The name of this layer
    private String name;
    // checked value to coordinate with the active/inactive switch (turns the layer on or off)
    private boolean checked;
    // GeoPackage name
    private String geoPackageName;
    // GeoPackageTable object
    private GeoPackageTable table;
    // Description
    private String description;
    // Feature Columns
    private List<FeatureColumn> featureColumns = new ArrayList<>();

    /**
     * Constructor
     * @param layerName
     */
    public DetailPageLayerObject(String layerName, String geoPackageName, boolean checked, GeoPackageTable table){
        this.name = layerName;
        this.geoPackageName = geoPackageName;
        this.checked = checked;
        this.table = table;
        this.description = table.getDescription();
        if(table instanceof GeoPackageFeatureTable){
            this.iconType = R.drawable.polygon;
        } else{
            this.iconType = R.drawable.colored_layers;
        }
        if(table instanceof GeoPackageFeatureTable){
            GeoPackageFeatureTable featureTable = (GeoPackageFeatureTable)table;
            featureColumns = featureTable.getFeatureColumns();
        }
    }

    /**
     * Match based on GeoPackage name and Layer name
     */
    public boolean equals(String geoPackageName, String layerName){
        return getGeoPackageName().equalsIgnoreCase(geoPackageName) &&
           getName().equalsIgnoreCase(layerName);
    }

    /**
     * Getters and setters
     */

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconType() {
        return iconType;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getGeoPackageName() {
        return geoPackageName;
    }

    public void setGeoPackageName(String geoPackageName) {
        this.geoPackageName = geoPackageName;
    }

    public GeoPackageTable getTable() {
        return table;
    }

    public void setTable(GeoPackageTable table) {
        this.table = table;
    }

    public List<FeatureColumn> getFeatureColumns() {
        return featureColumns;
    }

    public void setFeatureColumns(List<FeatureColumn> featureColumns) {
        this.featureColumns = featureColumns;
    }
}

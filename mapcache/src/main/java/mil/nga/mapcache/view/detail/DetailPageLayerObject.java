package mil.nga.mapcache.view.detail;

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

    /**
     * Constructor
     * @param layerName
     */
    public DetailPageLayerObject(String layerName){
        name = layerName;
    }


    /**
     * Getters and setters
     */
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
}

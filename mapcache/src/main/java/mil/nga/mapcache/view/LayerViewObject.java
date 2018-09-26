package mil.nga.mapcache.view;

/**
 * Holder object for a GeoPackage Layer name and icon type (feature or tile icon)
 */

public class LayerViewObject {


    private int iconType;
    private String name;
    private boolean checked;

    public LayerViewObject(int icon, String givenName){
        iconType = icon;
        name = givenName;
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
}

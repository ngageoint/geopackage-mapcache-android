package mil.nga.mapcache.view.map.feature;

/**
 * Holds a string and object representing the name and value of a feature column
 */
public class FcColumnDataObject {
    // name
    private String mName;
    // value
    private Object mValue;

    public FcColumnDataObject(String name, Object value){
        this.mName = name;
        this.mValue = value;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public Object getmValue() {
        return mValue;
    }

    public void setmValue(Object mValue) {
        this.mValue = mValue;
    }
}

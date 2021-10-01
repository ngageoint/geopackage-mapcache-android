package mil.nga.mapcache.view.map.feature;

/**
 * Holds a string and object representing the name and value of a feature column
 */
public class FcColumnDataObject {
    // name
    private String mName;
    // value
    private Object mValue;
    // Track the original object type
    private Object originalValue;

    public FcColumnDataObject(String name, Object value){
        this.mName = name;
        this.mValue = value;
        this.originalValue = value;
    }

    /**
     * Return the format of the original object type to make sure we save the data in the correct format
     * @return Class type of the original object type
     */
    public Class getFormat(){
        if(originalValue instanceof Boolean) {
            return Boolean.class;
        } else if(originalValue instanceof Double) {
            return Double.class;
        } else if(originalValue instanceof String) {
            return String.class;
        } else {
            return String.class;
        }
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

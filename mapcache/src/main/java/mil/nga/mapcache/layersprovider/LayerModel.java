package mil.nga.mapcache.layersprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * A model that represents a single layer from a server.
 */
public class LayerModel extends Observable {

    /**
     * The name property.
     */
    public static String NAME_PROPERTY = "name";

    /**
     * The description property.
     */
    public static String DESCRIPTION_PROPERTY = "description";

    /**
     * The title property.
     */
    public static String TITLE_PROPERTY = "title";

    /**
     * The epsgs property.
     */
    public static String EPSGS_PROPERTY = "epsgs";

    /**
     * The name of the layer.
     */
    private String name;

    /**
     * The title of the layer.
     */
    private String title;

    /**
     * The description of the layer.
     */
    private String description;

    /**
     * The available coordinate systems for this layer.
     */
    private long[] epsgs;

    /**
     * Gets the name of the layer.
     *
     * @return The name of the layer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the layer.
     *
     * @param name The name of the layer.
     */
    public void setName(String name) {
        this.name = name;
        setChanged();
        notifyObservers(NAME_PROPERTY);
    }

    /**
     * Gets the description of the layer.
     *
     * @return The description of the layer.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the layer.
     *
     * @param description The description of the layer.
     */
    public void setDescription(String description) {
        this.description = description;
        setChanged();
        notifyObservers(DESCRIPTION_PROPERTY);
    }

    /**
     * Gets the title of the layer.
     *
     * @return The title of the layer.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the layer.
     *
     * @param title The title of the layer.
     */
    public void setTitle(String title) {
        this.title = title;
        setChanged();
        notifyObservers(TITLE_PROPERTY);
    }

    /**
     * Gets the available coordinate systems for this layer.
     *
     * @return The available coordinate systems for this layer.
     */
    public long[] getEpsgs() {
        return epsgs;
    }

    /**
     * Sets the available coordinate systems for this layer.
     *
     * @param epsgs The available coordinate systems for this layer.
     */
    public void setEpsgs(long[] epsgs) {
        this.epsgs = epsgs;
        setChanged();
        notifyObservers(EPSGS_PROPERTY);
    }
}

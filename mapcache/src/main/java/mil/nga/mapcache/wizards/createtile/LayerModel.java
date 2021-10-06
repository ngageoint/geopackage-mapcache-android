package mil.nga.mapcache.wizards.createtile;

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
     * The name of the layer.
     */
    private String name;

    /**
     * The description of the layer.
     */
    private String description;

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
        notifyObservers();
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
        notifyObservers();
    }
}

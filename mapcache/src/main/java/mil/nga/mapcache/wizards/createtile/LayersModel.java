package mil.nga.mapcache.wizards.createtile;

import java.util.Observable;

/**
 * The model that contains all the possible layers from a given server.
 */
public class LayersModel extends Observable {

    /**
     * The layers property.
     */
    public static String LAYERS_PROP = "layers";

    /**
     * The selected layer.
     */
    public static String SELECTED_LAYER = "selectedLayers";

    /**
     * The available layers from a server.
     */
    private LayerModel[] layers;

    /**
     * The selected layer.
     */
    private LayerModel selectedLayer;

    /**
     * Gets the available layers from a server.
     *
     * @return The available layers from a server.
     */
    public LayerModel[] getLayers() {
        return layers;
    }

    /**
     * Sets the available layers from a server.
     *
     * @param layers The available layers.
     */
    public void setLayers(LayerModel[] layers) {
        this.layers = layers;
        setChanged();
        notifyObservers(LAYERS_PROP);
    }

    /**
     * Gets the selected layer.
     *
     * @return The selected layer.
     */
    public LayerModel getSelectedLayer() {
        return selectedLayer;
    }

    /**
     * Sets the selected layer.
     *
     * @param selectedLayer The selected layer.
     */
    public void setSelectedLayer(LayerModel selectedLayer) {
        this.selectedLayer = selectedLayer;
        setChanged();
        notifyObservers(SELECTED_LAYER);
    }
}

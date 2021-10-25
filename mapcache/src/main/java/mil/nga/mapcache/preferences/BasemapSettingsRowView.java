package mil.nga.mapcache.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import mil.nga.mapcache.R;
import mil.nga.mapcache.layersprovider.LayersModel;

/**
 * Shows a single server that can be added as a basemap.
 */
public class BasemapSettingsRowView implements Observer {

    /**
     * The view.
     */
    private View rowView;

    /**
     * The server model.
     */
    private BasemapServerModel model;

    /**
     * Constructor.
     *
     * @param inflater The inflater.
     * @param server   The server.
     */
    public BasemapSettingsRowView(LayoutInflater inflater, BasemapServerModel server) {
        rowView = inflater.inflate(R.layout.layer_row_layout, null, true);
        model = server;
        updateText();
        model.getLayers().addObserver(this);
    }

    /**
     * Gets the view.
     *
     * @return The view.
     */
    public View getView() {
        return rowView;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (LayersModel.LAYERS_PROP.equals(o)) {
            updateText();
        }
    }

    /**
     * Updates the server text to include the number of layers.
     */
    private void updateText() {
        String countText = System.lineSeparator() + "1 Layer";
        if (model.getLayers().getLayers() != null && model.getLayers().getLayers().length > 1) {
            countText = System.lineSeparator() + model.getLayers().getLayers().length + " Layers";
        }
        TextView textView = rowView.findViewById(R.id.layer_label);
        textView.setText(model.getServerUrl() + countText);
    }
}

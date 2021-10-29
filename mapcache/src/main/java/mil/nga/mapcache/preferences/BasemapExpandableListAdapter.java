package mil.nga.mapcache.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import mil.nga.mapcache.R;
import mil.nga.mapcache.layersprovider.LayerModel;
import mil.nga.mapcache.layersprovider.LayersModel;

/**
 * An expandable list adapter to show servers and their layers.
 */
public class BasemapExpandableListAdapter extends BaseExpandableListAdapter implements Observer {

    /**
     * LayoutInflater.
     */
    private LayoutInflater inflater;

    /**
     * Contains the servers and their layers.
     */
    private BasemapSettingsModel model;

    /**
     * Constructor.
     *
     * @param inflater The layout inflater.
     * @param model    Contains the servers and their layers.
     */
    public BasemapExpandableListAdapter(LayoutInflater inflater, BasemapSettingsModel model) {
        this.inflater = inflater;
        this.model = model;
    }

    @Override
    public int getGroupCount() {
        return this.model.getAvailableServers().length;
    }

    @Override
    public int getChildrenCount(int i) {
        int childCount = 0;
        LayersModel layers = this.model.getAvailableServers()[i].getLayers();
        if (layers.getLayers() != null) {
            childCount = layers.getLayers().length;
        }
        return childCount;
    }

    @Override
    public Object getGroup(int i) {
        return this.model.getAvailableServers()[i];
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.model.getAvailableServers()[i].getLayers().getLayers()[i1];
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.layer_row_layout, null, true);
        }
        String countText = System.lineSeparator() + "1 Layer";
        BasemapServerModel serverModel = model.getAvailableServers()[i];
        serverModel.getLayers().addObserver(this);

        Switch simpleSwitch = (Switch) view.findViewById(R.id.simpleSwitch);
        if (serverModel.getLayers().getLayers() != null
                && serverModel.getLayers().getLayers().length > 1) {
            countText = System.lineSeparator() + serverModel.getLayers().getLayers().length + " Layers";
            simpleSwitch.setVisibility(View.GONE);
        } else {
            simpleSwitch.setVisibility(View.VISIBLE);
            simpleSwitch.setOnCheckedChangeListener(null);
            simpleSwitch.setChecked(false);
            for (BasemapServerModel server : model.getSelectedBasemap()) {
                if (serverModel.getServerUrl().equals(server.getServerUrl())) {
                    simpleSwitch.setChecked(true);
                    break;
                }
            }
            simpleSwitch.setOnCheckedChangeListener((compoundButton, isChecked)
                    -> serverSwitchChanged(compoundButton, isChecked, serverModel));
        }

        TextView textView = view.findViewById(R.id.layer_label);
        textView.setText(serverModel.getServerUrl() + countText);
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.layer_row_description, null, true);
        }

        BasemapServerModel server = model.getAvailableServers()[i];
        LayerModel layer = model.getAvailableServers()[i].getLayers().getLayers()[i1];
        TextView txtTitle = (TextView) view.findViewById(R.id.title);
        TextView txtDescription = (TextView) view.findViewById(R.id.description);
        txtTitle.setText(layer.getTitle());
        txtDescription.setText(layer.getDescription());

        Switch simpleSwitch = (Switch) view.findViewById(R.id.simpleSwitch);
        simpleSwitch.setVisibility(View.VISIBLE);
        simpleSwitch.setOnCheckedChangeListener(null);
        simpleSwitch.setChecked(false);
        for (BasemapServerModel selectedServer : model.getSelectedBasemap()) {
            if (server.getServerUrl().equals(selectedServer.getServerUrl())) {
                for (LayerModel selectedLayer : selectedServer.getLayers().getSelectedLayers()) {
                    if (layer.getName().equals(selectedLayer.getName())) {
                        simpleSwitch.setChecked(true);
                        break;
                    }
                }
            }

            if (simpleSwitch.isChecked()) {
                break;
            }
        }

        simpleSwitch.setOnCheckedChangeListener(
                (compoundButton, isChecked) ->
                        layerSwitchChanged(compoundButton, isChecked, server, layer));

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (LayersModel.LAYERS_PROP.equals(o)) {
            notifyDataSetChanged();
        }
    }

    /**
     * Called when a server with only one layer is enabled or disabled and removes it from the
     * selected list within the model.
     *
     * @param buttonView The switch button.
     * @param isChecked  True if enabled, false if disabled.
     * @param server     The server the layer belongs to.
     */
    private void serverSwitchChanged(CompoundButton buttonView, boolean isChecked,
                                     BasemapServerModel server) {
        BasemapServerModel[] selectedServers = model.getSelectedBasemap();
        BasemapServerModel[] newSelectedServers;
        if (isChecked) {
            newSelectedServers = Arrays.copyOf(selectedServers, selectedServers.length + 1);
            newSelectedServers[newSelectedServers.length - 1] = server;
        } else {
            newSelectedServers = new BasemapServerModel[selectedServers.length - 1];
            int index = 0;
            for (BasemapServerModel selectedServer : selectedServers) {
                if (!selectedServer.getServerUrl().equals(server.getServerUrl())) {
                    newSelectedServers[index] = selectedServer;
                    index++;
                }
            }
        }

        model.setSelectedBasemap(newSelectedServers);
    }

    /**
     * Called when a layer is enabled or disabled and removes it from the selected list within
     * the model.
     *
     * @param buttonView The switch button.
     * @param isChecked  True if enabled, false if disabled.
     * @param theServer  The server the layer belongs to.
     * @param layer      The layer that has either been enabled or disabled.
     */
    private void layerSwitchChanged(CompoundButton buttonView, boolean isChecked,
                                    BasemapServerModel theServer, LayerModel layer) {
        LayerModel[] selectedLayers = null;
        BasemapServerModel server = null;
        for (BasemapServerModel selectedServer : model.getSelectedBasemap()) {
            if (selectedServer.getServerUrl().equals(theServer.getServerUrl())) {
                server = selectedServer;
                selectedLayers = selectedServer.getLayers().getSelectedLayers();
                break;
            }
        }

        if (selectedLayers != null) {
            if (isChecked && selectedLayers != null && selectedLayers.length > 0) {
                LayerModel[] newSelectedLayers = Arrays.copyOf(selectedLayers, selectedLayers.length + 1);
                newSelectedLayers[newSelectedLayers.length - 1] = layer;
                server.getLayers().setSelectedLayers(newSelectedLayers);
            } else if (selectedLayers.length > 1) {
                int index = 0;
                LayerModel[] newSelectedLayers = new LayerModel[selectedLayers.length - 1];
                for (LayerModel selected : selectedLayers) {
                    if (!selected.getName().equals(layer.getName())) {
                        newSelectedLayers[index] = selected;
                        index++;
                    }
                }

                server.getLayers().setSelectedLayers(newSelectedLayers);
            } else {
                server.getLayers().setSelectedLayers(null);
                BasemapServerModel[] selectedServers = model.getSelectedBasemap();
                List<BasemapServerModel> newSelectedServers = new ArrayList<>();
                int index = 0;
                for (BasemapServerModel selectedServer : selectedServers) {
                    if (!selectedServer.getServerUrl().equals(server.getServerUrl())) {
                        newSelectedServers.add(selectedServer);
                        index++;
                    }
                }

                model.setSelectedBasemap(newSelectedServers.toArray(new BasemapServerModel[0]));
            }
        } else if (isChecked) {
            LayerModel[] newSelectedLayers = new LayerModel[1];
            newSelectedLayers[0] = layer;
            theServer.getLayers().setSelectedLayers(newSelectedLayers);
            BasemapServerModel[] selectedServers = model.getSelectedBasemap();
            BasemapServerModel[] newSelectedServers = Arrays.copyOf(selectedServers, selectedServers.length + 1);
            newSelectedServers[newSelectedServers.length - 1] = theServer;
            model.setSelectedBasemap(newSelectedServers);
        }
    }
}

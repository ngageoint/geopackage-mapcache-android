package mil.nga.mapcache.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

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
        if (serverModel.getLayers().getLayers() != null
                && serverModel.getLayers().getLayers().length > 1) {
            countText = System.lineSeparator() + serverModel.getLayers().getLayers().length + " Layers";
            View simpleSwitch = view.findViewById(R.id.simpleSwitch);
            simpleSwitch.setVisibility(View.GONE);
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

        LayerModel layer = model.getAvailableServers()[i].getLayers().getLayers()[i1];
        TextView txtTitle = (TextView) view.findViewById(R.id.title);
        TextView txtDescription = (TextView) view.findViewById(R.id.description);
        txtTitle.setText(layer.getTitle());
        txtDescription.setText(layer.getDescription());

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
}

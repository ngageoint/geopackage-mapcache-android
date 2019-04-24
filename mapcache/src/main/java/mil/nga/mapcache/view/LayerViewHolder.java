package mil.nga.mapcache.view;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.DetailLayerClickListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.listeners.RecyclerViewClickListener;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 *  ViewHolder to show a GeoPackage layer name and icon corresponding to the layer type
 */

public class LayerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    /**
     * Layer name
     */
    TextView title;

    /**
     * Icon to represent the layer as either a feature or tile layer
     */
    ImageView icon;

    /**
     * On/Off switch for setting the layer to active
     */
    Switch layerOn;

    /**
     * Store the name of the geoPackage
     */
    private String geoPackageName;

    /**
     * Object containing details about the layer (Icon type, Name, Checked state)
     */
    private DetailPageLayerObject layerObject;

    /**
     * Click listener for clicking on the layer and opening a Layer view
     */
    private DetailLayerClickListener mListener;

    /**
     * Click listener to be attached to the layer switch
     */
    private LayerActiveSwitchListener mSwitchListener;

    /**
     * Tells this ViewHolder if it should ignore state changes on the switch (used for setting
     * the switch state)
     */
    private boolean ignoreStateChange;

    /**
     * Constructor
     * @param itemView View being created
     * @param listener DetailLayerClickListener for clicking on a layer and opening a layer detail view
     * @param switchListener Listener for clicking an active switch
     */
    public LayerViewHolder(View itemView, DetailLayerClickListener listener, LayerActiveSwitchListener switchListener){
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.layer_label);
        icon = (ImageView) itemView.findViewById(R.id.layer_icon);
        layerOn = (Switch) itemView.findViewById(R.id.simpleSwitch);

        mSwitchListener = switchListener;
        mListener = listener;
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
        setLayerSwitchListener();
    }

    /**
     * Take data from a DetailPageLayerObject and populate this view
     * @param data Should be of type DetailPageLayerObject
     */
    public void setData(Object data){
        if(data instanceof DetailPageLayerObject){
            this.layerObject = (DetailPageLayerObject)data;
            this.icon.setImageResource(this.layerObject.getIconType());
            this.title.setText(layerObject.getName());
            this.geoPackageName = layerObject.getGeoPackageName();
            setCheckedStatus(layerObject.isChecked());
        }
    }

    /**
     * Set the layerOn's checked state without kicking off the state change listener
     * @param checked
     */
    private void setCheckedStatus(boolean checked){
        setIgnoreStateChange(true);
        layerOn.setChecked(checked);
        setIgnoreStateChange(false);
    }

    /**
     * Set ignore state to tell the active switch listener to ignore a state change.  Used to set
     * the active status without triggering a live data update
     * @param ignore
     */
    public void setIgnoreStateChange(boolean ignore){
        ignoreStateChange = ignore;
    }

    /**
     * Listener to return info back to the view when this layer is switched to active/inactive
     */
    private void setLayerSwitchListener(){
        layerOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean switchState) {
                if(!ignoreStateChange) {
                    layerObject.setChecked(switchState);
                    layerObject.getTable().setActive(switchState);
                    mSwitchListener.onClick(switchState, layerObject.getTable());
                }
            }
        });
    }

    /**
     * Click listener for clicking on this layer row
     * @param view Clicked view
     */
    @Override
    public void onClick(View view) {
        mListener.onClick(layerObject);
    }
}

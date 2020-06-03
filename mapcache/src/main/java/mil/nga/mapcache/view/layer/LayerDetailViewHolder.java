package mil.nga.mapcache.view.layer;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.listeners.DetailActionListener;
import mil.nga.mapcache.listeners.LayerActiveSwitchListener;
import mil.nga.mapcache.utils.ViewAnimation;
import mil.nga.mapcache.view.detail.DetailPageLayerObject;

/**
 * View holder for the Layer Detail page
 */
public class LayerDetailViewHolder extends RecyclerView.ViewHolder{

    /**
     * Layer name text view
     */
    private TextView nameText;

    /**
     * Back button to take the RecyclerView back to the GeoPackage Detail page
     */
    private ImageButton backArrow;

    /**
     * Layer type text - either feature or tile
     */
    private TextView layerTypeText;

    /**
     * Icon showing either feature or tile layer type
     */
    private ImageView layerTypeIcon;

    /**
     * Text label above the feature/tile count
     */
    private TextView layerCountLbl;

    /**
     * Text to display the feature/tile count
     */
    private TextView layerCountText;

    /**
     * active/inactive switch
     */
    private Switch layerOn;

    /**
     * Description
     */
    private TextView descriptionText;

    /**
     * Min zoom label
     */
    private TextView zoomLabel;

    /**
     * Min zoom text
     */
    private TextView zoomText;

    /**
     * Text button for deleting the layer
     */
    private TextView mLayerDelete;

    /**
     * Text button for renaming a layer
     */
    private Button mLayerRename;

    /**
     * Text button for copying a layer
     */
    private Button mLayerCopy;

    /**
     * Text button for opening this layer in edit mode
     */
    private Button mLayerEdit;

    /**
     * DetailPageLayerObject containing details for the selected layer
     */
    private DetailPageLayerObject mLayerObject;

    /**
     * Click listener to be attached to the layer switch
     */
    private LayerActiveSwitchListener mSwitchListener;

    /**
     * Click listener for the delete button
     */
    private DetailActionListener mDetailActionListener;

    /**
     * Click listener for the rename layer button
     */
    private DetailActionListener mRenameLayerListener;

    /**
     * Click listener for the copy button
     */
    private DetailActionListener mCopyLayerListener;

    /**
     * Click listener for the edit features button
     */
    private DetailActionListener mEditFeaturesListener;

    /**
     * Tells this ViewHolder if it should ignore state changes on the switch (used for setting
     * the switch state)
     */
    private boolean ignoreStateChange;


    /**
     * Constructor
     * @param view view to inflate
     */
    public LayerDetailViewHolder(View view, View.OnClickListener backListener,
                                 LayerActiveSwitchListener activeLayerListener,
                                 DetailActionListener detailActionListener,
                                 DetailActionListener renameLayerListener,
                                 DetailActionListener copyLayerListener,
                                 DetailActionListener editFeaturesListener){
        super(view);
        nameText = (TextView) view.findViewById(R.id.layerName);
        backArrow = (ImageButton) itemView.findViewById(R.id.layerPageBackButton);
        backArrow.setOnClickListener(backListener);
        layerCountText = (TextView) view.findViewById(R.id.layerTileCount);
        layerTypeText = (TextView) view.findViewById(R.id.layerType);
        layerCountLbl = (TextView) view.findViewById(R.id.layerTileCountLbl);
        layerTypeIcon = (ImageView) view.findViewById(R.id.layer_type_icon);
        descriptionText = (TextView) view.findViewById(R.id.text_description);
        layerOn = (Switch) view.findViewById(R.id.enableSwitch);
        mLayerDelete = view.findViewById(R.id.layerDeleteButton);
        mLayerRename = view.findViewById(R.id.layerRenameButton);
        mLayerCopy = view.findViewById(R.id.layerCopyButton);
        mLayerEdit = view.findViewById(R.id.editFeaturesButton);
        zoomLabel = view.findViewById(R.id.layerZoomLabel);
        zoomText = view.findViewById(R.id.textMinZoom);
        mSwitchListener = activeLayerListener;
        mDetailActionListener = detailActionListener;
        mRenameLayerListener = renameLayerListener;
        mCopyLayerListener = copyLayerListener;
        mEditFeaturesListener = editFeaturesListener;
        setDeleteListener();
        setRenameListener();
        setCopyListener();
        setEditFeaturesListener();
        setLayerSwitchListener();
        ViewAnimation.fadeInFromRight(itemView, 200);

    }

    /**
     * Sets the view to hold data given in the LayerDetail object
     */
    public void setData(DetailPageLayerObject layerObject){
        mLayerObject = layerObject;
        nameText.setText(mLayerObject.getName());
        if(mLayerObject.getDescription() != null && !mLayerObject.getDescription().isEmpty()) {
            descriptionText.setText(mLayerObject.getDescription());
        }
        setCheckedStatus(layerObject.isChecked());
        if(layerObject.getTable() instanceof GeoPackageFeatureTable){
            GeoPackageFeatureTable feature = (GeoPackageFeatureTable)layerObject.getTable();
            layerTypeText.setText("Feature Layer");
            layerCountLbl.setText("Features");
            layerTypeIcon.setImageResource(R.drawable.polygon);
            layerCountText.setText(feature.getCount() + "");
        } else if(layerObject.getTable() instanceof GeoPackageTileTable){
            GeoPackageTileTable tile = (GeoPackageTileTable)layerObject.getTable();
            layerTypeText.setText("Tile Layer");
            layerCountLbl.setText("Tiles");
            layerTypeIcon.setImageResource(R.drawable.colored_layers);
            layerCountText.setText(tile.getCount() + "");
            if(tile.getMinZoom() >= 0 && tile.getMaxZoom() >= 0) {
                zoomLabel.setVisibility(View.VISIBLE);
                zoomText.setVisibility(View.VISIBLE);
                zoomText.setText(tile.getMinZoom() + "-" + tile.getMaxZoom());
            }
        }
    }

    /**
     * Click listener for the delete button
     */
    private void setDeleteListener(){
        mLayerDelete.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mDetailActionListener.onClick(view, DetailActionListener.DELETE_LAYER, mLayerObject.getGeoPackageName(), mLayerObject.getName());
            }
        });
    }

    /**
     * Click listener for the rename layer button
     */
    private void setRenameListener(){
        mLayerRename.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mRenameLayerListener.onClick(view, DetailActionListener.RENAME_LAYER, mLayerObject.getGeoPackageName(), mLayerObject.getName());
            }
        });
    }

    /**
     * Click listener for the copy layer button
     */
    private void setCopyListener(){
        mLayerCopy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCopyLayerListener.onClick(view, DetailActionListener.COPY_LAYER, mLayerObject.getGeoPackageName(), mLayerObject.getName());
            }
        });
    }

    /**
     * Click listener for the edit features button
     */
    private void setEditFeaturesListener(){
        mLayerEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mEditFeaturesListener.onClick(view, DetailActionListener.EDIT_FEATURES, mLayerObject.getGeoPackageName(), mLayerObject.getName());
            }
        });
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
                    mLayerObject.setChecked(switchState);
                    mLayerObject.getTable().setActive(switchState);
                    mSwitchListener.onClick(switchState, mLayerObject.getTable());
                }
            }
        });
    }


}

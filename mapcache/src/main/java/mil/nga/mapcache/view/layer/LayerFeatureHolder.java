package mil.nga.mapcache.view.layer;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.FeatureColumnListener;

class LayerFeatureHolder extends RecyclerView.ViewHolder {

    /**
     * Icon showing the column data type
     */
    private final ImageView columnTypeIcon;

    /**
     * Column name text view
     */
    private final TextView nameText;

    /**
     * Column type text view
     */
    private final TextView typeText;


    /**
     * Saved instance of the FeatureColumnDetailObject
     */
    private FeatureColumnDetailObject columnDetailObject;

    /**
     * Delete button
     */
    private final ImageView deleteIcon;

    /**
     * Click listener for the delete feature column function
     */
    private final FeatureColumnListener featureColumnListener;

    /**
     * Constructor
     * @param view - the view to be inflated
     */
    public LayerFeatureHolder(@NonNull View view, FeatureColumnListener detailActionListener) {
        super(view);
        featureColumnListener = detailActionListener;
        nameText = (TextView) view.findViewById(R.id.column_name);
        typeText = (TextView) view.findViewById(R.id.column_type);
        columnTypeIcon = (ImageView) view.findViewById(R.id.column_icon);
        deleteIcon = (ImageView) view.findViewById(R.id.delete_feature_column);
    }

    public void setData(Object dataObject) {
        if(dataObject instanceof FeatureColumnDetailObject){
            columnDetailObject = (FeatureColumnDetailObject) dataObject;
            nameText.setText(columnDetailObject.getName());
            setTypeInfo(columnDetailObject.getColumnType());
            setDeleteListener();
        }
    }

    /**
     * Listener for delete button
     */
    private void setDeleteListener(){
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                featureColumnListener.onClick(view, FeatureColumnListener.DELETE_FEATURE_COLUMN,
                        columnDetailObject);
            }
        });
    }

    /**
     * Set the corresponding icon and text label for the column type enum field
     */
    private void setTypeInfo(GeoPackageDataType geometryType){
        switch(geometryType){
            case BOOLEAN:
                columnTypeIcon.setImageResource(R.drawable.ic_twotone_toggle_on_24);
                typeText.setText(R.string.fc_label_checkbox);
                break;
            case TEXT:
                columnTypeIcon.setImageResource(R.drawable.ic_baseline_text_format_24);
                typeText.setText(R.string.fc_label_text);
                break;
            case TINYINT:
            case SMALLINT:
            case MEDIUMINT:
            case INT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case REAL:
                columnTypeIcon.setImageResource(R.drawable.ic_baseline_dialpad_24);
                typeText.setText(R.string.fc_label_number);
                break;
            default:
                break;
        }
    }
}
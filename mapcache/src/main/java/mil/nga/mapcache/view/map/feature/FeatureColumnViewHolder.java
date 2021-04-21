package mil.nga.mapcache.view.map.feature;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import mil.nga.mapcache.R;

/**
 * Holds a row in the recyclerview of the feature column list when a user clicks on a feature
 */
class FeatureColumnViewHolder extends RecyclerView.ViewHolder {

    private TextView name;

    private TextInputEditText valueText;

    private TextInputLayout valueLabel;

    public FeatureColumnViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.fc_name_label);
        name.setVisibility(View.GONE);
        valueText = (TextInputEditText) itemView.findViewById(R.id.fc_value);
        valueLabel = (TextInputLayout) itemView.findViewById(R.id.fc_value_holder);
    }

    /**
     * Set the view from the given feature column objects
     *
     * @param data FcColumnDataObject with name and value
     */
    public void setData(FcColumnDataObject data) {
        name.setText(data.getmName());
        valueText.setText(data.getmValue().toString());
        valueLabel.setHint(data.getmName());
    }
}

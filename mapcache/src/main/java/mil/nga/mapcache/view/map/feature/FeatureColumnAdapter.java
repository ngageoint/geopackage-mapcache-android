package mil.nga.mapcache.view.map.feature;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import mil.nga.mapcache.R;

/**
 *  Inflate a recyclerview based on a list of FeatureColumns from a feature.  This is embedded in the
 *  PointView popup window when a user clicks on a feature on the map
 */
class FeatureColumnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * List of FeatureColumn objects associated with a single feature
     */
    private List<FcColumnDataObject> mItems;

    /**
     * Context
     */
    private final Context context;

    /**
     * Constructor
     */
    public FeatureColumnAdapter(Context context){
        mItems = new ArrayList<FcColumnDataObject>();
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fcView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_column_row_layout, parent, false);
        return new FeatureColumnViewHolder(fcView, new EditTextListener(), new EditSwitchListener());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof FeatureColumnViewHolder){
            FeatureColumnViewHolder fcHolder = (FeatureColumnViewHolder)holder;
            fcHolder.setData(mItems.get(position), position);
            fcHolder.editTextListener.setPosition(position);
            fcHolder.switchListener.setPosition(position);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<FcColumnDataObject> getmItems(){
        return mItems;
    }

    public void setData(List<FcColumnDataObject> fcObjects){
        if(mItems !=  null){
            mItems.clear();
            mItems.addAll(fcObjects);
            notifyDataSetChanged();
        } else{
            mItems = fcObjects;
        }
    }

    // Listener for text changes
    private class EditTextListener implements TextWatcher {
        private int position;
        public void setPosition(int position) {
            this.position = position;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mItems.get(position).setmValue(charSequence.toString());
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    // Listener for checkbox changes
    private class EditSwitchListener implements CompoundButton.OnCheckedChangeListener{
        private int position;
        public void setPosition(int position) {
            this.position = position;
        }
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean newCheckedValue) {
            mItems.get(position).setmValue(newCheckedValue);
        }
    }


    class FeatureColumnViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText valueText;
        private final TextInputLayout valueLabel;
        private final TextView checkboxLabel;
        private final SwitchCompat checkboxSwitch;
        public EditTextListener editTextListener;
        public EditSwitchListener switchListener;

        public FeatureColumnViewHolder(View itemView, EditTextListener textListener,
                                       EditSwitchListener switchListener) {
            super(itemView);
            valueText = itemView.findViewById(R.id.fc_value);
            valueLabel = itemView.findViewById(R.id.fc_value_holder);
            checkboxLabel = itemView.findViewById(R.id.fc_check_label);
            checkboxSwitch = itemView.findViewById(R.id.fc_switch);
            this.editTextListener = textListener;
            this.switchListener = switchListener;

            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            valueText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean isFocused) {
                    if (isFocused) {
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            });
        }

        /**
         * Set the view from the given feature column objects
         *
         * @param data FcColumnDataObject with name and value
         */
        public void setData(FcColumnDataObject data, int newPosition) {
            editTextListener.setPosition(newPosition);
            switchListener.setPosition(newPosition);
            valueText.addTextChangedListener(editTextListener);
            checkboxSwitch.setOnCheckedChangeListener(switchListener);
            if(data.getmValue() instanceof Boolean){
                toggleCheckboxVisibility(true);
                checkboxLabel.setText(data.getmName());
                checkboxSwitch.setChecked((Boolean)data.getmValue());
            } else{
                toggleCheckboxVisibility(false);
                valueText.setText(data.getmValue().toString());
                valueLabel.setHint(data.getmName());
                if(data.getmName().equalsIgnoreCase("id")){
                    valueText.setEnabled(false);
                } else {
                    valueText.setEnabled(true);
                }
            }
        }

        /**
         * If this row is a boolean, show it as a checkbox instead of a text field
         * @param showCheck - the field is a boolean value
         */
        private void toggleCheckboxVisibility(boolean showCheck){
            if(showCheck) {
                checkboxLabel.setVisibility(View.VISIBLE);
                checkboxSwitch.setVisibility(View.VISIBLE);
                valueLabel.setVisibility(View.GONE);
            } else{
                checkboxLabel.setVisibility(View.GONE);
                checkboxSwitch.setVisibility(View.GONE);
                valueLabel.setVisibility(View.VISIBLE);
            }
        }
    }
}

package mil.nga.mapcache.preferences;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.ViewAnimation;

/**
 *  Fragment giving the user a way to modify a saved list of URLs, which will be used in the create
 *  tile layer wizard
 */
public class TileUrlFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    /**
     * Parent View
     */
    private View urlView;
    /**
     * URL input text
     */
    private EditText inputText;
    /**
     * Selected URL label
     */
    private TextView selectedLabel;
    /**
     * Shared preferences
     */
    private SharedPreferences prefs;
//    /**
//     * Clear all URLs
//     */
//    private Button clearButton;
    /**
     * Add new URL from the inputText field to the shared prefs
     */
    private Button addButton;
    /**
     * Layout to hold a reference to all saved URLs
     */
    private LinearLayout labelHolder;
    /**
     * Delete selected text
     */
//    private TextView deleteSelected;
    /**
     * Edit mode button
     */
    private TextView editModeText;
    /**
     * Tracks the state of delete visibility
     */
    private boolean editMode = false;

    /**
     * Create the parent view and set up listeners
     * @param inflater Layout inflator
     * @param container Main container
     * @param savedInstanceState Saved instance state
     * @return Parent view for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        urlView = inflater.inflate(R.layout.fragment_saved_tile_urls, container, false);
        inputText = urlView.findViewById(R.id.new_url);
//        clearButton = urlView.findViewById(R.id.clear_all);
        addButton = urlView.findViewById(R.id.add_url);
        addButton.setEnabled(false);
        labelHolder = urlView.findViewById(R.id.item_list_layout);
//        deleteSelected = urlView.findViewById(R.id.delete_selected_urls);
        editModeText = urlView.findViewById(R.id.edit_mode_label);
        selectedLabel = urlView.findViewById(R.id.selected_urls_label);
        prefs = getPreferenceManager().getSharedPreferences();
        setButtonListeners();
        setInitialPrefLabels();
        return urlView;
    }

    /**
     * Gets the shared preferences setting for the current list
     * @param defaultVal new list if null
     * @return Set of type String
     */
    private Set<String> getStringSet(Set<String> defaultVal) {
        return this.prefs.getStringSet(getString(R.string.geopackage_create_tiles_label), defaultVal);
    }

    /**
     * Save the given set to saved preferences
     * @param set set of strings to save
     * @return true after the commit is saved
     */
    private boolean saveSet(Set<String> set){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(getString(R.string.geopackage_create_tiles_label), set);
        return editor.commit();
    }

    /**
     * Adds a String to the given set by making a copy of it, then saving to shared preferences
     * @param originalSet original shared preference string set
     * @param newUrl new url to add
     * @return true after the commit is saved
     */
    private boolean addStringToSet(Set<String> originalSet, String newUrl){
        HashSet<String> prefList = new HashSet<>(originalSet);
        if(prefList.contains(newUrl)){
            Toast.makeText(getActivity(), "URL already exists", Toast.LENGTH_SHORT).show();
            return false;
        }
        prefList.add(newUrl);
        return saveSet(prefList);
    }

    /**
     * Gets the current shared prefs, copies it (leaving out the given string), then saves to shared prefs
     * @param remove string to remove
     * @return true once commit is finished
     */
    private boolean removeStringFromSet(String remove){
        Set<String> existing = getStringSet(new HashSet<String>());
        HashSet<String> prefList = new HashSet<>();
        if(existing.size() > 0){
            for(String str : existing){
                if(!str.equalsIgnoreCase(remove)){
                    prefList.add(str);
                }
            }
            return saveSet(prefList);
        }
        return false;
    }

    /**
     * Adds all the URLs from the loaded preferenes as labels in the view
     */
    private void setInitialPrefLabels(){
        Set<String> existing = getStringSet(new HashSet<String>());
        if(existing.size() > 0){
            for(String str : existing){
                addUrlView(str);
            }
        }
    }

    /**
     * Set button listeners for the Add Url button and Clear all button
     */
    private void setButtonListeners(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUrl = inputText.getText().toString();
                Set<String> existing = getStringSet(new HashSet<String>());
                boolean saved = addStringToSet(existing, newUrl);
                if(saved){
                    if(editMode){
                        showEditButtons();
                    }
                    addUrlView(newUrl);
                }
            }
        });
//        deleteSelected.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Keep track of rows that need to be deleted
//                HashMap<String, LinearLayout> deleteViews = new HashMap<>();
//                // Find rows that are checked and note the row number
//                for (int i = 0; i < labelHolder.getChildCount(); i++) {
//                    LinearLayout itemRow = (LinearLayout)labelHolder.getChildAt(i);
//                    if(isItemRowChecked(itemRow)){
//                        deleteViews.put(getRowText(itemRow), itemRow);
//                    }
//                }
//                // Delete all checked rows
//                Iterator it = deleteViews.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry pair = (Map.Entry) it.next();
//                    if(removeStringFromSet(pair.getKey().toString())){
////                        labelHolder.removeView((LinearLayout)pair.getValue());
//                        labelHolder = (LinearLayout)ViewAnimation.fadeOutAndRemove((LinearLayout)pair.getValue(), labelHolder, 250);
//                    }
//                }
//                setDeleteSelected();
//            }
//        });
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String url = inputText.getText().toString();
                if(url.isEmpty()){
                    addButton.setEnabled(false);
                } else{
                    addButton.setEnabled(true);
                }

            }
        });
        editModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditButtons();
            }
        });
    }

    /**
     * Shows or hides all the delete buttons next to each row of URLs
     */
    private void showEditButtons(){
        for (int i = 0; i < labelHolder.getChildCount(); i++) {
            LinearLayout itemRow = (LinearLayout) labelHolder.getChildAt(i);
            for (int j = 0; j < itemRow.getChildCount(); j++) {
                if(itemRow.getChildAt(j) instanceof ImageButton){
                    ImageButton deleteButton = (ImageButton) itemRow.getChildAt(j);
                    if(!editMode) {
                        deleteButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                    }
                }
            }
        }
        editMode = !editMode;
        if(editMode){
            editModeText.setText("Done");
            editModeText.setTextColor(ContextCompat.getColor(getActivity(), R.color.nga_primary_light));
        } else {
            editModeText.setText("Edit");
            editModeText.setTextColor(ContextCompat.getColor(getActivity(), R.color.nga_warning));
        }
    }

    /**
     * Creates a textView with the given string, and adds it to the item list
     * @param text String to add to the list
     */
    private void addUrlView(String text){
        final String rowName = text;
        // Create a new Layout to hold items
        final LinearLayout itemRow = new LinearLayout(getContext());
        itemRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.HORIZONTAL));
        itemRow.setGravity(Gravity.CENTER);
//        itemRow.setPadding(0,48,0, 48);
//        itemRow.setBackground(getResources().getDrawable(R.drawable.delete_bg));


        ImageButton deleteButton = new ImageButton(getContext());
        deleteButton.setImageResource(R.drawable.delete_forever);
        deleteButton.setBackground(null);
        deleteButton.setVisibility(View.GONE);
        deleteButton.setPadding(48,48,48, 48);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(removeStringFromSet(text)){
                    labelHolder = (LinearLayout)ViewAnimation.fadeOutAndRemove(itemRow, labelHolder, 250);
                }
            }
        });

        // Create checkbox
        CheckBox check = new CheckBox(getContext());
        check.setPadding(16,0,64,0);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                setDeleteSelected();
            }
        });

        // Create text
        TextView nameText = new TextView(getContext());
        nameText.setText(text);
        LinearLayout.LayoutParams textLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textLayout.setMargins(16, 16, 16, 16);
        textLayout.gravity = Gravity.CENTER;
        nameText.setLayoutParams(textLayout);
        nameText.setPadding(32,32,32, 32);

        nameText.setTextAppearance(getContext(), R.style.textAppearanceSubtitle1);

        // Add everything
        itemRow.addView(deleteButton);
//        itemRow.addView(check);
        itemRow.addView(nameText);
        ViewAnimation.setSlideInFromRightAnimation(itemRow, 250);
        labelHolder.addView(itemRow);
    }

    /**
     * Find the checkbox in the item row and tell us if it's checked
     * @param itemRow linearLayout containing a checkbox
     * @return true if the checkbox is checked
     */
    private boolean isItemRowChecked(LinearLayout itemRow){
        for (int i = 0; i < itemRow.getChildCount(); i++) {
            if(itemRow.getChildAt(i) instanceof CheckBox){
                CheckBox check = (CheckBox) itemRow.getChildAt(i);
                return check.isChecked();
            }
        }
        return false;
    }

    /**
     * Return true if any rows are checked
     */
    private boolean isAnyRowChecked(){
        for (int i = 0; i < labelHolder.getChildCount(); i++) {
            LinearLayout itemRow = (LinearLayout)labelHolder.getChildAt(i);
            if(isItemRowChecked(itemRow)){
                return true;
            }
        }
        return false;
    }

//    /**
//     * Sets the delete selected text to active if any rows are checked, else it's disabled
//     */
//    private void setDeleteSelected(){
//        if(isAnyRowChecked()){
//            deleteSelected.setEnabled(true);
//            deleteSelected.setTextColor(ContextCompat.getColor(getActivity(), R.color.nga_warning));
//        } else {
//            deleteSelected.setEnabled(false);
//            deleteSelected.setTextColor(ContextCompat.getColor(getActivity(), R.color.black50));
//        }
//    }

    /**
     * Get the URL string from a row
     * @param itemRow linear layout containing a checkbox and text field
     * @return text from the text field
     */
    private String getRowText(LinearLayout itemRow){
        for (int i = 0; i < itemRow.getChildCount(); i++) {
            if (itemRow.getChildAt(i) instanceof TextView && !(itemRow.getChildAt(i) instanceof CheckBox)) {
                TextView urlText = (TextView)itemRow.getChildAt(i);
                String text = urlText.getText().toString();
                return text;
            }
        }
        return null;
    }

    /**
     * Delete a row from shared prefs and the Linear Layout
     * @param itemRow LinearLayout of the row to be deleted
     */
    private void deleteRow(View itemRow){
        ((ViewManager)itemRow.getParent()).removeView(itemRow);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
}

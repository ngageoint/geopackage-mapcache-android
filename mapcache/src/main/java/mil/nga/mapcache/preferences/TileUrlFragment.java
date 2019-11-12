package mil.nga.mapcache.preferences;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashSet;
import java.util.Set;

import mil.nga.mapcache.R;

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
     * Shared preferences
     */
    private SharedPreferences prefs;
    /**
     * Clear all URLs
     */
    private Button clearButton;
    /**
     * Add new URL from the inputText field to the shared prefs
     */
    private Button addButton;
    /**
     * Layout to hold a reference to all saved URLs
     */
    private LinearLayout labelHolder;

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
        clearButton = urlView.findViewById(R.id.clear_all);
        addButton = urlView.findViewById(R.id.add_url);
        labelHolder = urlView.findViewById(R.id.item_list_layout);
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
        return this.prefs.getStringSet("list", defaultVal);
    }

    /**
     * Save the given set to saved preferences
     * @param set set of strings to save
     * @return true after the commit is saved
     */
    private boolean saveSet(Set<String> set){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("list", set);
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
                    addUrlView(newUrl);
                }
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashSet<String> blankSet = new HashSet<>();
                saveSet(blankSet);
                // Remove all views from the Linear Layout holding the labels
                if(labelHolder.getChildCount() > 0)
                    labelHolder.removeAllViews();
            }
        });
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
        itemRow.setPadding(0,0,0, 32);

        // Create delete button
        ImageButton deleteButton = new ImageButton(getContext());
        deleteButton.setImageResource(R.drawable.ic_close_black_24dp);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = 32;
        deleteButton.setLayoutParams(params);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(removeStringFromSet(rowName)) {
                    deleteRow(itemRow);
                }
            }
        });

        // Create text
        TextView nameText = new TextView(getContext());
        nameText.setText(text);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add everything
        itemRow.addView(deleteButton);
        itemRow.addView(nameText);
        labelHolder.addView(itemRow);
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

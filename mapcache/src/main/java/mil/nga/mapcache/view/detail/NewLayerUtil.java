package mil.nga.mapcache.view.detail;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * Utilities associated with creating layers in a geopackage
 */
public class NewLayerUtil {

    /**
     * Set listeners on the min and max zoom level spinners to make them sync when a value is selected.
     * This way a user can't select a max zoom that is lower than the min zoom selection.
     * example: Select 10 for the min zoom, the max should automatically move to 10
     * @param minSpinner Minimum zoom level spinner
     * @param maxSpinner Maximum zoom level spinner
     */
    public static void setZoomLevelSyncListener(Spinner minSpinner, Spinner maxSpinner){

        // Make sure max is always higher than or equal to min
        minSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int minValue, long l) {
                int maxValue = maxSpinner.getSelectedItemPosition();
                if(minValue > maxValue){
                    maxSpinner.setSelection(minValue);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        // Make sure min is always less than max
        maxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int maxValue, long l) {
                int minValue = minSpinner.getSelectedItemPosition();
                if(minValue > maxValue){
                    minSpinner.setSelection(maxValue);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}

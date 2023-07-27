package mil.nga.mapcache.wizards.createfeature;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.mapcache.GeoPackageUtils;
import mil.nga.mapcache.R;
import mil.nga.mapcache.viewmodel.GeoPackageViewModel;
import mil.nga.sf.GeometryType;

/**
 * Handlers for creating a new feature layer in a GeoPackage
 */
public class NewFeatureLayerUI {

   /**
    * Create a dialog which accepts name, bounds, and type.  Then create a feature layer
    * on submit
    * @param activity - calling activity to access UI
    * @param viewModel - viewModel to access GeoPackages in the repository
    * @param geoName - name of the GeoPackage to create the layer in
    */
   public static void newFeatureLayerPopup(Activity activity, GeoPackageViewModel viewModel,
                                           String geoName){
      if (activity != null && viewModel != null && !geoName.isEmpty() && geoName != null) {
         LayoutInflater inflater = LayoutInflater.from(activity);
         View createFeaturesView = inflater.inflate(R.layout.create_features,
                 null);
         AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
         dialog.setView(createFeaturesView);

         final TextInputEditText nameInput = createFeaturesView
                 .findViewById(R.id.create_features_name_input);
         final TextInputEditText minLatInput = createFeaturesView
                 .findViewById(R.id.bounding_box_min_latitude_input);
         final TextInputEditText maxLatInput = createFeaturesView
                 .findViewById(R.id.bounding_box_max_latitude_input);
         final TextInputEditText minLonInput = createFeaturesView
                 .findViewById(R.id.bounding_box_min_longitude_input);
         final TextInputEditText maxLonInput = createFeaturesView
                 .findViewById(R.id.bounding_box_max_longitude_input);
         final TextView preloadedLocationsButton = createFeaturesView
                 .findViewById(R.id.bounding_box_preloaded);
         final Spinner geometryTypeSpinner = createFeaturesView
                 .findViewById(R.id.create_features_geometry_type);

         GeoPackageUtils.prepareBoundingBoxInputs(activity, minLatInput,
                         maxLatInput, minLonInput, maxLonInput,
                         preloadedLocationsButton);

         dialog.setPositiveButton(
                 activity.getString(R.string.geopackage_create_features_label),
                 (DialogInterface d, int id) -> {

                    try {
                       String tableName = nameInput.getText().toString();
                       if (tableName.isEmpty()) {
                          throw new GeoPackageException(
                                  activity.getString(R.string.create_features_name_label)
                                          + " is required");
                       }
                       double minLat = Double.parseDouble(minLatInput.getText().toString());
                       double maxLat = Double.parseDouble(maxLatInput.getText().toString());
                       double minLon = Double.parseDouble(minLonInput.getText().toString());
                       double maxLon = Double.parseDouble(maxLonInput.getText().toString());

                       if (minLat > maxLat) {
                          throw new GeoPackageException(
                                  activity.getString(R.string.bounding_box_min_latitude_label)
                                          + " can not be larger than "
                                          + activity.getString(R.string.bounding_box_max_latitude_label));
                       }

                       if (minLon > maxLon) {
                          throw new GeoPackageException(
                                  activity.getString(R.string.bounding_box_min_longitude_label)
                                          + " can not be larger than "
                                          + activity.getString(R.string.bounding_box_max_longitude_label));
                       }

                       BoundingBox boundingBox = new BoundingBox(minLon, minLat, maxLon, maxLat);

                       GeometryType geometryType = GeometryType.fromName(geometryTypeSpinner
                                       .getSelectedItem().toString());
                       if (!viewModel.createFeatureTable(geoName, boundingBox, geometryType, tableName)) {
                          GeoPackageUtils.showMessage(activity,
                                          activity.getString(R.string.geopackage_create_features_label),
                                          "There was a problem generating a tile table");
                       }
                    } catch (Exception e) {
                       GeoPackageUtils.showMessage(activity,
                                       activity.getString(R.string.geopackage_create_features_label),
                                       e.getMessage());
                    }
                 }).setNegativeButton(activity.getString(R.string.button_cancel_label),
                 (DialogInterface d, int id) -> d.cancel());
         AlertDialog createdDialog = dialog.create();
         createdDialog.setOnShowListener(dialog1 -> {
            createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    activity.getResources().getColor(R.color.textNavNotSelected,
                            activity.getTheme()));
         });
         nameInput.setError("Name cannot be empty");
         nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
               String givenName = nameInput.getText() != null ? nameInput.getText().toString() : "";
               createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
               createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                       activity.getResources().getColor(R.color.primaryButtonColor,
                               activity.getTheme()));
               if(givenName.isEmpty()) {
                  createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                  nameInput.setError("Name cannot be empty");
                  createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                          activity.getResources().getColor(R.color.textNavNotSelected,
                                  activity.getTheme()));
               } else {
                  String pattern = activity.getResources().getString(R.string.regex_alphanumeric);
                  boolean allowed = Pattern.matches(pattern, givenName);
                  if (!allowed) {
                     nameInput.setError(activity.getResources().getString(R.string.must_be_alphanumeric));
                     createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                     createdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                             activity.getResources().getColor(R.color.textNavNotSelected,
                                     activity.getTheme()));
                  }
               }
            }
         });
         createdDialog.show();
      }
   }
}

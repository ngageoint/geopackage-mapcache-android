package mil.nga.mapcache.view.layer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.OnDialogButtonClickListener;

/**
 * Util class to launch dialogs and return click listeners for the feature column action buttons
 * in the GeoPackage layer detail view page
 */

public class FeatureColumnUtil {
    /**
     * Context of the activity
     */
    private Context mContext;

    public FeatureColumnUtil(Context context){
        mContext = context;
    }

    /**
     * Open a confirmation dialog for deleting a Layer Feature Column
     */
    public void openDeleteDialog(Context context, FeatureColumnDetailObject columnDetailObject,
                                 final OnDialogButtonClickListener listener){
        // Create Alert window with basic input text layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.basic_label_alert, null);
        // Logo and title
        ImageView alertLogo = (ImageView) alertView.findViewById(R.id.alert_logo);
        alertLogo.setBackgroundResource(R.drawable.material_delete_forever);
        TextView titleText = (TextView) alertView.findViewById(R.id.alert_title);
        titleText.setText(R.string.delete_feature_column_text);
        TextView actionLabel = (TextView) alertView.findViewById(R.id.action_label);
        actionLabel.setText(columnDetailObject.getName());
        actionLabel.setVisibility(View.VISIBLE);

        AlertDialog deleteDialog = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setView(alertView)
                .setIcon(context.getResources().getDrawable(R.drawable.material_delete_forever, context.getTheme()))
                .setPositiveButton("Delete", (dialog, which)->{
                    dialog.dismiss();
                    listener.onDeleteFeatureColumn(columnDetailObject.getGeoPackageName(),
                            columnDetailObject.getLayerName(), columnDetailObject.getName());
                })
                .setNegativeButton(context.getString(R.string.button_cancel_label),
                        (dialog, which)->{
                            dialog.dismiss();
                            listener.onCancelButtonClicked();
                        }).create();

        deleteDialog.show();

    }
}

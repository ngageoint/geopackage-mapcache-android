package mil.nga.mapcache.layersprovider;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import java.util.Observable;
import java.util.Observer;

import mil.nga.mapcache.R;

/**
 * Shows the layers view within a dialog.
 */
public class LayersViewDialog extends LayersView implements Observer {

    /**
     * The dialog the layers view is shown in.
     */
    private AlertDialog alertDialog;

    /**
     * Constructor.
     *
     * @param context The application context.
     * @param model   The layers model.
     */
    public LayersViewDialog(Context context, LayersModel model) {
        super(context, model);
        model.addObserver(this);
    }

    @Override
    public void show() {
        super.show();

        AlertDialog.Builder dialog = new AlertDialog.Builder(
                getContext(), R.style.AppCompatAlertDialogStyle)
                .setView(getView());
        alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        getCloseLogo().setOnClickListener((v) -> alertDialog.dismiss());

        alertDialog.show();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (LayersModel.SELECTED_LAYERS_PROP.equals(o)) {
            alertDialog.dismiss();
        }
    }
}

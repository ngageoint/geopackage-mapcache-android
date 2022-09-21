package mil.nga.mapcache.io.network.slowserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * The dialog informing the user of a slow server.
 */
public class SlowServerView {

    /**
     * The model.
     */
    private final SlowServerModel model;

    /**
     * Constructor.
     *
     * @param model The model for the view.
     */
    public SlowServerView(SlowServerModel model) {
        this.model = model;
    }

    /**
     * Shows the dialog informing the user of a slow server.
     *
     * @param activity The main activity.
     */
    public void show(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Slow to Download From " + model.getHost());
        builder.setMessage(this.model.getMessage());
        builder.setPositiveButton("Ok", (DialogInterface dialog, int arg1) -> dialog.dismiss());
        builder.show();
    }
}

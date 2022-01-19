package mil.nga.mapcache.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import mil.nga.mapcache.R;

/**
 * Dialog used to get the user's username and password for specific servers.
 */
public class UserPassDialog {

    /**
     * The applications activity.
     */
    private Activity activity;

    /**
     * Constructs a new username password dialog.
     * @param activity The applications activity.
     */
    public UserPassDialog(Activity activity) {
        this.activity = activity;
    }

    /**
     * Asks the user for their username and password for a specific server.
     */
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View userPassView = inflater.inflate(R.layout.userpass_layout, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
                .setView(userPassView);
        dialog.setNegativeButton("Cancel", null);
        dialog.setPositiveButton("Login", null);
        dialog.setTitle("Login To");
        dialog.setMessage("server.com");
        dialog.create().show();
    }
}

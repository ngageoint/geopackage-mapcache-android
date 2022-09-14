package mil.nga.mapcache.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import mil.nga.mapcache.R;

/**
 * Dialog used to get the user's username and password for specific servers.
 */
public class UserPassDialog {

    /**
     * The applications activity.
     */
    private final Activity activity;

    /**
     * The model for the dialog.
     */
    private final UserPassModel model = new UserPassModel();

    /**
     * Constructs a new username password dialog.
     *
     * @param activity The applications activity.
     * @param host     The server requesting login information.
     */
    public UserPassDialog(Activity activity, String host) {
        this.activity = activity;
        this.model.setLoginTo(host);
    }

    /**
     * Asks the user for their username and password for a specific server.
     */
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View userPassView = inflater.inflate(R.layout.userpass_layout, null);
        TextView username = (TextView) userPassView.findViewById(R.id.username);
        TextView password = (TextView) userPassView.findViewById(R.id.password);
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
                .setView(userPassView);
        dialog.setNegativeButton("Cancel", this::onCancelClicked);
        dialog.setPositiveButton("Login", (var1, var2) -> onLoginClicked(username, password));
        dialog.setTitle("Login To");
        dialog.setMessage(model.getLoginTo());
        dialog.create().show();
    }

    /**
     * Gets the login information.
     *
     * @return The model containing the login info.
     */
    public UserPassModel getModel() {
        return model;
    }

    /**
     * Called when login is clicked.
     *
     * @param username The username text input.
     * @param password The password text input.
     */
    private void onLoginClicked(TextView username, TextView password) {
        model.setUsername(username.getText().toString());
        model.setPassword(password.getText().toString());
    }

    /**
     * Called when the login has been canceled.
     *
     * @param dialog The login dialog.
     * @param var2   The buttons pressed state.
     */
    private void onCancelClicked(DialogInterface dialog, int var2) {
        model.setUsername(null);
        model.setPassword(null);
    }
}

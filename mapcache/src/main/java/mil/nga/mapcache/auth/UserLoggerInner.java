package mil.nga.mapcache.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.util.Log;

import java.net.URL;

/**
 * Logs the user into a specified server.
 */
public class UserLoggerInner {

    /**
     * Unique string identifying map cache saved accounts.
     */
    private final static String MAPCACHE_ACCOUNT_TYPE = "mil.nga.mapcache";

    /**
     * The applications activity.
     */
    private final Activity activity;

    /**
     * Contains what the user entered for their username and password if we needed to prompt them.
     */
    private UserPassModel model;

    /**
     * Constructor.
     *
     * @param activity The applications activity.
     */
    public UserLoggerInner(Activity activity) {
        this.activity = activity;
    }

    /**
     * Retrieves the user's username and password for the specified server.  If found a stored one
     * it will then use the specified authenticator to log the user into the server.  If a username
     * and password is not found in the AccountManager or if authentication fails, we will then
     * prompt the user for a username and password authenticate and then store it for later use.
     *
     * @param url            The url to login to.
     * @param IAuthenticator Object that knows how to authenticate user against the url.
     */
    public void login(URL url, Authenticator IAuthenticator) {
        String host = url.getAuthority();
        AccountManager accountManager = AccountManager.get(this.activity);
        Account[] accounts = accountManager.getAccountsByType(MAPCACHE_ACCOUNT_TYPE);

        boolean authenticated = false;
        if (accounts != null && accounts.length > 0) {
            for (Account account : accounts) {
                String[] split = account.name.split(" - ");
                if (split[1].equals(host)) {
                    String userName = split[0];
                    String password = accountManager.getPassword(account);
                    authenticated = IAuthenticator.authenticate(url, userName, password);
                    if (!authenticated) {
                        accountManager.removeAccount(account, null, null);
                    }
                    break;
                }
            }
        }

        while (!authenticated) {
            askUserAndWait(host);
            String username = model.getUsername();
            String password = model.getPassword();
            if (username != null && password != null) {
                authenticated = IAuthenticator.authenticate(url, username, password);
                if (authenticated) {
                    Account newAccount = new Account(username + " - " + host, MAPCACHE_ACCOUNT_TYPE);
                    accountManager.addAccountExplicitly(newAccount, password, null);
                }
            } else {
                // Not really authenticated, user cancelled the login
                authenticated = true;
            }
        }
    }

    /**
     * Prompts the user for their username and password, on the UI thread, and waits for the users
     * response before returning.
     *
     * @param host The server the user will be logging into.
     */
    private synchronized void askUserAndWait(String host) {
        activity.runOnUiThread(() -> askUser(host));
        try {
            wait();
        } catch (InterruptedException e) {
            Log.d(UserLoggerInner.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for their username and password.
     *
     * @param host The server the user will be logging into.
     */
    private void askUser(String host) {
        UserPassDialog dialog = new UserPassDialog(this.activity, host);
        model = dialog.getModel();
        model.addObserver(this::onUpdate);
        dialog.show();
    }

    /**
     * Called when the UserPassDialog model is updated.
     *
     * @param model    The model.
     * @param property The property that changed in the model.
     */
    private synchronized void onUpdate(Object model, Object property) {
        if (UserPassModel.PASSWORD_PROP.equals(property)) {
            notify();
        }
    }
}

package mil.nga.mapcache.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Class needed in order to be able to save user's accounts for this app.
 */
public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    // Notice, this is the same Authenticator class we defined earlier
    private AccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        authenticator = new AccountAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}

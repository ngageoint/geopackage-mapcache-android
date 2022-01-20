package mil.nga.mapcache.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Class needed in order to be able to save user's accounts for this app.
 */
public class AuthenticatorService extends Service {

    @Override
    public void onCreate() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package mil.nga.mapcache.preferences;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.MatomoEventDispatcher;

public class PrivacyPolicyActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_privacy_policy);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getSupportActionBar().setTitle("Privacy Policy");
        // Adds back arrow button to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new PrivacyPolicyFragment()).commit();

        MatomoEventDispatcher.Companion.submitScreenEvent("/Privacy Policy Activity", "Privacy Policy Opened");
    }

    /**
     * Add back arrow button listener
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

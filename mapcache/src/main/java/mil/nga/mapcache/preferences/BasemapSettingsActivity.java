package mil.nga.mapcache.preferences;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.MatomoEventDispatcher;

/**
 * The activity where the user can configure their basemaps.
 */
public class BasemapSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getSupportActionBar().setTitle("Base maps");
        // Adds back arrow button to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new BasemapSettingsFragment(this)).commit();

        MatomoEventDispatcher.Companion.submitScreenEvent("/BaseMap Settings Activity", "BaseMap Settings Opened");
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

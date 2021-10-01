package mil.nga.mapcache.listeners;

import android.hardware.SensorEvent;

public interface SensorCallback {
    void onSensorChanged(SensorEvent event, float bearing);

}
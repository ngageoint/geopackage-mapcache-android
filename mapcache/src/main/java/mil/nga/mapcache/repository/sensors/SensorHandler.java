package mil.nga.mapcache.repository.sensors;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import mil.nga.mapcache.listeners.SensorCallback;

/**
 * Handles sensor events to show map bearing
 * Registers itself as a sensor listener based on the TYPE_ROTATION_VECTOR and calculates bearing,
 * then sends out a callback
 */
public class SensorHandler extends Service implements SensorEventListener {

    private double mAzimuth = 0;
    private final Context mContext;
    private static final String TAG = "Sensor Mgr: ";
    private final SensorCallback mSensorEventCallback;
    private SensorManager mSensorManager;

    public SensorHandler(SensorCallback sensorCallback, Context context) {
        this.mContext = context;
        this.mSensorEventCallback = sensorCallback;
        initSensors();
    }

    /**
     * Calculates the measured bearing when the sensor change is dedected and calls the callback
     * @param event sensor event that's received
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] mMatrixR = new float[9];
            float[] mMatrixValues = new float[3];
            // Get rotation matrix
            SensorManager.getRotationMatrixFromVector(mMatrixR, event.values);
            SensorManager.getOrientation(mMatrixR, mMatrixValues);
            // convert to degrees
            mAzimuth = Math.toDegrees(mMatrixValues[0]);
        }
        float mCompassLastMeasuredBearing = (float) mAzimuth;
        mSensorEventCallback.onSensorChanged(event, mCompassLastMeasuredBearing);
    }

    /**
     * Create the sensor and register
     */
    private void initSensors() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensorRotationVector = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        /* Initialize the gravity sensor */
        if (mSensorRotationVector != null) {
            Log.i(TAG, "Rotation Vector sensor available. (TYPE_ROTATION_VECTOR)");
            mSensorManager.registerListener(this,
                    mSensorRotationVector, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.i(TAG, "Rotation Vector sensor unavailable. (TYPE_ROTATION_VECTOR)");
        }
    }

    /**
     * Unregisters the listener to stop updates
     */
    public void stopUpdates(){
        mSensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

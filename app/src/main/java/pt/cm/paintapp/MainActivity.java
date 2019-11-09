package pt.cm.paintapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.GestureDetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /**
     * Holder for the threshold value
     */
    private static final float TRESHHOLD = 5.0f;

    /**
     * Holder for the sensor manager instance.
     */
    @Nullable
    private SensorManager mSensorManager;
    /**
     * Holder for the accelerometer instance.
     */
    @Nullable
    private Sensor mAccelerometer;
    /**
     * Holder for the proximity instance.
     */
    @Nullable
    private Sensor mProximity;
    /**
     * Holder for the paint canvas
     */
    @Nullable
    private PaintCanvas mPaintCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestureListener mGestureListener = new GestureListener();
        GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);

        // Get the sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Check if the sensor manager is valid
        if (mSensorManager != null) {
            // Get the gyroscope
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // Get the proximity sensor
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        mPaintCanvas = new PaintCanvas(getApplicationContext(), null, mGestureDetector);
        mGestureListener.setCanvas(mPaintCanvas);

        setContentView(mPaintCanvas);// adds the created view to the screen
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            // Check if the sensor is the gyroscope
            case Sensor.TYPE_LINEAR_ACCELERATION:

                // Get current values
                float aX = event.values[0];
                float aY = event.values[1];
                float aZ = event.values[2];

                // Log.d(getClass().getSimpleName(), String.format("Delta values: X=%s, Y=%s, Z=%s", aX, aY, aZ));

                // Change the background
                if (Math.abs(aX) >= TRESHHOLD || Math.abs(aY) >= TRESHHOLD || Math.abs(aZ) >= TRESHHOLD)
                    // Check if the paint canvas is set
                    if (mPaintCanvas != null)
                        // Change the background
                        mPaintCanvas.changeBackground();

                break;

            // Check if is the proximity sensor
            case Sensor.TYPE_LIGHT:

                // Check if the paint canvas is set
                if (mPaintCanvas != null && mPaintCanvas.getBackground() instanceof ColorDrawable) {

                    float val = event.values[0];
                    if (val > 50)
                        val = 50f;

                    // Get the current color
                    int color = ((ColorDrawable) mPaintCanvas.getBackground()).getColor();
                    int alpha = (int) ((val / (mProximity != null ? 50 : 1)) * 255);

                    // Log.d(getClass().getSimpleName(), "V: " + val);

                    // Set the paint canvas
                    mPaintCanvas.setBackgroundColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
                    mPaintCanvas.invalidate();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}

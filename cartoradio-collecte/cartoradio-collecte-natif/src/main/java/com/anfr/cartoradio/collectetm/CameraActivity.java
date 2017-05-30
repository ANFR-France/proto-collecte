package com.anfr.cartoradio.collectetm;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

public class CameraActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    /*Camera */
    TextView txt;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        txt = (TextView) findViewById(R.id.label_hello);
    }

    @Override
    public void onLocationChanged(Location location) {
        float resultMatrice[] = new float[0];
        float acceleromterVector[]  = new float[0];
        float magneticVector[] =  new float[0];

        boolean bool = SensorManager.getRotationMatrix(resultMatrice,null,acceleromterVector,magneticVector);
        txt.setText(String.valueOf(bool));
    }



    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            mGravity = event.values;
        }
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mGeomagnetic = event.values;
        }
        if(mGravity != null && mGeomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R,I,mGravity,mGeomagnetic);
            if(success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                Log.e("NEW ORIENTATION---","---------------");
                Log.e("orientation azimut", String.valueOf(orientation[0]*60));
                Log.e("orientation pitch", String.valueOf(orientation[1]*60));
                Log.e("orientation roll", String.valueOf(orientation[2]*60));

                txt.setText("orientation azimut " + String.valueOf(orientation[0]*60) + "\n" + "orientation pitch" + String.valueOf(orientation[1]*60) + "\n" + "orientation roll"+ String.valueOf(orientation[2]*60));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }
}

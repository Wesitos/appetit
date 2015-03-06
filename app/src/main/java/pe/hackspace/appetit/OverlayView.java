package pe.hackspace.appetit;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

public class OverlayView extends View implements SensorEventListener, LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";
    private Location lastLocation = null;
    
    private LocationManager locationManager;
    private float[] lastAccelerometer = null;
    private float[] lastCompass = null;
    private float[] lastGyro = null;
    private float[] cameraRotation;
    private float[] orientation = null;
    private Pair<Float,Float> cameraAngle;


    public OverlayView(Context context, Pair<Float,Float> camAngle) {
        super(context);
        // Camera Angle
        cameraAngle = camAngle;

        // Motion Sensors
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        boolean isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Position Sensor
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);

        locationManager.requestLocationUpdates(best, 50, 0, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (orientation != null) {
            float horizontalFOV = cameraAngle.first;
            float verticalFOV = cameraAngle.second;

            // use roll for screen rotation
            canvas.rotate((float) (90.0f - Math.toDegrees(orientation[2])));
            // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
            float dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

            // wait to translate the dx so the horizon doesn't get pushed off
            canvas.translate(0.0f, 0.0f - dy);

            // make our line big enough to draw regardless of rotation and translation
            canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, targetPaint);
        }
    }

    public void onLocationChanged(Location location) {
        lastLocation = location;
    }

    public void onProviderDisabled(String provider) {
        // ...
    }

    public void onProviderEnabled(String provider) {
        // ...
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ...
    }

    public void onAccuracyChanged(Sensor sensor, int i){

    }

    public void onSensorChanged(SensorEvent event){

        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values;
                break;
            case Sensor.TYPE_GYROSCOPE:
                lastGyro = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = event.values;
                break;
        }

        if( lastAccelerometer != null &&  lastCompass != null) {
            // compute rotation matrix
            float rotation[] = new float[9];
            float identity[] = new float[9];
            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);

            if (gotRotation) {
                cameraRotation = new float[9];
                // remap such that the camera is pointing straight down the Y axis
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, cameraRotation);

                // orientation vector
                orientation = new float[3];
                SensorManager.getOrientation(cameraRotation, orientation);
            }
        }
        this.invalidate();
    }
}

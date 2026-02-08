package com.example.stepcounter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView steps;
    private int totalSteps = 0;
    private int initialSteps = -1;
    private SensorManager sensorManager = null;
    private Sensor stepSensor;
    private static final int DAILY_STEP_GOAL = 5000;


    private CircularProgressIndicator progressBar;
    private static final String PREFS = "step_prefs";
    private static final String KEY_INITIAL_STEPS = "initialSteps";
    private static final String KEY_LAST_RESET_DAY = "lastResetDay";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        }

        steps = findViewById(R.id.steps);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        initialSteps = loadInitialSteps();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (stepSensor != null) {
                    sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }

            } else {
                Toast.makeText(this, "Step permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            totalSteps = (int) event.values[0];

            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            int lastResetDay = prefs.getInt(KEY_LAST_RESET_DAY, -1);
            int today = getTodayDayNumber();

            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                totalSteps = (int) event.values[0];

                if (initialSteps == -1) {
                    initialSteps = totalSteps;
                    saveInitialSteps(initialSteps);
                }

                int stepsSinceReset = totalSteps - initialSteps;
                steps.setText(String.valueOf(stepsSinceReset));

                int progress = Math.min(stepsSinceReset, DAILY_STEP_GOAL);
                progressBar.setProgress(progress);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void saveInitialSteps ( int steps){
            getSharedPreferences("step_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("initialSteps", steps)
                    .apply();
        }

        private int loadInitialSteps () {
            return getSharedPreferences("step_prefs", MODE_PRIVATE)
                    .getInt("initialSteps", -1);
        }

        private int getTodayDayNumber () {
            return (int) (System.currentTimeMillis() / (1000 * 60 * 60 * 24));
        }

}

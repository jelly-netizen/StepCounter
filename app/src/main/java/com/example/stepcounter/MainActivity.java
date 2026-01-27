package com.example.stepcounter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView steps;
    private int totalSteps = 0;
    private int initialSteps = -1;
    private SensorManager sensorManager = null;
    private Sensor stepSensor;
    private TextView textView;

    private Button reset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        }

        steps = findViewById(R.id.steps);
        textView = findViewById(R.id.textView);
        reset = findViewById(R.id.resestBtn);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        initialSteps = loadInitialSteps();


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    initialSteps = totalSteps;
                    saveInitialSteps(initialSteps);
                    steps.setText("0");

                }
        });
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

            if (initialSteps == -1) {
                initialSteps = totalSteps;
                saveInitialSteps(initialSteps);
            }

            int stepsSinceReset = totalSteps - initialSteps;
            steps.setText(String.valueOf(stepsSinceReset));
        }


    }

    private void saveInitialSteps(int steps) {
        getSharedPreferences("step_prefs", MODE_PRIVATE)
                .edit()
                .putInt("initialSteps", steps)
                .apply();
    }

    private int loadInitialSteps() {
        return getSharedPreferences("step_prefs", MODE_PRIVATE)
                .getInt("initialSteps", -1);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

package com.example.stepcounter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView steps;
    private int totalSteps = 0;
    private int initialSteps = -1;
    private SensorManager sensorManager = null;
    private Sensor stepSensor;
    private static int dailyStepGoal = 5000;


    private ProgressBar progressBar;

    private static final String PREFS = "step_prefs";
    private static final String KEY_INITIAL_STEPS = "initialSteps";
    private static final String KEY_LAST_RESET_DAY = "lastResetDay";
    private SharedPreferences prefs;
    private static final String KEY_STEP_GOAL = "stepGoal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions and sensor setup here...
        steps = findViewById(R.id.steps);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(dailyStepGoal);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        dailyStepGoal = prefs.getInt(KEY_STEP_GOAL, 5000);

        // -------------------------------
        // YOUR NEW GOAL BUTTON LOGIC
        // -------------------------------
        Button showGoalButton = findViewById(R.id.showGoalButton);
        EditText goalInput = findViewById(R.id.goalInput);
        Button setGoalButton = findViewById(R.id.setGoalButton);

        showGoalButton.setOnClickListener(v -> {
            goalInput.setVisibility(View.VISIBLE);
            setGoalButton.setVisibility(View.VISIBLE);
        });

        setGoalButton.setOnClickListener(v -> {
            String input = goalInput.getText().toString();
            if (!input.isEmpty()) {
                int newGoal = Integer.parseInt(input);
                dailyStepGoal = newGoal;
                progressBar.setMax(dailyStepGoal);
                prefs.edit().putInt(KEY_STEP_GOAL, newGoal).apply();
                Toast.makeText(this, "Goal set to " + newGoal, Toast.LENGTH_SHORT).show();

                // optionally hide input again
                goalInput.setVisibility(View.GONE);
                setGoalButton.setVisibility(View.GONE);
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(goalInput.getWindowToken(), 0);
            }
        });
        // -------------------------------
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
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        totalSteps = (int) event.values[0];

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int lastResetDay = prefs.getInt(KEY_LAST_RESET_DAY, -1);
        int today = getTodayDayNumber();

        // FIRST RUN or NEW DAY
        if (initialSteps == -1 || lastResetDay != today) {
            initialSteps = totalSteps;
            saveInitialSteps(initialSteps);
            saveLastResetDay(today);
        }

        int stepsSinceReset = totalSteps - initialSteps;
        steps.setText(String.valueOf(stepsSinceReset));

        int progress = Math.min(stepsSinceReset, dailyStepGoal);
        progressBar.setProgress(progress);
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

    private void saveLastResetDay(int day) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putInt(KEY_LAST_RESET_DAY, day)
                .apply();
    }


}

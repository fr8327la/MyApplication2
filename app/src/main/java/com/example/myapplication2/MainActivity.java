package com.example.myapplication2;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Vibrator;
import android.media.Ringtone;
import android.media.RingtoneManager;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Button startButton;
    private TextView accelerometerInfo;
    private TextView originalInfo;
    private ImageView myImageView;
    private Button activateButton;
    private boolean isActivated = false;
    private SensorEventListener sensorEventListener;
    private Ringtone notificationSound;
    private boolean isAccelerometerActive = false;
    private boolean shouldShowAccelerometerInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        notificationSound = RingtoneManager.getRingtone(getApplicationContext(),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        startButton = findViewById(R.id.button1);
        accelerometerInfo = findViewById(R.id.accelerometerInfo);
        originalInfo = findViewById(R.id.originalInfo);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAccelerometerActive) {
                    // Unregister the sensor listener when the button is pressed again
                    sensorManager.unregisterListener(sensorEventListener, accelerometer);
                    shouldShowAccelerometerInfo = false;
                    accelerometerInfo.setVisibility(View.GONE);
                    originalInfo.setVisibility(View.VISIBLE);
                    startButton.setBackgroundColor(Color.parseColor("#90b990"));
                    startButton.setText("Accelerometer");
                } else {
                    startAccelerometer();
                    shouldShowAccelerometerInfo = true;
                    startButton.setBackgroundColor(Color.GRAY);
                    startButton.setText("Stop");
                }
                // Toggle the accelerometer state
                isAccelerometerActive = !isAccelerometerActive;
            }
        });

        myImageView = findViewById(R.id.star);
        activateButton = findViewById(R.id.activateButton);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float x = event.values[0];

                    // Adjust the speed of movement based on the tilt
                    float movementSpeed = 20;

                    // Calculate the new X position of the ImageView
                    float newX = myImageView.getX() - x * movementSpeed;

                    // Limit the new position to stay within the screen bounds
                    float screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float newXCapped = Math.max(0, Math.min(newX, screenWidth - myImageView.getWidth()));

                    if (Math.abs(x) > 0.1) {
                        playNotificationSound();
                    }
                    // Update the position of the ImageView
                    myImageView.setX(newXCapped);
                }
            }
        };

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActivated) {
                    // Unregister the sensor listener when the button is pressed again
                    sensorManager.unregisterListener(sensorEventListener, accelerometer);

                    // Change the button color back to grey when deactivated
                    activateButton.setBackgroundColor(Color.parseColor("#90b990"));
                    activateButton.setText("Activate");
                } else {
                    // Register the sensor listener only when the button is activated
                    sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

                    // Change the button color to green when activated
                    activateButton.setBackgroundColor(Color.GRAY);
                    activateButton.setText("Deactivate");
                }

                // Toggle the activation state
                isActivated = !isActivated;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(sensorEventListener, accelerometer);
        if (!shouldShowAccelerometerInfo) {
            accelerometerInfo.setVisibility(View.GONE);
            originalInfo.setVisibility(View.VISIBLE);
        }
    }

    private void startAccelerometer() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 200 milliseconds when the accelerometer is started
        vibrator.vibrate(200);

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    String info = "X: " + x + "\nY: " + y + "\nZ: " + z;
                    accelerometerInfo.setText("Accelerometer Data:\n" + info);
                    if (shouldShowAccelerometerInfo) {
                        accelerometerInfo.setVisibility(View.VISIBLE); // Show the accelerometer information
                        originalInfo.setVisibility(View.GONE);
                    }
                }
            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void playNotificationSound() {
        if (notificationSound != null && !notificationSound.isPlaying()) {
            notificationSound.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the notification sound when the activity is destroyed
        if (notificationSound != null) {
            notificationSound.stop();
            notificationSound.play();
        }
    }
}

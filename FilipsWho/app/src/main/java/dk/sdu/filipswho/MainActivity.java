package dk.sdu.filipswho;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

import nl.stijngroenen.tradfri.device.Device;
import nl.stijngroenen.tradfri.device.Gateway;
import nl.stijngroenen.tradfri.device.Light;
import nl.stijngroenen.tradfri.device.event.EventHandler;
import nl.stijngroenen.tradfri.device.event.LightChangeBrightnessEvent;
import nl.stijngroenen.tradfri.util.ColourHex;
import nl.stijngroenen.tradfri.util.Credentials;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtPrint = null;
    private SensorManager sensorManager;
    private Sensor sensor;

    private Gateway gateway = null;
    private Credentials credentials = null;
    private Light lightBulb = null;
    private static final int LIGHTBULB_ID = 65541;

    // State variables
    private int latestLux = 0;
    private boolean autoAdjustIsRunning = false;
    private int setpoint = 0;
    private int setpointError = 15;
    private int desiredBrightness = 0;
    private int actualBrightness = 0;
    private int adjustmentLevel = 1;
    private int minBrightness = 1;
    private int maxBrightness = 254;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtPrint = findViewById(R.id.txtPrint);

        initializeSensor();

        if (this.gateway == null) {
            initializeGateway();
        }
        this.desiredBrightness = this.actualBrightness;
        SeekBar brightnessBar = findViewById(R.id.brightnessSeekbar);
        brightnessBar.setProgress(this.actualBrightness);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sendBrightnessLevel(i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                autoAdjustIsRunning = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    private void initializeSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, sensor, SENSOR_DELAY_NORMAL);
    }


    private void initializeGateway() {
        this.gateway = new Gateway("192.168.1.100");
        this.credentials = this.gateway.connect("mbpRdLgXUNXBzS5M");

        Device lightDevice = gateway.getDevice(LIGHTBULB_ID);
        this.lightBulb = gateway.getDevice(LIGHTBULB_ID).toLight();
        setActualBrightness(this.lightBulb.getBrightness());
        this.lightBulb.setColourHex(ColourHex.WARM);

        lightDevice.enableObserve();
        lightDevice.addEventHandler(new EventHandler<LightChangeBrightnessEvent>() {
            @Override
            public void handle(LightChangeBrightnessEvent event) {
                setActualBrightness(event.getNewBrightness());
                System.out.println("Received brightness: " + event.getNewBrightness());
            }
        });
    }

    public void setActualBrightness(int actualBrightness) {
        this.actualBrightness = actualBrightness;
    }

    public void btnSetpointHandler(View view) {
        SeekBar input = findViewById(R.id.brightnessSeekbar);
        this.setpoint = latestLux;
        autoAdjustIsRunning = true;
        desiredBrightness = actualBrightness;
        System.out.println("New setpoint: " + setpoint);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float illuminance = event.values[0];
        this.latestLux = (int)illuminance;

        TextView txtIlluminance = findViewById(R.id.txtIlluminance);

        String unit = " lx";

        txtIlluminance.setText(illuminance + unit);

        if(this.autoAdjustIsRunning)
        adjustLightSource((int)illuminance, this.setpoint, this.setpointError);

    }

    private void adjustLightSource(int lux, int setpoint, int setpointError) {
        int difference =lux - setpoint;

        int multiplier = 1;
        if (difference > setpointError) {
            if (this.desiredBrightness > this.minBrightness) {
                this.desiredBrightness = desiredBrightness - this.adjustmentLevel * multiplier;
            }
        } else if (difference < setpointError * -1) {
            if (this.desiredBrightness < this.maxBrightness) {
                this.desiredBrightness = desiredBrightness + this.adjustmentLevel *multiplier;

            }
        }


        sendBrightnessLevel(this.desiredBrightness);


    }

    private void sendBrightnessLevel(int newBrightness) {
        if(newBrightness < minBrightness)
            newBrightness = minBrightness;
        if(newBrightness > maxBrightness)
            newBrightness = maxBrightness;

        lightBulb.setBrightness(newBrightness);
        System.out.println("Adjust light source: " + newBrightness);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
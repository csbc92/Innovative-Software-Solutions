package dk.sdu.filipswho;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

import nl.stijngroenen.tradfri.device.Device;
import nl.stijngroenen.tradfri.device.Gateway;
import nl.stijngroenen.tradfri.device.Light;
import nl.stijngroenen.tradfri.device.event.EventHandler;
import nl.stijngroenen.tradfri.device.event.LightChangeBrightnessEvent;
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
    private int setpoint = 500;
    private int setpointError = 15;
    private int brightness = 0;
    private int adjustmentLevel = 2;
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

        initializeUiValues();
    }

    private void initializeUiValues() {
        // Setpoint
        EditText setpointInput = findViewById(R.id.inputSetPoint);
        setpointInput.setText(this.setpoint + "");
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
        setBrightness(this.lightBulb.getBrightness());

        lightDevice.enableObserve();
        lightDevice.addEventHandler(new EventHandler<LightChangeBrightnessEvent>() {
            @Override
            public void handle(LightChangeBrightnessEvent event) {
                setBrightness(event.getNewBrightness());
                System.out.println("Received brightness: " + event.getNewBrightness());
            }
        });
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public void btnSetpointHandler(View view) {
        EditText input = findViewById(R.id.inputSetPoint);
        this.setpoint = Integer.parseInt(input.getText().toString());
        System.out.println("New setpoint: " + setpoint);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float illuminance = event.values[0];

        TextView txtIlluminance = findViewById(R.id.txtIlluminance);

        String unit = " lx";

        txtIlluminance.setText(illuminance + unit);

        adjustLightSource((int)illuminance, this.setpoint, this.setpointError);

    }

    private void adjustLightSource(int lux, int setpoint, int setpointError) {
        int difference = lux - setpoint;
        int newBrightness;

        if (difference > setpointError) {
            if (this.brightness > this.minBrightness) {
                 newBrightness = brightness - this.adjustmentLevel;
                lightBulb.setBrightness(newBrightness);
                System.out.println("Adjust light source: " + newBrightness);
            }
        } else if (difference < setpointError * -1) {
            if (this.brightness < this.maxBrightness) {
                newBrightness = brightness + this.adjustmentLevel;
                lightBulb.setBrightness(newBrightness);
                System.out.println("Adjust light source: " + newBrightness);
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
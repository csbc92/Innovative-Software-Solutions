package dk.sdu.filipswho;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private SensorManager sensorManager;
    private Sensor sensor;

    // Gateway variables
    private Gateway gateway = null;
    private Credentials credentials = null;
    private Light lightBulb = null;
    private static final int LIGHTBULB_ID = 65541;
    private static final String GATEWAY_IP = "192.168.1.100";
    private static final String GATEWAY_PASSPHRASE = "mbpRdLgXUNXBzS5M";

    // Intensity variables
    private int latestLux = 0;
    private boolean isRunning = false;
    private int setpoint = 0;
    private int slack = 15;
    private int desiredIntensity = 0; // The intensity that we wish to set
    private int actualIntensity = 0; // The actual intensity received from the light bulb
    private static final int RESOLUTION = 1; // This is the resolution used to adjust the intensity state
    private static final int MIN_INTENSITY = 1;
    private static final int MAX_INTENSITY = 254;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeSensor();

        if (this.gateway == null) {
            initializeGateway();
        }

        initializeUiElements();
    }

    private void initializeUiElements() {
        // Config section
        TextView inputSetpoint = findViewById(R.id.inputSetpoint);
        inputSetpoint.setText(String.valueOf(this.setpoint));

        TextView inputSlack = findViewById(R.id.inputSlack);
        inputSlack.setText(String.valueOf(this.slack));

        // Seekbar section
        SeekBar brightnessBar = findViewById(R.id.intensitySeekbar);
        brightnessBar.setProgress(this.actualIntensity);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sendBrightnessLevel(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if (isRunning) {
                    btnStartStopHandler(null);
                }
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
        this.gateway = new Gateway(GATEWAY_IP);
        this.credentials = this.gateway.connect(GATEWAY_PASSPHRASE);

        this.lightBulb = gateway.getDevice(LIGHTBULB_ID).toLight();
        this.actualIntensity = this.lightBulb.getBrightness();
        this.lightBulb.setColourHex(ColourHex.WARM);
    }

    public void btnStartStopHandler(View view) {

        Button btnStartStop = findViewById(R.id.btnStartStop);
        EditText inputSlack = findViewById(R.id.inputSlack);
        TextView txtRunning = findViewById(R.id.txtRunningOut);

        // Update state variables
        this.setpoint = latestLux;
        desiredIntensity = actualIntensity;

        if (isRunning) {
            // Update state variables
            isRunning = false;

            // Disable and update UI elements
            btnStartStop.setText("Start");
            inputSlack.setEnabled(true);
            txtRunning.setText("False");

        } else {
            isRunning = true;

            // Update System Status View
            TextView inputSetpoint = findViewById(R.id.inputSetpoint);
            inputSetpoint.setText(String.valueOf(this.latestLux));
            txtRunning.setText("True");

            // Disable and update UI elements
            this.slack = Integer.parseInt(inputSlack.getText().toString());
            inputSlack.setEnabled(false);

            btnStartStop.setText("Stop");
        }


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float illuminance = event.values[0];
        this.latestLux = (int)illuminance;

        // Update lux in UI
        TextView txtIlluminance = findViewById(R.id.txtCurrentLuxOut);
        String unit = " lx";
        txtIlluminance.setText(illuminance + unit);

        // Calculate the Slack error
        TextView txtSlackErrorOut = findViewById(R.id.txtSlackErrorOut);
        txtSlackErrorOut.setText(Math.abs(setpoint - latestLux) + unit);

        // Run light adjustment
        if(this.isRunning) {
            adjustLightSource(latestLux, this.setpoint, this.slack);
        }
    }

    private void adjustLightSource(int lux, int setpoint, int setpointError) {
        int difference =lux - setpoint;

        int multiplier = 1;
        if (difference > setpointError) {
            if (this.desiredIntensity > this.MIN_INTENSITY) {
                this.desiredIntensity = desiredIntensity - this.RESOLUTION * multiplier;
                sendBrightnessLevel(this.desiredIntensity);
            }
        } else if (difference < setpointError * -1) {
            if (this.desiredIntensity < this.MAX_INTENSITY) {
                this.desiredIntensity = desiredIntensity + this.RESOLUTION *multiplier;
                sendBrightnessLevel(this.desiredIntensity);
            }
        }
    }

    private void sendBrightnessLevel(int newIntensity) {
        if(newIntensity < MIN_INTENSITY)
            newIntensity = MIN_INTENSITY;
        if(newIntensity > MAX_INTENSITY)
            newIntensity = MAX_INTENSITY;

        lightBulb.setBrightness(newIntensity);

        // Update UI element
        TextView txtIntensity = findViewById(R.id.txtIntensityOut);
        txtIntensity.setText(String.valueOf(newIntensity));

        TextView txtWattOut = findViewById(R.id.txtWattOut);
        txtWattOut.setText(String.valueOf(round(wattUsage(newIntensity), 2)));

        SeekBar intensitySeekBar = findViewById(R.id.intensitySeekbar);
        intensitySeekBar.setProgress(newIntensity, true);
    }

    /**
     * This method is based on a regression model made with a watt meter and an IKEA E27 1000 lumen light bulb
     * @param intensity Value between 0 and 254
     * @return the watt usage from 0.2 watt to 11.8 watt
     */
    private double wattUsage(int intensity) {
        if (intensity == 0){
            return 0.2;
        } else {
            return 0.737514880516844 * Math.exp(0.010723316391607 * intensity);
        }
    }

    /**
     * Returns the provided double value to the amount of decimals
     * @param d
     * @param decimals
     * @return
     */
    private double round(double d, int decimals) {
        return  Math.round(d * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
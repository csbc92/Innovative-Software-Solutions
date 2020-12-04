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
import static dk.sdu.filipswho.Utils.round;
import static dk.sdu.filipswho.Utils.wattUsage;

import nl.stijngroenen.tradfri.device.Gateway;
import nl.stijngroenen.tradfri.device.Light;
import nl.stijngroenen.tradfri.util.ColourHex;
import nl.stijngroenen.tradfri.util.Credentials;

public class MainActivity extends AppCompatActivity implements SensorEventListener, IntensityChangeListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    // Gateway variables
    private Gateway gateway = null;
    private Credentials credentials = null;
    private Light lightBulb = null;
    private static final int LIGHTBULB_ID = 65541;
    private static final String GATEWAY_IP = "192.168.1.100";
    private static final String GATEWAY_PASSPHRASE = "mbpRdLgXUNXBzS5M";

    private boolean isRunning = false;

    // Algorithm
    private LightAlgorithm algo;
    private Thread algoThread;
    private LightState lightState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAlgorithm();
        initializeSensor();
        initializeGateway();
        initializeUiElements();
        this.algo.addListener(new DataCollector());
    }

    private void initializeAlgorithm() {
        this.lightState = new LightState();
        this.algo = new LightAlgorithm(lightState);
        this.algo.addListener(this);
    }

    private void initializeUiElements() {
        // Config section
        TextView inputSetpoint = findViewById(R.id.inputSetpoint);
        inputSetpoint.setText(String.valueOf(this.lightState.getSetpoint()));

        TextView inputSlack = findViewById(R.id.inputSlack);
        inputSlack.setText(String.valueOf(this.lightState.getSlack()));

        // Seekbar section
        SeekBar brightnessBar = findViewById(R.id.intensitySeekbar);
        brightnessBar.setProgress(this.lightState.getIntensity());
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateIntensity(i);
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
        this.lightState.setIntensity(this.lightBulb.getBrightness());
        this.lightBulb.setColourHex(ColourHex.WARM);
    }

    public void btnStartStopHandler(View view) {

        Button btnStartStop = findViewById(R.id.btnStartStop);
        EditText inputSlack = findViewById(R.id.inputSlack);
        TextView txtRunning = findViewById(R.id.txtRunningOut);

        // Update state variables
        this.lightState.setSetpoint(this.lightState.getLux());

        if (isRunning) {
            // Update state variables
            isRunning = false;
            this.algoThread.interrupt();
            this.algoThread = null;

            // Disable and update UI elements
            btnStartStop.setText("Start");
            inputSlack.setEnabled(true);
            txtRunning.setText("False");

        } else {
            isRunning = true;
            this.algoThread = new Thread(this.algo);
            this.algoThread.start();

            // Update System Status View
            TextView inputSetpoint = findViewById(R.id.inputSetpoint);
            inputSetpoint.setText(String.valueOf(this.lightState.getLux()));
            txtRunning.setText("True");

            // Disable and update UI elements
            this.lightState.setSlack(Integer.parseInt(inputSlack.getText().toString()));
            inputSlack.setEnabled(false);

            btnStartStop.setText("Stop");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float illuminance = event.values[0];
        this.lightState.setLux((int)illuminance);

        // Update lux in UI
        TextView txtIlluminance = findViewById(R.id.txtCurrentLuxOut);
        String unit = " lx";
        txtIlluminance.setText(illuminance + unit);

        // Calculate the Slack error
        TextView txtSlackErrorOut = findViewById(R.id.txtSlackErrorOut);
        txtSlackErrorOut.setText(Math.abs(this.lightState.getSetpoint() - this.lightState.getLux()) + unit);
    }

    private void updateIntensity(int newIntensity) {
        if(newIntensity < this.lightState.getMinIntensity())
            newIntensity = this.lightState.getMinIntensity();
        if(newIntensity > this.lightState.getMaxIntensity())
            newIntensity = this.lightState.getMaxIntensity();

        lightBulb.setBrightness(newIntensity);
        this.lightState.setIntensity(newIntensity);

        // Update UI element
        TextView txtIntensity = findViewById(R.id.txtIntensityOut);
        txtIntensity.setText(String.valueOf(newIntensity));

        TextView txtWattOut = findViewById(R.id.txtWattOut);
        txtWattOut.setText(String.valueOf(round(wattUsage(newIntensity), 2)));

        SeekBar intensitySeekBar = findViewById(R.id.intensitySeekbar);
        intensitySeekBar.setProgress(newIntensity, true);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onIntensityChange(LightState state) {
        System.out.println("On intensity changed: " + state.getIntensity());

        runOnUiThread(() -> updateIntensity(state.getIntensity()));
    }
}
package dk.sdu.filipswho;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static dk.sdu.filipswho.Utils.round;
import static dk.sdu.filipswho.Utils.wattUsage;

public class DataCollector implements IntensityChangeListener {

    private int dataCounter = 0;

    public DataCollector() {
        String header = "Timestamp,Setpoint,Lux,Slack,Intensity,Counter,WattUsage\n";
        writeToFile(header);
    }

    @Override
    public void onIntensityChange(LightState state) {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSS"));
        int setpoint = state.getSetpoint();
        int lux = state.getLux();
        int slack = state.getSlack();
        int intensity = state.getIntensity();
        int counter = dataCounter++;
        double wattUsage = round(wattUsage(intensity), 2);

        String toWrite = date + "," +
                setpoint + "," +
                lux + "," +
                slack + "," +
                intensity + "," +
                counter + "," +
                wattUsage + "\n";

        writeToFile(toWrite);
    }

    private void writeToFile(String toWrite) {
        try {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            root.mkdirs();

            File file = new File(root, "LightWhalePrototypeData.csv");
            FileWriter writer = new FileWriter(file, true);
            writer.write(toWrite);
            writer.flush();
            writer.close();

            System.out.println("Wrote following to file: " + toWrite);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

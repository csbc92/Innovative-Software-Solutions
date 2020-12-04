package dk.sdu.filipswho;

/**
 * This class represents the light state of a light source and light sensor
 */
public class LightState {
    private int intensity; // The light source intensity
    private int resolution; // This is the resolution used to adjust the light source
    private int minIntensity; // The minimum allowed intensity of the light source
    private int maxIntensity; // The maximum allowed intensity of the light source

    private int setpoint; // A setpoint that represents the target lux level that we wish to maintain.
    private int slack; // Slack represents the margin on each side of the setpoint where the lux level is allowed to be without further adjustment.

    private int lux; // The current state of the ambient sensor

    public LightState() {
        this.intensity = 0;
        this.resolution = 1;
        this.minIntensity = 1;
        this.maxIntensity = 254;
        this.setpoint = 500;
        this.slack = 15;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getMinIntensity() {
        return minIntensity;
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity = minIntensity;
    }

    public int getMaxIntensity() {
        return maxIntensity;
    }

    public void setMaxIntensity(int maxIntensity) {
        this.maxIntensity = maxIntensity;
    }

    public int getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(int setpoint) {
        this.setpoint = setpoint;
    }

    public int getSlack() {
        return slack;
    }

    public void setSlack(int slack) {
        this.slack = slack;
    }

    public int getLux() {
        return lux;
    }

    public void setLux(int lux) {
        this.lux = lux;
    }
}

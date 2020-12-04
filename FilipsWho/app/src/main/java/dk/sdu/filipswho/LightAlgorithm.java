package dk.sdu.filipswho;

import java.util.HashSet;
import java.util.Set;

public class LightAlgorithm implements Runnable {

    private final int delay = 500;
    private final Set<IntensityChangeListener> listeners;

    private final LightState lightState;

    /**
     * Creates a new light algorithm.
     * @param lightState
     */
    public LightAlgorithm(LightState lightState) {
        this.lightState = lightState;
        this.listeners = new HashSet<>();
    }

    public boolean addListener(IntensityChangeListener listener) {
        return this.listeners.add(listener);
    }

    public boolean removeListener(IntensityChangeListener listener) {
        return this.listeners.remove(listener);
    }

    public LightState getLightState() {
        return this.lightState;
    }

    private void notifyListeners(int intensity) {
        for (IntensityChangeListener l : this.listeners) {
            l.onIntensityChange(lightState);
        }
    }

    private void adjustLightSource(int lux) {
        int difference = lux - this.lightState.getSetpoint();

        if (difference > this.lightState.getSlack()) {
            if (this.lightState.getIntensity() > this.lightState.getMinIntensity()) {
                this.lightState.setIntensity(this.lightState.getIntensity() - this.lightState.getResolution());
                notifyListeners(this.lightState.getIntensity());
            }
        } else if (difference < this.lightState.getSlack() * -1) {
            if (this.lightState.getIntensity() < this.lightState.getMaxIntensity()) {
                this.lightState.setIntensity(this.lightState.getIntensity() + this.lightState.getResolution());
                notifyListeners(this.lightState.getIntensity());
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                adjustLightSource(this.lightState.getLux());
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
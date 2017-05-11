package fr.aquarium;

public final class Threshold {
    private final int fishId;
    private final int sensorId;
    private final double min;
    private final double max;
    
    public Threshold(int fishId, int sensorId, double min, double max) {
        this.fishId = fishId;
        this.sensorId = sensorId;
        this.min = min;
        this.max = max;
    }

    public int getSensorId() {
        return sensorId;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}

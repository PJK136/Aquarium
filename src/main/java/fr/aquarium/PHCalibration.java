package fr.aquarium;

import java.util.Calendar;

public final class PHCalibration {
    private final int sensorId;
    private final Calendar date;
    private final int pH4;
    private final int pH7;

    public PHCalibration(int sensorId, Calendar date, int pH4, int pH7) {
        this.sensorId = sensorId;
        this.date = (Calendar) date.clone();
        this.pH4 = pH4;
        this.pH7 = pH7;
    }

    public int getSensorId() {
        return sensorId;
    }
    
    public Calendar getDate() {
        return (Calendar) date.clone();
    }

    public int getpH4() {
        return pH4;
    }

    public int getpH7() {
        return pH7;
    }
}

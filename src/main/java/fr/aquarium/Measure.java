package fr.aquarium;

import java.util.Calendar;

public final class Measure {
    final private int sensorId;
    final private Calendar date;
    final private double rawValue;
    final private double value;
    
    public Measure(int sensorId, Calendar date, double rawValue, double value) {
        this.sensorId = sensorId;
        this.date = (Calendar) date.clone();
        this.rawValue = rawValue;
        this.value = value;
    }

    public int getSensorId() {
        return sensorId;
    }

    public Calendar getDate() {
        return (Calendar) date.clone();
    }

    public double getRawValue() {
        return rawValue;
    }
        
    public double getValue() {
        return value;
    }
}

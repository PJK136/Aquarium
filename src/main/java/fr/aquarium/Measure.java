package fr.aquarium;

import java.util.Calendar;

public final class Measure {
    final private int sensorId;
    final private Calendar date;
    final private int rawValue;
    final private double value;
    
    public Measure(int sensorId, Calendar date, int rawValue, double value) {
        this.sensorId = sensorId;
        this.date = (Calendar) date.clone();
        this.rawValue = rawValue;
        this.value = value;
    }
    
    public String toString(){
        String s;
        s="ID du capteur: " + sensorId + "; Date mesure: " + date.toString() + "; Valeur binaire: " + rawValue + "; Valeur reelle: " + value;
        return s;
    }

    public int getSensorId() {
        return sensorId;
    }

    public Calendar getDate() {
        return (Calendar) date.clone();
    }

    public int getRawValue() {
        return rawValue;
    }
        
    public double getValue() {
        return value;
    }
}

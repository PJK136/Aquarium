package fr.aquarium;

public final class Sensor {
    private final int id;
    private final String name;
    private final String unit;

    public Sensor(int id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }
 }

package fr.aquarium;

import java.util.Collections;
import java.util.List;

final public class MeasureEvent {
    private final List<Measure> measures;
    
    public MeasureEvent(List<Measure> measures) {
        this.measures = Collections.unmodifiableList(measures);
    }
    
    public List<Measure> getMeasures() {
        return measures;
    }
}

package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor implements MeasureListener {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final int fishId;
    private final Database database;

    public Monitor(Database database, int FishId) {
        this.database = database;
        this.fishId=FishId;
    }
    
    @Override
    public void measureReceived(MeasureEvent event) {
        List<Measure> measures = event.getMeasures();
        Threshold threshold;
        Sensor sensor;
        for (Measure measure : measures){
            threshold = database.queryThreshold(this.fishId,measure.getSensorId());
            sensor = database.querySensor(measure.getSensorId());
            double value = measure.getValue();
            if (value>threshold.getMax()){
                logger.error("La donnée {} est trop haute : {} {}",sensor.getName(), value, sensor.getUnit());
            }
            if (value<threshold.getMin()){
                logger.error("La donnée {} est trop basse : {} {}",sensor.getName(), value, sensor.getUnit());
            }
        }
    }
}

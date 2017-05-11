package fr.aquarium;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor implements MeasureListener {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Database database;

    public Monitor(Database database) {
        this.database = database;
    }
    
    @Override
    public void measureReceived(MeasureEvent event) {
        //TODO
    }
}

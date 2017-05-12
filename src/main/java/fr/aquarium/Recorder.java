package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recorder implements MeasureListener {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Database database;

    public Recorder(Database database) {
        this.database = database;
    }
    
    @Override
    public void measureReceived(MeasureEvent event) {
        try {
            logger.info("Enregistrement de {} mesures", event.getMeasures().size());
            database.insertMeasures(event.getMeasures());
        } catch (SQLException ex) {
            logger.error("Insertion des mesures impossible", ex);
        }
    }
}

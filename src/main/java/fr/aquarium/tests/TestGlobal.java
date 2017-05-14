package fr.aquarium.tests;

import fr.aquarium.Database;
import fr.aquarium.Monitor;
import fr.aquarium.Receiver;
import fr.aquarium.Recorder;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGlobal {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) {
        try {
            //database = new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B");
            Database database = new Database("localhost", 3306, "Aquarium", "aquarium", "BocalBocal");
                
            Receiver receiver = new Receiver(database.queryLastPHCalibration());
            Recorder recorder = new Recorder(database);
            Monitor monitor = new Monitor(database, 1);
            
            receiver.addMeasureListener(recorder);
            receiver.addMeasureListener(monitor);

            receiver.run();
        } catch (SQLException ex) {
            logger.error("Impossible de se connecter à la BD", ex);
        } catch (IOException ex) {
            logger.error("Impossible d'ouvrir une communication avec Arduino", ex);
        }
    }
}

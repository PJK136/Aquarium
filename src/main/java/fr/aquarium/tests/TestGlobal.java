package fr.aquarium.tests;

import fr.aquarium.Database;
import fr.aquarium.Extractor;
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
            Database database = new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B");
            //Database database = new Database("localhost", 3306, "Aquarium", "aquarium", "BocalBocal");
                
            Receiver receiver = new Receiver(database, "COM11");
            //FakeReceiver receiver = new FakeReceiver();
            Recorder recorder = new Recorder(database);
            Monitor monitor = new Monitor(database, 8, "xxx.xxx@xxx.xxx", "xxx.xxx@xxx.xxx", "xxx.xxx@xxx.xxx");
            Extractor extractor = new Extractor(database, null, null, null);
            
            receiver.addMeasureListener(recorder);
            receiver.addMeasureListener(monitor);
            
            extractor.schedule(60000, 20000, true, true, false, false);

            receiver.run();
            
            extractor.cancel();
        } catch (SQLException ex) {
            logger.error("Impossible de se connecter à la BD", ex);
        } catch (IOException ex) {
            logger.error("Impossible d'ouvrir une communication avec Arduino", ex);
        }
    }
}

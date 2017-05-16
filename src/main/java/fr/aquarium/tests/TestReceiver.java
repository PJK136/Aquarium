package fr.aquarium.tests;

import fr.aquarium.Database;
import fr.aquarium.Receiver;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReceiver {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) {
        try {
            Receiver receiver = new Receiver(new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B"));
            receiver.run();
        } catch (IOException | SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
        }
    }
}

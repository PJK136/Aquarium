package fr.aquarium.tests;

import fr.aquarium.Database;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) {
        Database database = null;
        try {
            database = new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B");
        } catch (Exception ex) {
            logger.error("Impossible de se connecter à la BD", ex);
        }
    }
}

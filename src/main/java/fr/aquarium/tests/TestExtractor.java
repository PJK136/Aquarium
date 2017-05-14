package fr.aquarium.tests;

import fr.aquarium.Database;
import fr.aquarium.Extractor;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExtractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) {
        try {
            //database = new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B");
            Database database = new Database("localhost", 3306, "Aquarium", "aquarium", "BocalBocal");
                
            Extractor extractor = new Extractor(database);
            extractor.dumpToJSON(1000);
            extractor.dumpToCSV();
        } catch (SQLException ex) {
            logger.error("Impossible de se connecter à la BD", ex);
        }
    }
}

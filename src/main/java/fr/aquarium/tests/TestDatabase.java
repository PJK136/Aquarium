package fr.aquarium.tests;

import fr.aquarium.Database;
import fr.aquarium.Measure;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) {
        Database database = null;
        List<Measure> measures = new LinkedList<Measure>();
        try {
            database = new Database("PC-TP-MYSQL", 3306, "G221_B_BD1", "G221_B", "G221_B");
            measures = database.queryMeasures(1,new GregorianCalendar(2017, Calendar.MAY, 1), new GregorianCalendar(2017, Calendar.MAY, 17));
            System.out.println(measures);
        } catch (Exception ex) {
            logger.error("Impossible de se connecter à la BD", ex);
        }
    }
}
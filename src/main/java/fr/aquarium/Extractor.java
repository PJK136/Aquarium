package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.gson.Gson;

public class Extractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public final static String JSON_FILENAME = "data.js";
    
    private final Database database;

    public Extractor(Database database) {
        this.database = database;
    }
    
    private class JSONSensor {
        public String name;
        public List<List> data;
        
        public JSONSensor(String name) {
            this.name = name;
            this.data = new LinkedList<List>();
        }
    }
    
    public void dumpToJSON() {
        List<Sensor> sensors = database.querySensors();
        List<JSONSensor> jss = new ArrayList<JSONSensor>(sensors.size());
        for (Sensor sensor : sensors) {
            JSONSensor js = new JSONSensor(sensor.getName());
            List<Measure> measures = database.queryMeasures(sensor.getId(), new GregorianCalendar(2017, Calendar.JANUARY, 1), Calendar.getInstance());
            for (Measure measure : measures) {
                ArrayList al = new ArrayList(2);
                al.add(measure.getDate().getTimeInMillis());
                al.add(measure.getValue());
                js.data.add(al);
            }
            jss.add(js);
        }
        
        try (PrintWriter writer = new PrintWriter(JSON_FILENAME)) {

            writer.println("var MEASURE_SERIES = ");
            writer.println(new Gson().toJson(jss));
            writer.println(";");

            logger.info("Mesures enregistrées en JSON dans {}", JSON_FILENAME);
        } catch (FileNotFoundException ex) {
            logger.error("Impossible d'enregistrer les mesures JSON", ex);
        }
    }
    
    public void dumpToCSV() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String date = dateFormat.format(new Date());
        String filename = "aquarium_measures_" + date + ".csv";
            
        List<Measure> measures = database.queryMeasures(new GregorianCalendar(2017, Calendar.JANUARY, 1), Calendar.getInstance());
        
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("date;sensorId;value");
            
            for (Measure measure : measures) {
                writer.println(measure.getDate().getTimeInMillis()+";"+measure.getSensorId()+";"+measure.getValue());
            }
            
            logger.info("Mesures enregistrées en CSV dans {}", filename);
        } catch (FileNotFoundException ex) {
            logger.error("Impossible d'enregistrer les mesures CSV", ex);
        }
    }
}

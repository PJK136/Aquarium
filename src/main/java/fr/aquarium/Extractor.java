package fr.aquarium;

import java.lang.invoke.MethodHandles;
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
import java.util.Timer;
import java.util.TimerTask;

public class Extractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public final static String JSON_FILENAME = "data.js";
    
    private final Database database;
    
    private Timer timer;

    public Extractor(Database database) {
        this.database = database;
    }

    public void schedule(final int count) {
        if (timer != null)
            timer.cancel();
        
        timer = new Timer();
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dumpToJSON(count);
            }
        }, 0, 10000);
    }
    
    public void cancel() {
        if (timer != null)
            timer.cancel();
    }
    
    private class JSONSensor {
        public String name;
        public List<List> data;
        
        public JSONSensor(String name) {
            this.name = name;
            this.data = new LinkedList<List>();
        }
    }
    
    public void dumpToJSON(final int count) {
        dumpToJSON(JSON_FILENAME, new MeasureQuery() {
            @Override
            public List<Measure> query(int sensorId) {
                return database.queryLastMeasures(sensorId, count);
            }
        });
    }
    
    public void dumpToJSON(final int count, final int interval) {
        dumpToJSON(JSON_FILENAME, new MeasureQuery() {
            @Override
            public List<Measure> query(int sensorId) {
                return database.queryLastMeasuresByInterval(sensorId, count, interval);
            }
        });
    }
    
    private interface MeasureQuery {
        List<Measure> query(int sensorId);
    }
    
    private void dumpToJSON(String filename, MeasureQuery query) {
        List<Sensor> sensors = database.querySensors();
        List<JSONSensor> jss = new ArrayList<JSONSensor>(sensors.size());
        for (Sensor sensor : sensors) {
            JSONSensor js = new JSONSensor(sensor.getName());
            List<Measure> measures = query.query(sensor.getId());
            for (Measure measure : measures) {
                ArrayList al = new ArrayList(2);
                al.add(measure.getDate().getTimeInMillis());
                al.add(measure.getValue());
                js.data.add(al);
            }
            jss.add(js);
        }
        
        try (PrintWriter writer = new PrintWriter(filename)) {

            writer.println("var MEASURE_SERIES = ");
            writer.println(new Gson().toJson(jss));
            writer.println(";");

            logger.info("Mesures enregistrées en JSON dans {}", filename);
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

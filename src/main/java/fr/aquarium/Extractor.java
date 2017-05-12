package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.gson.Gson;

public class Extractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
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
    
    public void dumpToJSON(String filename) {
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
        
        try {
            PrintWriter writer = new PrintWriter(filename);

            writer.println("var MEASURE_SERIES = ");
            writer.println(new Gson().toJson(jss));
            writer.println(";");

            writer.close();

            logger.info("Mesures enregistrées dans {}", filename);
        } catch (FileNotFoundException ex) {
            logger.error("Impossible d'enregistrer les mesures", ex);
        }
    }
    
    public void dumpToCSV(Path path) {
        
        List<Measure> measures = database.queryMeasures(new GregorianCalendar(2017, Calendar.JANUARY, 1), Calendar.getInstance());
        try {
            SimpleDateFormat formatDatePourNomFichier = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String datePourNomFichier = formatDatePourNomFichier.format(new Date());
            String nomFichier = "aquarium_measures_" + datePourNomFichier + ".csv";
            
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nomFichier)));
            
            DecimalFormat formatNombreDecimal = new DecimalFormat("0.00");
            formatNombreDecimal.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ROOT));
            
            for(Measure measure : measures){
                writer.println(measure.getDate()+";"+measure.getSensorId()+";"+formatNombreDecimal.format(measure.getValue()));
            }            
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}

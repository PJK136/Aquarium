package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class Extractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Database database;

    public Extractor(Database database) {
        this.database = database;
    }
    
    public void dumpToJSON(Path path) {
        //Paul
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

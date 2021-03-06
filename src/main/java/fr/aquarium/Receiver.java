package fr.aquarium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final double PH4_REF = 4.01;
    private final double PH7_REF = 7.0;
        
    private final Database database;
    
    private final List<MeasureListener> listeners = new ArrayList<MeasureListener>();
    
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    private final PrintStream output = System.out;
    
    private ArduinoUsbChannel vcpChannel;
    
    private boolean stop = true;
    
    public Receiver(Database database)  throws IOException {
        this(database, null);
    }
    
    public Receiver(Database database, String port) throws IOException {
        while (port == null) {
            logger.info("Recherche d'un port disponible...");
            port = ArduinoUsbChannel.getOneComPort();

            if (port == null) {
                logger.warn("Aucun port disponible !");
                logger.info("Nouvel essai dans 5s");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    /*ex*/      // Ignorer l'exception
                }
            }
        }
        
        logger.info("Connection au port {}", port);
        
        vcpChannel = new ArduinoUsbChannel(port);
        
        logger.info("Connection réussie au port {}", port);
        
        this.database = database;
    }
    
    @Override
    public void run() {
        if (!this.stop) {
            logger.error("Receiver déjà en cours d'exécution");
            return;
        }
        
        this.stop = false;
        
        logger.info("Début de l'écoute Arduino");
        
        try {
            //Ouverture du canal de communication
            vcpChannel.open();
            
            //Lancement du thread de décodage
            Thread readingThread = new Thread(new ReadingTask());
            readingThread.start();
            
            Thread writingThread = new Thread(new WritingTask());
            writingThread.start();
            
            while (!stop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) { }
            }
                
            //Fin de la communication
            try {
                writingThread.interrupt();
                writingThread.join(1000);
            } catch (InterruptedException ex) {  }
            
            try {
                readingThread.interrupt();
                readingThread.join(1000);
            } catch (InterruptedException ex) {  }
            
            vcpChannel.close();
        } catch (IOException | SerialPortException ex) {
            logger.error(ex.getClass().getName(), ex);
        }
        
        logger.info("Fin de l'écoute Arduino");
        this.stop = true;
    }
    
    public void stop() {
        this.stop = true;
    }
    
    private String readLine(String header) throws IOException {
        output.print(header);
        output.flush();
        return input.readLine();
    }
    
    /**
     * Acquittement avec l'Arduino
     */
    private class WritingTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                try {
                    vcpChannel.getWriter().write('A'); // ACK
                } catch (IOException ex) {
                    logger.error(ex.getClass().getName(), ex);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) { }
            }

            try {
                vcpChannel.getWriter().write('B'); // Disconnect
            } catch (IOException ex) {
                logger.error(ex.getClass().getName(), ex);
            }  
        }
    }
        
    /**
     * Décodage des données reçues
     */
    private class ReadingTask implements Runnable {
        @Override
        public void run() {
            BufferedReader vcpInput = new BufferedReader(new InputStreamReader(vcpChannel.getReader()));

            String line;

            try {
                while (!stop) {
                    if ((line = vcpInput.readLine()) != null) {
                        logger.info("Data from Arduino : {}", line);
                        try {
                            if(line.startsWith("Measures : ")){
                                line = line.replaceAll("Measures : ", "");
                                line = line.replaceAll(" ","");
                                String[] tempo;
                                tempo = line.split(",");
                                Calendar date = Calendar.getInstance();
                                date.add(Calendar.MILLISECOND, (-1)*Integer.parseInt(tempo[0]));
                                String[] tempo2;
                                List<Measure> measures = new LinkedList<Measure>();
                                for(int i=1; i<tempo.length; i++) {
                                    tempo2=tempo[i].split(":");
                                    if (tempo2.length == 2)
                                        measures.add(new Measure(Integer.parseInt(tempo2[0]),date,Integer.parseInt(tempo2[1]),computeRealValue(Integer.parseInt(tempo2[0]),Integer.parseInt(tempo2[1]))));
                                    else
                                        logger.warn("Unknown data !");
                                }
                                sendMeasures(measures);
                            } else if (line.startsWith("PH Calib :")) {
                                line = line.replaceAll("PH Calib :", "");
                                line = line.replaceAll(" ","");
                                String[] data;
                                data = line.split(",");
                                if (data.length == 4) {
                                    Calendar date = Calendar.getInstance();
                                    date.add(Calendar.MILLISECOND, (-1)*Integer.parseInt(data[0]));
                                    sendPHCalibration(new PHCalibration(Integer.parseInt(data[1]), date, Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                                } else
                                    logger.warn("Unknown data !");
                            }
                        } catch (NumberFormatException ex) {
                            logger.error(ex.getClass().getName(), ex);
                        }
                    }
                }
            } catch (IOException ex) {
                //logger.error(ex.getClass().getName(), ex);
            }
        }
    }
    
    /**
     * Calcule la valeur réelle à partir de la valeur brute pour un capteur donné
     * @param sensorId Identifiant du capteur
     * @param rawValue Valeur brute
     * @return Valeur réelle
     */
    private double computeRealValue(int sensorId, int rawValue) {
        switch (sensorId) {
            case 1: //Capteur luminosité P
                return 1023-rawValue;
            case 2: //Capteur pH
                PHCalibration pHCalibration = database.queryLastPHCalibration(sensorId);
                if (pHCalibration == null) {
                    logger.error("Capteur de pH non calibré ! Utilisation de la valeur 7 par défaut.");
                    return 7.0;
                }
                
                double m = (PH7_REF-PH4_REF)/(pHCalibration.getpH7()-pHCalibration.getpH4()); // (b-a)/(B-A)
                double p = (PH4_REF*pHCalibration.getpH7()-pHCalibration.getpH4()*PH7_REF)/(pHCalibration.getpH7()-pHCalibration.getpH4()); //(aB-Ab)/(B-A)
                double value = m*rawValue + p; // f(x) = mx + p
                if (value == Double.NaN || value < 0 || value > 14) {
                    logger.error("Capteur de pH mal calibré : {} ! Utilisation de la valeur 7 par défaut.", value);
                    return 7.0;
                }
                
                return value;
            case 3: //Capteur débit
                return rawValue*60/5.5/5;
            case 4: //Capteur niveau
                return 1023-rawValue;
            case 5: //Capteur température
                return rawValue/16.;
            case 6: //Capteur luminosité S
                return 1023-rawValue;
            default:
                logger.warn("Capteur {} inconnu, utilisation de la valeur brute : {}", sensorId, rawValue);
                return rawValue;
        }
    }
    
    public void addMeasureListener(MeasureListener listener) {
        listeners.add(listener);
    }
    
    public void removeMeasureListener(MeasureListener listener) {
        listeners.remove(listener);
    }
    
    public void removeAllMeasureListener() {
        listeners.clear();
    }
    
    public void sendMeasures(List<Measure> measures) {
        MeasureEvent event = new MeasureEvent(measures);
        for (MeasureListener listener : listeners) {
            listener.measureReceived(event);
        }
    }
    
    public void sendPHCalibration(PHCalibration calibration) {
        PHCalibrationEvent event = new PHCalibrationEvent(calibration);
        for (MeasureListener listener : listeners) {
            listener.pHCalibrationReceived(event);
        }
    }
}

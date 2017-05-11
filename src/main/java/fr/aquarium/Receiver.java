package fr.aquarium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<MeasureListener> listeners = new ArrayList<MeasureListener>();
    
    public final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    public final PrintStream output = System.out;
    
    private ArduinoUsbChannel vcpChannel;
    
    public Receiver() throws IOException {
        logger.info("Début de l'écoute Arduino");
        
        String port = null;
        
        //do {
        
            logger.info("Recherche d'un port disponible...");
            port = ArduinoUsbChannel.getOneComPort();
            
            if (port == null) {
                //logger.warn("Aucun port disponible !");
                throw new IOException("Aucun port disponible !");
                /*logger.info("Nouvel essai dans 5s");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    // Ignorer l'exception
                }*/
            }

        //} while (port == null);
        
        port = "COM9";
        
        logger.info("Connection au Port {}", port);
        
        vcpChannel = new ArduinoUsbChannel(port);
    }
    
    @Override
    public void run() {
        try {
            //Lancement du thread de décodage
            Thread readingThread = new Thread(new ReadingTask());
            readingThread.start();
            
            //Ouverture du canal de communication
            vcpChannel.open();
            
            boolean exit = false;
            
            //Partie communication PC ---> Arduino
            while (!exit) {
                String line = readLine("Envoyer une ligne (ou 'fin') > ");
            
                if (line.length() == 0) {
                    continue;
                }
                
                if ("fin".equals(line)) {
                    exit = true;
                    continue;
                }
                
                vcpChannel.getWriter().write(line.getBytes("UTF-8"));
                vcpChannel.getWriter().write('\n');
            }
            
            //Fin de la communication
            
            vcpChannel.close();
            
            readingThread.interrupt();
            try {
                readingThread.join(1000);
            } catch (InterruptedException ex) {
                logger.error(ex.getClass().getName(), ex);
            }
        } catch (IOException | SerialPortException ex) {
            logger.error(ex.getClass().getName(), ex);
        }
    }
    
    private String readLine(String header) throws IOException {
        output.print(header);
        output.flush();
        return input.readLine();
    }
    
    /**
     * Décodage des données reçues
     */
    private class ReadingTask implements Runnable {
        @Override
        public void run() {
            BufferedReader vcpInput = new BufferedReader(new InputStreamReader(vcpChannel.getReader()));

            String line;
            //Calendar dateMeasure=getInstance(); // A VERIFIER

            try {
                while ((line = vcpInput.readLine()) != null) {
                    logger.info("Data from Arduino : {}", line);
                    int[] measuresValues = new int[8];
                    int i=0;
                    int j=0;
                    line.replaceAll(" ","");
                    String[] tempo;
                    tempo = line.split(",");

                    //measuresValues[0] = 

                    /* Votre but : envoyer toutes les mesures reçues avec sendMeasures(measures);
                    List<Measure> measures = new LinkedList<Measure>();
                    
                    Pour chaque mesure de chaque capteur :
                    double value = computeRealValue(sensorId, rawValue);
                    measures.add(new Measure(sensorId, date, rawValue, value));
                    
                    À la fin :
                    sendMeasures(measures);
                    */
                }
            } catch (IOException ex) {
                logger.error(ex.getClass().getName(), ex);
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
            //TODO
            
            /* Exemple :
            case 1: //Capteur de température par exemple
                return rawValue/16.;
            case 2: //Autre capteur avec id 2
                return 3.*rawValue/4.;
            */
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
    
    public void sendMeasures(List<Measure> measures) {
        MeasureEvent event = new MeasureEvent(measures);
        for (MeasureListener listener : listeners) {
            listener.measureReceived(event);
        }
    }
}

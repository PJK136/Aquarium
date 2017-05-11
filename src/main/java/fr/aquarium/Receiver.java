package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    List<MeasureListener> listeners;
    
    public Receiver() {
        listeners = new ArrayList<MeasureListener>();
    }
    
    @Override
    public void run() {
        //TODO
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
    
    public void emitMeasureEvent(MeasureEvent event) {
        for (MeasureListener listener : listeners) {
            listener.measureReceived(event);
        }
    }
}

package fr.aquarium.tests;

import fr.aquarium.Measure;
import fr.aquarium.MeasureEvent;
import fr.aquarium.MeasureListener;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Timer;
import java.util.TimerTask;

public class FakeReceiver implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<MeasureListener> listeners = new ArrayList<MeasureListener>();
    Timer timer;
    
    
    public FakeReceiver() throws IOException {
        
    }
    
    @Override
    public void run() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<Measure> measures = new LinkedList<>();
                measures.add(new Measure(1,Calendar.getInstance(),(int)(Math.random()*1023),(Math.random()*1023)));
                measures.add(new Measure(2,Calendar.getInstance(),(int)(Math.random()*1023),(Math.random()*1023)));
                measures.add(new Measure(3,Calendar.getInstance(),(int)(Math.random()*1023),(Math.random()*1023)));
                measures.add(new Measure(4,Calendar.getInstance(),(int)(Math.random()*1023),(Math.random()*1023)));
                measures.add(new Measure(5,Calendar.getInstance(),(int)(Math.random()*1023),(Math.random()*1023)));
                sendMeasures(measures);
            }
        }, 0, 10000);
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

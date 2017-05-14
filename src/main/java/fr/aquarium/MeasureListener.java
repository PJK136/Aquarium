package fr.aquarium;

public interface MeasureListener {
    void measureReceived(MeasureEvent event);
    
    void pHCalibrationReceived(PHCalibrationEvent event);
}

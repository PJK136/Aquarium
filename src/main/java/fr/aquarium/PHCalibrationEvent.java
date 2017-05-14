package fr.aquarium;

public final class PHCalibrationEvent {
    private final PHCalibration calibration;

    public PHCalibrationEvent(PHCalibration calibration) {
        this.calibration = calibration;
    }

    public PHCalibration getCalibration() {
        return calibration;
    }
}

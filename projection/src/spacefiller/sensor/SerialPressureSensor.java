package spacefiller.sensor;

import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class SerialPressureSensor extends Sensor {
  private boolean thresholdPassed;
  private float sensitivity = 5;

  public SerialPressureSensor() {

  }

  public float getSensitivity() {
    return sensitivity;
  }

  public void setSensitivity(float sensitivity) {
    this.sensitivity = sensitivity;
  }

  protected void setSensorValue(int value) {
    thresholdPassed = value > sensitivity;
  }

  @Override
  public boolean isDepressed() {
    return thresholdPassed;
  }
}

package spacefiller.sensor;

import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class SerialPressureSensor extends Sensor {
  private boolean thresholdPassed;

  public SerialPressureSensor() {

  }

  protected void setSensorValue(int value) {
    thresholdPassed = value > 5;
  }

  @Override
  public boolean isDepressed() {
    return thresholdPassed;
  }
}

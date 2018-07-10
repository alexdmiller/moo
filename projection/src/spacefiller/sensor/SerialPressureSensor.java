package spacefiller.sensor;

import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class SerialPressureSensor extends Sensor {
  private float sensitivity = 5;
  private int value;

  public SerialPressureSensor(int historyLength) {
    super(historyLength);
  }

  public float getSensitivity() {
    return sensitivity;
  }

  public void setSensitivity(float sensitivity) {
    this.sensitivity = sensitivity;
  }

  protected void setSensorValue(int value) {
    this.value = value;
    setSensorState(value > sensitivity);
  }

  public int getValue() {
    return value;
  }
}

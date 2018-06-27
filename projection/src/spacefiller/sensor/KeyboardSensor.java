package spacefiller.sensor;

import processing.core.PApplet;
import processing.event.KeyEvent;

import static processing.core.PApplet.println;

public class KeyboardSensor extends Sensor {
  private PApplet parent;
  private char key;

  public KeyboardSensor(PApplet parent, char key) {
    this.parent = parent;
    this.key = key;
    this.parent.registerMethod("keyEvent", this);
  }

  public void keyEvent(KeyEvent event) {
    if (event.getKey() == key) {
      setSensorState(event.getAction() == KeyEvent.PRESS || event.getAction() == KeyEvent.TYPE);
    }
  }
}

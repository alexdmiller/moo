package spacefiller.sensor;

import processing.core.PApplet;
import processing.event.KeyEvent;

import static processing.core.PApplet.println;

public class KeyboardSensor extends Sensor {
  private PApplet parent;
  private char key;
  private boolean keyDown;

  public KeyboardSensor(PApplet parent, char key, int historyLength) {
    super(historyLength);
    this.parent = parent;
    this.key = key;
    this.parent.registerMethod("draw", this);
    this.parent.registerMethod("keyEvent", this);
  }

  public void draw() {
    setSensorState(keyDown);
  }

  public void keyEvent(KeyEvent event) {
    if (event.getKey() == key) {
      if (event.getAction() == KeyEvent.PRESS) {
        keyDown = true;
      } else if (event.getAction() == KeyEvent.RELEASE) {
        keyDown = false;
      }
    }
  }
}

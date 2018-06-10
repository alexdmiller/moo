package spacefiller.sensor;

import processing.core.PApplet;
import processing.event.KeyEvent;

import static processing.core.PApplet.println;

public class KeyboardSensor implements Sensor {
  private PApplet parent;
  private char key;
  private boolean keyDepressed;

  public KeyboardSensor(PApplet parent, char key) {
    this.parent = parent;
    this.key = key;
    this.parent.registerMethod("keyEvent", this);
  }

  public void keyEvent(KeyEvent event) {
    if (event.getKeyCode() == 32) {
      keyDepressed = event.getAction() == KeyEvent.PRESS;
    }
  }


  @Override
  public boolean isDepressed() {
    return keyDepressed;
  }
}

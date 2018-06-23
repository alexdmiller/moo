package spacefiller.sensor;

import processing.core.PVector;

public abstract class Sensor {
  private boolean lastDepressedValue = false;
  private PVector position;

  public Sensor() {
    this.position = new PVector();
  }

  public PVector getPosition() {
    return position;
  }

  public void setPosition(PVector position) {
    this.position = position;
  }

  public void setPosition(float x, float y) {
    this.position.set(x, y);
  }

  public abstract boolean isDepressed();

  public boolean checkDepressed() {
    if (!lastDepressedValue && isDepressed()) {
      lastDepressedValue = true;
      return true;
    } else if (!isDepressed()) {
      lastDepressedValue = false;
    }
    return false;
  }


}

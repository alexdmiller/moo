package spacefiller.sensor;

import de.looksgood.ani.Ani;
import processing.core.PVector;

public abstract class Sensor {
  private boolean lastValue = false;
  private boolean checkDownDirty = false;
  private boolean checkUpDirty = false;
  private PVector position;

  private float smoothedValue;

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

  public boolean isDepressed() {
    return lastValue;
  }

  public boolean checkDown() {
    boolean value = checkDownDirty && lastValue;
    checkDownDirty = false;
    return value;
  }

  public boolean checkUp() {
    boolean value = checkUpDirty && !lastValue;
    checkUpDirty = false;
    return value;
  }

  public float getSmoothedValue() {
    return smoothedValue;
  }

  protected void setSensorState(boolean down) {
    if (lastValue != down) {
      checkDownDirty = true;
      checkUpDirty = true;
      Ani.to(this, 2, "smoothedValue", down ? 0 : 1);
    }
    lastValue = down;
  }
}

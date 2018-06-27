package spacefiller.sensor;

import de.looksgood.ani.Ani;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PVector;

public abstract class Sensor {
  private boolean lastValue = false;
  private boolean checkDownDirty = false;
  private boolean checkUpDirty = false;
  private PVector position;
  private RShape associatedShape;
  private float smoothedValue;

  public void setAssociatedShape(RShape associatedShape) {
    this.associatedShape = associatedShape;
    RPoint center = associatedShape.getCentroid();
    this.position = new PVector(center.x, center.y);
  }

  public RShape getAssociatedShape() {
    return associatedShape;
  }

  public PVector getPosition() {
    return position;
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

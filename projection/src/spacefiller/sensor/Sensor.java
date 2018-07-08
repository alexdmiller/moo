package spacefiller.sensor;

import de.looksgood.ani.Ani;
import geomerative.RPath;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PVector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Sensor {
  private Queue<Boolean> lastValues;
  private boolean lastValue = false;
  private boolean checkDownDirty = false;
  private boolean checkUpDirty = false;
  private PVector position;
  private RShape associatedShape;
  private float smoothedValue;
  private int historySize = 0;

  public Sensor(int historyLength) {
    this.historySize = historyLength;
    this.lastValues = new LinkedList<>();
  }

  public void setAssociatedShape(RShape associatedShape) {
    this.associatedShape = associatedShape;
    recomputePosition();
  }

  public RShape getAssociatedShape() {
    return associatedShape;
  }

  public void recomputePosition() {
    RPoint center = associatedShape.getCentroid();
    this.position = new PVector(center.x, center.y);
  }

  public PVector getPosition() {
    return position;
  }

  public boolean isDepressed() {
    return lastValue;
  }

  private float valueAverage() {
    int count = 0;
    for (Boolean value : lastValues) {
      count += value ? 1 : 0;
    }
    return (float) count / lastValues.size();
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
    System.out.println(down);

    lastValues.add(down);

    if (lastValues.size() > historySize) {
      lastValues.poll();
    }

    if (valueAverage() == 1 && !lastValue) {
      checkDownDirty = true;
      Ani.to(this, 2, "smoothedValue", 0);
      lastValue = true;
    } else if (valueAverage() == 0 && lastValue) {
      checkUpDirty = true;
      Ani.to(this, 2, "smoothedValue", 1);
      lastValue = false;
    }
  }
}

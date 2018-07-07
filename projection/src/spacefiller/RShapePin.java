package spacefiller;

import geomerative.RPoint;
import processing.core.PVector;

import java.io.Serializable;

public class RShapePin implements Draggable, Serializable {
  private PVector originalPosition;
  private PVector position;
  private RShapeTransformer parent;

  public RShapePin(RPoint point, RShapeTransformer parent) {
    this.originalPosition = new PVector(point.x, point.y);
    this.position = new PVector(point.x, point.y);
    this.parent = parent;
  }

  public RShapePin(PVector position) {
    this.position = position;
  }

  @Override
  public void moveTo(float x, float y) {
    this.position.set(x, y);
    this.parent.computeWarp();
  }

  public void setPosition(float x, float y) {
    this.position.set(x, y);
  }

  public PVector getOriginalPosition() {
    return originalPosition;
  }

  public PVector getPosition() {
    return position;
  }

  public void translatePosition(float x, float y) {
    this.position.add(x, y);
  }
}

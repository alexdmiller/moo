package spacefiller;

import geomerative.RPoint;
import processing.core.PVector;

public class RShapePin implements Draggable {
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

  public PVector getOriginalPosition() {
    return originalPosition;
  }

  public PVector getPosition() {
    return position;
  }
}

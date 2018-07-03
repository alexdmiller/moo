package spacefiller;

import geomerative.RPoint;
import processing.core.PVector;

public class RShapePin implements Draggable {
  private PVector position;

  public RShapePin(RPoint point) {
    this.position = new PVector(point.x, point.y);
  }

  public RShapePin(PVector position) {
    this.position = position;
  }

  @Override
  public void moveTo(float x, float y) {
    this.position.set(x, y);
  }

  public PVector getPosition() {
    return position;
  }
}

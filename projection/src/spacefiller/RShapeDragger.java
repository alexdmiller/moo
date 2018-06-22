package spacefiller;

import geomerative.RShape;
import processing.core.PVector;
import spacefiller.Draggable;

public class RShapeDragger implements Draggable {
  private RShape shape;
  private PVector point;
  private float lastX;
  private float lastY;

  public RShapeDragger(RShape shape, PVector point) {
    this.shape = shape;
    this.point = point;
    this.lastX = point.x;
    this.lastY = point.y;
  }

  @Override
  public void moveTo(float x, float y) {
    shape.translate(x - lastX, y - lastY);
    lastX = x;
    lastY = y;
  }
}

package spacefiller;

import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PVector;

public class RShapeTransformer implements Transformable {
  private RShape shape;

  public RShapeTransformer(RShape shape) {
    this.shape = shape;
  }


  @Override
  public void scale(float scale) {
    RPoint center = shape.getCenter();
    shape.scale(scale, center.x, center.y);
  }

  @Override
  public void rotate(float theta) {
    RPoint center = shape.getCenter();
    shape.rotate(theta, center.x, center.y);
  }

  @Override
  public Draggable select(PVector point) {
    if (shape.contains(point.x, point.y)) {
      return new RShapeDragger(shape, point);
    }
    return null;
  }

  @Override
  public PVector getCenter() {
    RPoint p = shape.getCenter();
    return new PVector(p.x, p.y);
  }
}

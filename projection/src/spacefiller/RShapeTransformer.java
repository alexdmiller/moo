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
  public void scale(PVector origin, float scale) {
    shape.scale(scale, origin.x, origin.y);
  }

  @Override
  public void rotate(PVector origin, float theta) {
    shape.rotate(theta, origin.x, origin.y);
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

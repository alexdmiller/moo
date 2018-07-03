package spacefiller;

import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PGraphics;
import processing.core.PVector;

public class RShapeTransformer implements Transformable {
  private RShape shape;
  private CornerPinSurface parentSurface;

  private RShapePin topLeft;
  private RShapePin topRight;
  private RShapePin bottomLeft;
  private RShapePin bottomRight;

  private RShapePin[] pins;

  public RShapeTransformer(RShape shape, CornerPinSurface parentSurface) {
    this.shape = shape;
    this.parentSurface = parentSurface;

    topLeft = new RShapePin(shape.getTopLeft());
    topRight = new RShapePin(shape.getTopRight());
    bottomLeft = new RShapePin(shape.getBottomLeft());
    bottomRight = new RShapePin(shape.getBottomRight());

    pins = new RShapePin[] {topLeft, topRight, bottomLeft, bottomRight};
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

  @Override
  public PVector getRelativePoint(PVector point) {
    return parentSurface.getTransformedCursor(point.x, point.y);
  }

  public void renderControlPoints(PGraphics graphics, PGraphics canvas) {
    for (RShapePin pin : pins) {
      canvas.fill(255);
      canvas.ellipse(pin.getPosition().x, pin.getPosition().y, 30, 30);
    }
  }
}

package spacefiller;

import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PGraphics;
import processing.core.PVector;

import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class RShapeTransformer implements Draggable, Transformable {
  private RShape shape;
  private RShape originalShape;

  private CornerPinSurface parentSurface;

  private RShapePin topLeft;
  private RShapePin topRight;
  private RShapePin bottomLeft;
  private RShapePin bottomRight;

  private float lastX;
  private float lastY;

  private RShapePin[] pins;

  public RShapeTransformer(RShape shape, CornerPinSurface parentSurface) {
    this.shape = shape;
    this.originalShape = new RShape(shape);

    this.parentSurface = parentSurface;

    topLeft = new RShapePin(shape.getTopLeft(), this);
    topRight = new RShapePin(shape.getTopRight(), this);
    bottomLeft = new RShapePin(shape.getBottomLeft(), this);
    bottomRight = new RShapePin(shape.getBottomRight(), this);

    pins = new RShapePin[] {topLeft, topRight, bottomRight, bottomLeft};
  }

  @Override
  public void scale(float scale) {
    RPoint center = shape.getCenter();
    shape.scale(scale, center.x, center.y);

    for (RShapePin pin : pins) {
      pin.translatePosition(-center.x, -center.y);
      PVector p = pin.getPosition();
      float newX = p.x * scale;
      float newY = p.y * scale;
      p.x = newX;
      p.y = newY;
      pin.translatePosition(center.x, center.y);
    }
  }

  @Override
  public void rotate(float theta) {
    RPoint center = shape.getCenter();
    shape.rotate(theta, center.x, center.y);

    for (RShapePin pin : pins) {
      pin.translatePosition(-center.x, -center.y);
      PVector p = pin.getPosition();
      float newX = (float) (p.x * Math.cos(theta) - p.y * Math.sin(theta));
      float newY = (float) (p.x * Math.sin(theta) + p.y * Math.cos(theta));
      p.x = newX;
      p.y = newY;
      pin.translatePosition(center.x, center.y);
    }
  }

  @Override
  public Draggable select(PVector point) {
    for (RShapePin pin : pins) {
      if (pin.getPosition().dist(point) < 30) {
        return pin;
      }
    }

    if (shape.contains(point.x, point.y)) {
      lastX = point.x;
      lastY = point.y;
      return this;
    }

    return null;
  }

  @Override
  public Draggable selectClosestPin(PVector point) {
    Draggable closest = null;

    for (RShapePin pin : pins) {
      if (closest == null || pin.getPosition().dist(point) < closest.getPosition().dist(point)) {
        closest = pin;
      }
    }

    return closest;
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
    for (int i = 0; i < pins.length; i++) {
      RShapePin pin = pins[i];
      canvas.fill(255);
      canvas.ellipse(pin.getPosition().x, pin.getPosition().y, 30, 30);

      RShapePin pin2 = pins[(i + 1) % pins.length];
      canvas.stroke(255);
      canvas.strokeWeight(2);
      canvas.line(pin.getPosition().x, pin.getPosition().y, pin2.getPosition().x, pin2.getPosition().y);
    }
  }

  @Override
  public PVector[] getControlPoints() {
    PVector[] points = new PVector[4];
    for (int i = 0; i < 4; i++) {
      points[i] = new PVector(pins[i].getPosition().x, pins[i].getPosition().y);
    }
    return points;
  }

  @Override
  public void setControlPoints(PVector[] points) {
    for (int i = 0; i < 4; i++) {
      pins[i].setPosition(points[i].x, points[i].y);
    }

    computeWarp();
  }

  protected void computeWarp() {
    float w = originalShape.getWidth();
    float h = originalShape.getHeight();

    PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(
        topLeft.getOriginalPosition().x, topLeft.getOriginalPosition().y,
        topRight.getOriginalPosition().x, topRight.getOriginalPosition().y,
        bottomRight.getOriginalPosition().x, bottomRight.getOriginalPosition().y,
        bottomLeft.getOriginalPosition().x, bottomLeft.getOriginalPosition().y,
        topLeft.getPosition().x, topLeft.getPosition().y,
        topRight.getPosition().x, topRight.getPosition().y,
        bottomRight.getPosition().x, bottomRight.getPosition().y,
        bottomLeft.getPosition().x, bottomLeft.getPosition().y);

    WarpPerspective warpPerspective = new WarpPerspective(transform);

    RPoint[] originalShapeHandles = originalShape.getHandles();
    RPoint[] shapeHandles = shape.getHandles();

    for (int i = 0; i < shape.getHandles().length; i++) {
      Point2D point = warpPerspective.mapDestPoint(new Point((int) originalShapeHandles[i].x, (int) originalShapeHandles[i].y));
      shapeHandles[i].x = (float) point.getX();
      shapeHandles[i].y = (float) point.getY();
    }
  }

  @Override
  public void moveTo(float x, float y) {
    shape.translate(x - lastX, y - lastY);

    for (RShapePin pin : pins) {
      pin.translatePosition(x - lastX, y - lastY);
    }

    lastX = x;
    lastY = y;
  }

  @Override
  public PVector getPosition() {
    return new PVector(shape.getX(), shape.getY());
  }

  public RShape getShape() {
    return shape;
  }
}

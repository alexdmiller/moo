package spacefiller.modes;

import geomerative.RShape;
import processing.core.PGraphics;
import spacefiller.MooYoung;
import spacefiller.RShapeTransformer;
import spacefiller.sensor.Sensor;

public class EditMode extends Mode {
  public EditMode(MooYoung mooYoung) {
    super(mooYoung);
  }

  @Override
  final public void draw() {
    PGraphics canvas = mooYoung.getCanvas();
    PGraphics graphics = mooYoung.getGraphics();

    canvas.beginDraw();
    canvas.clear();
    canvas.stroke(255);
    canvas.strokeWeight(3);
    canvas.noFill();

    RShape selectedShape = null;
    if (mooYoung.getTransformTarget() instanceof RShapeTransformer) {
      RShapeTransformer transformer = (RShapeTransformer) mooYoung.getTransformTarget();
      selectedShape = transformer.getShape();
    }

    for (RShape shape : mooYoung.getShapes()) {
      if (selectedShape != null && selectedShape == shape) {
        canvas.stroke(255, 0, 0);
      } else {
        canvas.stroke(255);
      }
      shape.draw(canvas);
    }

    canvas.stroke(255);
    for (Sensor sensor : mooYoung.getSensors()) {
      sensor.recomputePosition();
      if (sensor.isDepressed()) {
        canvas.fill(255, 0, 0);
      } else {
        canvas.fill(0);
      }
      canvas.ellipse(sensor.getPosition().x, sensor.getPosition().y, 50, 50);
    }

    drawEditingUI();

    canvas.endDraw();

    mooYoung.getCornerPinSurface().render(graphics, canvas, true);
  }

  protected void drawEditingUI() { }
}

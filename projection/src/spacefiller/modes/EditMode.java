package spacefiller.modes;

import geomerative.RShape;
import processing.core.PGraphics;
import spacefiller.MooYoung;
import spacefiller.RShapeTransformer;
import spacefiller.sensor.Sensor;
import spacefiller.sensor.SerialPressureSensor;

public class EditMode extends Mode {
  public EditMode(MooYoung mooYoung) {
    super(mooYoung);

    mooYoung.cursor();
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
      canvas.pushMatrix();
      canvas.stroke(255);
      canvas.strokeWeight(1);
      sensor.recomputePosition();

      if (sensor.isDepressed()) {
        canvas.fill(255, 0, 0);
      } else {
        canvas.fill(0);
      }
      canvas.translate(sensor.getPosition().x, sensor.getPosition().y);
      canvas.ellipse(0, 0, 50, 50);

      if (sensor instanceof SerialPressureSensor) {
        SerialPressureSensor serialSensor = (SerialPressureSensor) sensor;
        canvas.noFill();
        canvas.stroke(1);
        canvas.strokeWeight(1);
        canvas.rect(0, 50, 100, 20);

        canvas.fill(255);
        canvas.rect(0, 50, serialSensor.getValue() / 10f * 100, 20);

        canvas.textSize(30);
        canvas.text(serialSensor.getValue(), -15,  15);
      }

      canvas.popMatrix();
    }

    drawEditingUI();

    canvas.endDraw();

    mooYoung.getCornerPinSurface().render(graphics, canvas, true);
  }

  protected void drawEditingUI() { }
}

package spacefiller.modes;

import geomerative.RShape;
import processing.core.PGraphics;
import spacefiller.MooYoung2;
import spacefiller.Transformable;
import spacefiller.sensor.Sensor;

public class EditMode extends Mode {
  public EditMode(MooYoung2 mooYoung) {
    super(mooYoung);
  }

  @Override
  final public void draw() {
    PGraphics canvas = mooYoung.getCanvas();
    PGraphics graphics = mooYoung.getGraphics();

    canvas.beginDraw();
    canvas.clear();
    canvas.stroke(255);
    canvas.strokeWeight(1);
    canvas.noFill();

    for (RShape shape : mooYoung.getShapes()) {
      shape.draw(canvas);
    }

    canvas.stroke(255);
    for (Sensor sensor : mooYoung.getSensors()) {
      if (sensor.isDepressed()) {
        canvas.fill(255, 0, 0);
      } else {
        canvas.fill(0);
      }
      canvas.ellipse(sensor.getPosition().x, sensor.getPosition().y, 50, 50);
    }
    canvas.endDraw();

    drawEditingUI();

    mooYoung.getCornerPinSurface().render(graphics, canvas, true);
  }

  protected void drawEditingUI() { }
}

package spacefiller.modes;

import geomerative.RShape;
import processing.core.PGraphics;
import spacefiller.MooYoung;
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

    drawEditingUI();

    canvas.endDraw();

    mooYoung.getCornerPinSurface().render(graphics, canvas, true);
  }

  protected void drawEditingUI() { }
}

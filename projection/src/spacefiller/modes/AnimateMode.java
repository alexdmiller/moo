package spacefiller.modes;

import de.looksgood.ani.Ani;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import spacefiller.MooYoung;
import spacefiller.Ripple;
import spacefiller.sensor.Sensor;
import toxi.color.ReadonlyTColor;
import toxi.color.TColor;

import java.util.List;

public class AnimateMode extends Mode {
  private static final float RIPPLE_SPEED = 20;
  private static final float MAX_RIPPLE_STRENGTH = 30;
  private static final float LINE_PULSE = 20;

  public AnimateMode(MooYoung mooYoung) {
    super(mooYoung);
  }

  @Override
  public void draw() {
    List<Ripple> ripples = mooYoung.getRipples();

    for (Sensor sensor : mooYoung.getSensors()) {
      if (sensor.checkUp()) {
        Ripple ripple = new Ripple(sensor.getPosition());
        ripples.add(ripple);
        Ani.to(ripple, 4f, "radius", mooYoung.width, Ani.QUAD_OUT);
      }
    }

    for (int i = ripples.size() - 1; i >= 0; i--) {
      Ripple ripple = ripples.get(i);
      if (ripple.radius > mooYoung.width) {
        ripples.remove(i);
      }
    }

    PGraphics canvas = mooYoung.getCanvas();
    PGraphics graphics = mooYoung.getGraphics();

    canvas.beginDraw();
    canvas.clear();
    canvas.stroke(255);
    canvas.noFill();

    for (int i = 0; i < mooYoung.getShapes().size(); i++) {
      RShape shape = mooYoung.getShapes().get(i);
      RPoint[] points = shape.getPoints();
      RPoint centroid = shape.getCentroid();

      canvas.beginShape();
      for (RPoint p : points) {
        PVector totalDisplacement = new PVector();
        for (Ripple ripple : ripples) {
          float dx = ripple.position.x - p.x;
          float dy = ripple.position.y - p.y;
          float dist = (float) Math.sqrt(dx * dx + dy * dy);
          float disp = Math.min(500 / Math.abs(dist - ripple.radius), MAX_RIPPLE_STRENGTH);
          RPoint radial = new RPoint(centroid);
          radial.sub(p);
          radial.normalize();
          radial.scale(disp);
          totalDisplacement.add(radial.x, radial.y);
        }

        float energy = 0;

        for (Sensor sensor : mooYoung.getSensors()) {
          RPoint epicenter = new RPoint(sensor.getPosition().x, sensor.getPosition().y);
          energy += sensor.getSmoothedValue() * 100 / epicenter.dist(p);

          if (sensor.getAssociatedShape() == shape) {
            canvas.fill((1 - sensor.getSmoothedValue()) * 255);
          }
        }

        TColor bright = TColor.RED.getRotatedRYB(i / 10f + mooYoung.frameCount / 100f);
        ReadonlyTColor idle = TColor.WHITE;

        canvas.stroke(bright.blend(idle, 1 - energy).toARGB());
        canvas.strokeWeight((float) ((Math.sin(i / 2f + -mooYoung.frameCount / 10f) + 1) / 2 * energy * LINE_PULSE + 2));
        canvas.vertex(p.x + totalDisplacement.x, p.y + totalDisplacement.y);
      }
      canvas.endShape(PConstants.CLOSE);
    }

    canvas.endDraw();
    mooYoung.getCornerPinSurface().render(graphics, canvas, false);
  }
}

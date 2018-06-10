package spacefiller;

import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import spacefiller.sensor.KeyboardSensor;
import spacefiller.sensor.Sensor;
import spacefiller.sensor.SerialPressureSensor;

import java.util.*;
import java.util.ArrayList;

public class MooYoung extends PApplet {
  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung");
  }


  private Keystone keystone;
  private List<ShapeRenderer> shapeRenderers;
  private int visibleContour = 0;
  private float t;
  private Sensor sensor;


  public void settings() {
    fullScreen(P3D, 1);
  }

  public void setup() {
    //
    try {
      sensor = new SerialPressureSensor(this);
    } catch (RuntimeException e) {
      System.out.println("Can't find serial connection. Resorting to keyboard control.");
      sensor = new KeyboardSensor(this, ' ');
    }

    RG.init(this);
    RG.setPolygonizer(RG.ADAPTATIVE);
    RG.ignoreStyles();

    shapeRenderers = new ArrayList<ShapeRenderer>();
    keystone = new Keystone(this);
    RShape shape = RG.loadShape(System.getProperty("user.dir") + "/contours.svg").children[0];
    // RShape shape = RG.loadShape("shoe-platform.svg");
    shape.scale(0.4f);

    RShape last = null;

    for (int i = shape.children.length - 1; i >= 0; i--) {
      PGraphics canvas = createGraphics(730, 500, P3D);
      CornerPinSurface surface = keystone.createCornerPinSurface(canvas, 10);
      ShapeRenderer renderer = new ShapeRenderer(last, shape.children[i], surface, i);
      last = shape.children[i];
      shapeRenderers.add(renderer);
    }

    try {
      keystone.load(System.getProperty("user.dir") + "/keystone.xml");
    } catch (NullPointerException e) {

    }

    visibleContour = 0;
    shapeRenderers.get(visibleContour).surface.setVisible(true);
  }

  public void draw() {
    background(0);

    if (!keystone.isCalibrating()) {
      t += 0.1f;
    } else {

    }

    for (int i = shapeRenderers.size() - 1; i >= 0; i--) {
      shapeRenderers.get(i).draw();
    }
  }

  class ShapeRenderer {
    private RShape inner;
    private RShape outer;
    CornerPinSurface surface;
    int i;

    public ShapeRenderer(RShape inner, RShape outer, CornerPinSurface surface, int i) {
      this.inner = inner;
      this.outer = outer;
      this.surface = surface;
      this.i = i;
    }

    void draw() {
      PGraphics graphics = surface.getCanvas();
      if (keystone.isCalibrating() && surface.visible) {
        graphics.beginDraw();
        graphics.clear();
        graphics.noFill();
        graphics.stroke(255);
        graphics.strokeWeight(5);
        if (this.inner != null) {
          this.inner.draw(graphics);
        }
        this.outer.draw(graphics);
        graphics.endDraw();
      } else if (!keystone.isCalibrating()) {
        graphics.beginDraw();
        graphics.clear();
        graphics.stroke(255);
        graphics.noFill();
        float lineThickness = sensor.isDepressed() ? 5 : 1;
        graphics.strokeWeight((sin(t / 5f + i / 5f) + 1) / 2 * lineThickness);
        graphics.stroke(255);

        this.outer.draw(graphics);
        graphics.endDraw();
      }
    }
  }

  public void keyPressed() {
    if (key == 's') {
      keystone.setSkewMode();
    } else if (key == 'x') {
      keystone.setScaleMode();
    } else if (key == 'r') {
      keystone.setRotateMode();
    } else if (key == ' ') {
      keystone.setNoMode();
    }

    if (keyCode == RIGHT) {
      shapeRenderers.get(visibleContour).surface.setVisible(false);
      visibleContour = (visibleContour + 1) % shapeRenderers.size();
      shapeRenderers.get(visibleContour).surface.setVisible(true);
    } else if (keyCode == LEFT) {
      shapeRenderers.get(visibleContour).surface.setVisible(false);
      visibleContour = (visibleContour - 1) % shapeRenderers.size();
      shapeRenderers.get(visibleContour).surface.setVisible(true);
    }
  }

  public void keyReleased() {
    if (key == 's') {
      keystone.setSkewMode();
    } else if (key == 'x') {
      keystone.setSkewMode();
    } else if (key == 'r') {
      keystone.setSkewMode();
    }
  }

  public void mouseReleased() {
    keystone.save(System.getProperty("user.dir") + "/keystone.xml");
  }
}

package spacefiller;

import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.modes.Mode;
import spacefiller.modes.NoOpMode;
import spacefiller.modes.WarpMode;
import spacefiller.sensor.KeyboardSensor;
import spacefiller.sensor.Sensor;
import spacefiller.sensor.SerialPressureSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MooYoung2 extends PApplet {
  private Mode currentMode;
  private Transformable currentSelection;

  private CornerPinSurface surface;
  private PGraphics canvas;
  private List<RShape> shapes;
  private List<Sensor> sensors;
  private List<Ripple> ripples;

  public void settings() {
    fullScreen(P3D, 1);
  }

  public void setup() {
    canvas = createGraphics(width, height, P3D);

    surface = new CornerPinSurface(width, height, 10);
    currentSelection = surface;

    registerMethod("mouseEvent", this);
    registerMethod("keyEvent", this);

    RG.init(this);
    RG.setPolygonizer(RG.ADAPTATIVE);
    RG.ignoreStyles();

    RShape shape = RG.loadShape(System.getProperty("user.dir") + "/contours.svg");
    shapes = Arrays.asList(shape.children);
    Collections.sort(shapes, new ShapeComparator());
    shape.scale(1);

    RPoint epicenter = shapes.get(shapes.size() - 1).getCentroid();

    sensors = new ArrayList<>();
    try {
      Sensor sensor = new SerialPressureSensor(this);
      sensor.setPosition(new PVector(epicenter.x, epicenter.y));
      sensors.add(sensor);
    } catch (RuntimeException e) {
      System.out.println("Can't find serial connection. Resorting to keyboard control.");
      Sensor sensor = new KeyboardSensor(this, ' ');
      sensor.setPosition(new PVector(epicenter.x, epicenter.y));
      sensors.add(sensor);
    }


    currentMode = new WarpMode(new RShapeTransformer(shape.children[0]));
  }

  public void draw() {
    background(0);

    canvas.beginDraw();
    canvas.clear();
    canvas.stroke(255);
    canvas.noFill();

    if (currentMode != null) {
      currentMode.draw(canvas);
    }

    for (RShape shape : shapes) {
      shape.draw(canvas);
    }
    canvas.endDraw();

    surface.render(this.getGraphics(), canvas, true);
  }

  public void mouseEvent(MouseEvent mouseEvent) {
    currentMode.mouseEvent(mouseEvent);
  }

  public void keyEvent(KeyEvent keyEvent) {
    currentMode.keyEvent(keyEvent);
  }

  public void mousePressed() {

  }

  public void mouseReleased() {

  }

  public void keyPressed() {

  }

  public void keyReleased() {

  }


  private void setMode(Mode nextMode) {
    currentMode = nextMode;
  }



  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung2");
  }
}

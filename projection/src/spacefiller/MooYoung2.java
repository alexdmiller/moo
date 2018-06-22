package spacefiller;

import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.serial.Serial;
import spacefiller.modes.*;
import spacefiller.sensor.KeyboardSensor;
import spacefiller.sensor.Sensor;
import spacefiller.sensor.SerialConnection;
import spacefiller.sensor.SerialPressureSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MooYoung2 extends PApplet {
  private Mode currentMode;

  private CornerPinSurface surface;
  private PGraphics canvas;
  private List<RShape> shapes;
  private List<Sensor> sensors;
  private List<Ripple> ripples;

  private List<Transformable> transformables;
  private int selectedIndex = 1;

  public void settings() {
    fullScreen(P3D, 1);
  }

  public void setup() {
    canvas = createGraphics(width, height, P3D);

    transformables = new ArrayList<>();
    surface = new CornerPinSurface(width, height, 10);
    transformables.add(surface);

    registerMethod("mouseEvent", this);
    registerMethod("keyEvent", this);

    RG.init(this);
    RG.setPolygonizer(RG.ADAPTATIVE);
    RG.ignoreStyles();

    RShape shape = RG.loadShape(System.getProperty("user.dir") + "/contours.svg");
    shapes = Arrays.asList(shape.children);
    Collections.sort(shapes, new ShapeComparator());
    shape.scale(1);

    transformables.addAll(shapes.stream().map(e -> new RShapeTransformer(e)).collect(Collectors.toList()));

    RPoint epicenter = shapes.get(shapes.size() - 1).getCentroid();

    sensors = new ArrayList<>();
    try {
      println(Arrays.toString(Serial.list()));
      SerialConnection connection = new SerialConnection(new Serial(this, "/dev/tty.usbmodem1421", 9600), 2);

      sensors.add(connection.getSensor(0));
      sensors.add(connection.getSensor(1));

//      Sensor sensor = new SerialPressureSensor(this);
//      sensor.setPosition(new PVector(epicenter.x, epicenter.y));
//      sensors.add(sensor);
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


    translate(20, 20);
    stroke(255);

    for (Sensor sensor : sensors) {
      if (sensor.isDepressed()) {
        fill(255, 0, 0);
      } else {
        fill(0);
      }

      ellipse(0, 0, 50, 50);
      translate(0, 100);
    }

  }

  public void mouseEvent(MouseEvent mouseEvent) {
    currentMode.mouseEvent(mouseEvent);
  }

  public void keyEvent(KeyEvent keyEvent) {
    if (keyEvent.getAction() == KeyEvent.PRESS) {
      if (keyEvent.getKey() == 'r') {
        currentMode = new RotateMode(transformables.get(selectedIndex));
      } else if (keyEvent.getKey() == 's') {
        currentMode = new ScaleMode(transformables.get(selectedIndex));
      } else if (keyEvent.getKeyCode() == RIGHT) {
        selectedIndex = Math.floorMod(selectedIndex + 1, transformables.size());
      } else if (keyEvent.getKeyCode() == LEFT) {
        selectedIndex = Math.floorMod(selectedIndex - 1, transformables.size());
      } else if (keyEvent.getKey() == 'b') {
        selectedIndex = 0;
      }
    } else if (keyEvent.getAction() == KeyEvent.RELEASE) {
      currentMode = new WarpMode(transformables.get(selectedIndex));
    }

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

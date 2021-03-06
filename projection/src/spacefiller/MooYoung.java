package spacefiller;

import de.looksgood.ani.Ani;
import geomerative.RG;
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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MooYoung extends PApplet {
  public static final float SHAPE_SCALE = 0.97f;

  private Mode currentMode;
  private CornerPinSurface surface;
  private PGraphics canvas;
  private List<RShape> shapes;
  private List<Sensor> sensors;
  private List<Transformable> transformables;
  private int selectedIndex = 1;

  public void settings() {
    fullScreen(P3D, 1);
    //size(1920, 1080, P3D);
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
    shape.scale(SHAPE_SCALE);
    shape.translate(-shape.getTopLeft().x, -shape.getTopLeft().y);
    shape.translate(width / 2 - shape.getWidth() / 2, height / 2 - shape.getHeight() / 2);

    transformables.addAll(shapes.stream().map(e -> new RShapeTransformer(e, surface)).collect(Collectors.toList()));

    sensors = new ArrayList<>();
    try {
      println(Arrays.toString(Serial.list()));
//      SerialConnection connection = new SerialConnection(new Serial(this, "/dev/ttyACM0", 9600), 2, 20);
      SerialConnection connection = new SerialConnection(new Serial(this, "/dev/ttyACM0", 9600), 2, 5);

      SerialPressureSensor sensor1 = connection.getSensor(0);
      SerialPressureSensor sensor2 = connection.getSensor(1);

      // Back
      sensor1.setSensitivity(4f);

      // Front
      sensor2.setSensitivity(4f);

      sensors.add(sensor1);
      sensors.add(sensor2);
    } catch (RuntimeException e) {
      e.printStackTrace();
      System.out.println("Can't find serial connection. Resorting to keyboard control.");
      sensors.add(new KeyboardSensor(this, 'm', 20));
      sensors.add(new KeyboardSensor(this, 'n', 20));
    }

    RShape frontPlatform = shapes.get(shapes.size() - 1);
    RShape backPlatform = shapes.get(shapes.size() - 2);

    sensors.get(0).setAssociatedShape(backPlatform);
    sensors.get(1).setAssociatedShape(frontPlatform);

    Ani.init(this);

    try {
      load();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    currentMode = new AnimateMode(this);
  }

  @Override
  public void mouseReleased() {
    try {
      FileWriter fileWriter = new FileWriter("warp-points.csv");
      PrintWriter printWriter = new PrintWriter(fileWriter);

      for (Transformable transformable : transformables) {
        List<PVector> points = Arrays.asList(transformable.getControlPoints());
        List<String> pointStrings = points.stream().map(p -> p.x + "\t" + p.y).collect(Collectors.toList());
        printWriter.println(String.join("\t", pointStrings));
      }

      printWriter.close();
      fileWriter.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  public void load() throws FileNotFoundException {
    File file = new File("warp-points.csv");
    Scanner scanner = new Scanner(file);

    for (int i = 0; i < transformables.size(); i++) {
      String line = scanner.nextLine();
      Scanner lineScan = new Scanner(line);

      PVector[] controlPoints = new PVector[4];
      for (int j = 0; j < 4; j++) {
        float x = lineScan.nextFloat();
        float y = lineScan.nextFloat();
        controlPoints[j] = new PVector(x, y);
      }

      transformables.get(i).setControlPoints(controlPoints);
    }
  }

  public void draw() {
    background(0);

    if (currentMode != null) {
      currentMode.draw();
    }
  }

  public void mouseEvent(MouseEvent mouseEvent) {
    currentMode.mouseEvent(mouseEvent);
  }

  public void keyEvent(KeyEvent keyEvent) {
    if (keyEvent.getAction() == KeyEvent.PRESS) {
      if (keyEvent.getKey() == 'e') {
        currentMode = new WarpMode(this);
      } else if (keyEvent.getKey() == 'r') {
        currentMode = new RotateMode(this);
      } else if (keyEvent.getKey() == 's') {
        currentMode = new ScaleMode(this);
      } else if (keyEvent.getKeyCode() == RIGHT) {
        selectedIndex = Math.floorMod(selectedIndex + 1, transformables.size());
      } else if (keyEvent.getKeyCode() == LEFT) {
        selectedIndex = Math.floorMod(selectedIndex - 1, transformables.size());
      } else if (keyEvent.getKey() == 'b') {
        selectedIndex = 0;
      } else if (keyEvent.getKey() == 'a') {
        currentMode = new AnimateMode(this);
      }
    } else if (keyEvent.getAction() == KeyEvent.RELEASE && currentMode.getClass() != AnimateMode.class) {
      currentMode = new WarpMode(this);
    }

    currentMode.keyEvent(keyEvent);
  }

  private void setMode(Mode nextMode) {
    currentMode = nextMode;
  }

  public CornerPinSurface getCornerPinSurface() {
    return surface;
  }

  public PGraphics getCanvas() {
    return canvas;
  }

  public List<RShape> getShapes() {
    return shapes;
  }

  public List<Sensor> getSensors() {
    return sensors;
  }

  public List<Transformable> getTransformables() {
    return transformables;
  }

  public Transformable getTransformTarget() {
    return transformables.get(selectedIndex);
  }

  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung");
  }

}

package spacefiller;

import codeanticode.syphon.SyphonServer;
import de.looksgood.ani.Ani;
import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.*;
import processing.opengl.PJOGL;
import processing.opengl.PShader;
import processing.serial.Serial;
import spacefiller.particles.Bounds;
import spacefiller.particles.Particle;
import spacefiller.particles.ParticleSystem;
import spacefiller.sensor.KeyboardSensor;
import spacefiller.sensor.Sensor;
import spacefiller.sensor.SerialPressureSensor;
import toxi.color.ReadonlyTColor;
import toxi.color.TColor;

import java.util.*;
import java.util.ArrayList;

public class MooYoung extends PApplet {
  private static final float RIPPLE_SPEED = 20;
  private static final float MAX_RIPPLE_STRENGTH = 30;

  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung");
  }

  private Keystone keystone;
  private List<ShapeRenderer> shapeRenderers;
  private CornerPinSurface particleSurface;
  private List<RShape> shapes;
  private int visibleContour = 0;
  private float t;
  private List<Sensor> sensors;
  private List<Ripple> ripples;
  private SyphonServer server;

  private ParticleSystem system;

  public void settings() {
    fullScreen(P3D, 1);
  }

  public void setup() {
    RG.init(this);
    RG.setPolygonizer(RG.ADAPTATIVE);
    RG.ignoreStyles();

    shapeRenderers = new ArrayList<ShapeRenderer>();
    keystone = new Keystone(this);
    RShape shape = RG.loadShape(System.getProperty("user.dir") + "/contours.svg");
    //RShape shape = RG.loadShape(System.getProperty("user.dir") + "/shoe-platform.svg");

    shapes = Arrays.asList(shape.children);
    Collections.sort(shapes, new ShapeComparator());

    background(0);
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

    for (int i = 0; i < shapes.size(); i++) {
      PGraphics canvas = createGraphics(width, height, P3D);
      CornerPinSurface surface = keystone.createCornerPinSurface(canvas, 10);
      ShapeRenderer renderer = new ShapeRenderer(shapes.get(i), surface, i);
      shapeRenderers.add(renderer);
    }

    PGraphics canvas = createGraphics(width, height, P3D);
    particleSurface = keystone.createCornerPinSurface(canvas, 10);

    try {
      keystone.load(System.getProperty("user.dir") + "/keystone.xml");
    } catch (NullPointerException e) {

    }

    visibleContour = 0;
    shapeRenderers.get(visibleContour).surface.setVisible(true);

    ripples = new ArrayList<>();

    Ani.init(this);

    system = new ParticleSystem(new Bounds(width, height), 100);
    system.fillWithParticles(100, 2);
  }

  public void draw() {
    background(0);

    if (!keystone.isCalibrating()) {
      t += 0.1f;

      for (Sensor sensor : sensors) {
        if (sensor.checkDepressed()) {
          Ripple ripple = new Ripple(sensor.getPosition());
          ripples.add(ripple);
          Ani.to(ripple, 4f, "radius", width, Ani.QUAD_OUT);
        }
      }

      for (int i = ripples.size() - 1; i >= 0; i--) {
        Ripple ripple = ripples.get(i);
        if (ripple.radius > width) {
          ripples.remove(i);
        }
      }
    }

    for (ShapeRenderer renderer : shapeRenderers) {
      renderer.draw();
    }


    system.update();
    particleSurface.canvas.beginDraw();
    particleSurface.canvas.clear();
    particleSurface.canvas.stroke(255);
    particleSurface.canvas.strokeWeight(10);
    for (Particle p : system.getParticles()) {
      particleSurface.canvas.point(p.position.x, p.position.y);
    }
    particleSurface.canvas.endDraw();
    particleSurface.render();

    fill(255);
    stroke(255);
    textSize(20);
    text(frameRate, 10, 20);
  }

  class ShapeRenderer {
    private RShape shape;
    public CornerPinSurface surface;
    int i;

    public ShapeRenderer(RShape shape, CornerPinSurface surface, int i) {
      this.shape = shape;
      this.surface = surface;
      this.i = i;
    }

    void draw() {
      PGraphics graphics = surface.getCanvas();
      if (keystone.isCalibrating() && surface.visible) {
        graphics.beginDraw();
        graphics.clear();
        graphics.blendMode(PConstants.ADD);

        graphics.noFill();
        graphics.stroke(255);
        graphics.strokeWeight(5);
        this.shape.draw(graphics);
        graphics.endDraw();

        surface.render(true);
      } else if (!keystone.isCalibrating()) {
        graphics.beginDraw();
        graphics.clear();
//
        graphics.noFill();
        graphics.stroke(255);

        RPoint[] points = this.shape.getPoints();
        RPoint centroid = this.shape.getCentroid();

        graphics.beginShape();
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

          for (Sensor sensor : sensors) {
            RPoint epicenter = new RPoint(sensor.getPosition().x, sensor.getPosition().y);
            if (!sensor.isDepressed()) {
              energy += 100 / epicenter.dist(p);
            }
          }

          TColor bright = TColor.RED.getRotatedRYB(i / 10f + t);
          ReadonlyTColor idle = TColor.WHITE;

          graphics.stroke(bright.blend(idle, 1 - energy).toARGB());
          graphics.strokeWeight(energy * 10 + 1);
          graphics.vertex(p.x + totalDisplacement.x, p.y + totalDisplacement.y);
        }
        graphics.endShape(CLOSE);
        graphics.endDraw();

        surface.render();
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
    } else if (key == 'b') {
      selectContour(0);
    }

    if (keyCode == RIGHT) {
      selectContour((visibleContour + 1) % shapeRenderers.size());
    } else if (keyCode == LEFT) {
      selectContour((visibleContour - 1) % shapeRenderers.size());
    }
  }

  private void selectContour(int layer) {
    shapeRenderers.get(visibleContour).surface.setVisible(false);
    visibleContour = layer;
    shapeRenderers.get(visibleContour).surface.setVisible(true);
  }

  public void keyReleased() {
    if (key == 's') {
      keystone.setSkewMode();
    } else if (key == 'x') {
      keystone.setSkewMode();
    } else if (key == 'r') {
      keystone.setSkewMode();
    } else if (key == 'm') {
      CornerPinSurface bottom = shapeRenderers.get(0).surface;

      for (int i = 1; i < shapeRenderers.size(); i++) {
        CornerPinSurface surface = shapeRenderers.get(i).surface;
        surface.setMeshCorner(surface.BL, bottom.getCorner(bottom.BL));
        surface.setMeshCorner(surface.TR, bottom.getCorner(bottom.TR));
        surface.setMeshCorner(surface.BR, bottom.getCorner(bottom.BR));
        surface.setMeshCorner(surface.TL, bottom.getCorner(bottom.TL));
        surface.setPosition(bottom.getPosition());
        surface.calculateMesh();
      }

      particleSurface.setMeshCorner(particleSurface.BL, bottom.getCorner(bottom.BL));
      particleSurface.setMeshCorner(particleSurface.TR, bottom.getCorner(bottom.TR));
      particleSurface.setMeshCorner(particleSurface.BR, bottom.getCorner(bottom.BR));
      particleSurface.setMeshCorner(particleSurface.TL, bottom.getCorner(bottom.TL));
      particleSurface.setPosition(bottom.getPosition());
      particleSurface.calculateMesh();

      save();
    }
  }

  public void mouseReleased() {
    save();
  }

  public void save() {
    keystone.save(System.getProperty("user.dir") + "/keystone.xml");
  }
}

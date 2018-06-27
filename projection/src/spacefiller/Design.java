package spacefiller;

import controlP5.ControlP5;
import geomerative.RG;
import geomerative.RShape;
import peasy.PeasyCam;
import processing.core.PGraphics;
import spacefiller.particles.*;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;
import java.util.ArrayList;

public class Design extends PApplet {

  public static void main(String[] args) {
    PApplet.main("spacefiller.Design");
  }

  ContourSpace contourSpace;
  List<Circle> circles;
  Circle selected;
  ParticleSystem particles;
  float maxThreshold = 2;
  float startThreshold = 0.2f;
  float startStep = 0.02f;
  float stepMultiplier = 1.5f;
  float noiseScale = 0.2f;
  float noiseHeight = 0.1f;
  float layerHeight = 50;
  PeasyCam cam;
  private boolean editMode;

  ControlP5 cp5;

  public void settings() {
    fullScreen(P3D, 2);
  }

  public void setup() {
    contourSpace = new ContourSpace(width, height, 15);
    circles = new ArrayList<>();
    RG.init(this);
//    particles = new ParticleSystem(new Bounds(width, height), 100);
//    particles.addBehavior(new ReflectiveBounds());
//    //particles.addBehavior(new FlockParticles(2, 1, 1, 50, 150, 200, 0.1f, 10));
//    particles.addBehavior(new AttractParticles(50,1));
//    particles.addBehavior(new RepelParticles(25,2));
//    particles.addBehavior(new ParticleFriction(0.99f));
//    particles.fillWithParticles(100, 2);

    cp5 = new ControlP5(this);

    cp5.addSlider("maxThreshold")
        .setPosition(200,20)
        .setRange(1,5f);

    cp5.addSlider("startThreshold")
        .setPosition(20,40)
        .setRange(0,1f);

    cp5.addSlider("startStep")
        .setPosition(200,40)
        .setRange(0.0001f,0.1f);

    cp5.addSlider("stepMultiplier")
        .setPosition(20,60)
        .setRange(1.01f,2f);

    cp5.addSlider("noiseScale")
        .setPosition(200,60)
        .setRange(0.01f,0.3f);

    cp5.addSlider("noiseHeight")
        .setPosition(20,80)
        .setRange(0,0.5f);

    cp5.addSlider("layerHeight")
        .setPosition(200,80)
        .setRange(0,30);

    cp5.addSlider("circleHeight")
        .setPosition(20, 100)
        .setRange(0, 2);

    cp5.addSlider("circleRadius")
        .setPosition(200, 100)
        .setRange(0, 100);

    cam = new PeasyCam(this, 400);
    cam.lookAt(width/2, height/2, 0);
    cam.setMinimumDistance(1000);
    cam.setMaximumDistance(2000);
    editMode = true;
    cam.setActive(false);
  }

  public void mousePressed() {
    if (editMode) {
      PVector mouse = new PVector(mouseX, mouseY);
      if (keyPressed && keyCode == ALT) {
        Circle circle = new Circle(mouse.copy(), 100, 1);
        circles.add(circle);
        cp5.get("circleHeight").setValue(circle.height);
        cp5.get("circleRadius").setValue(circle.radius);
        selected = circle;
      } else {
        for (Circle c : circles) {
          float distance = mouse.dist(c.position);
          if (distance < c.radius) {
            selected = c;
            cp5.get("circleHeight").setValue(c.height);
            cp5.get("circleRadius").setValue(c.radius);
            break;
          }
        }
      }
    }
  }

  public void mouseDragged() {
    if (keyPressed && keyCode == SHIFT && selected != null) {
      selected.position.x = mouseX;
      selected.position.y = mouseY;
    }
  }

  public void mouseReleased() {
    //selected = null;
  }

  public void keyPressed() {
    if (key == 's') {
      RShape contours = new RShape();

      VectorGroupBuilder builder = new VectorGroupBuilder(2);
      for (List<LineSegment> layer : contourSpace.getLayers()) {
        for (LineSegment segment : layer) {
          builder.addGroup(new PVector[]{segment.p1, segment.p2});
        }
      }

      builder.merge();

      for (PVector[] group : builder.getGroups()) {
        RShape contour = new RShape();
        strokeWeight(1);
        noFill();
        contour.addMoveTo(group[0].x, group[0].y);

        for (int i = 1; i < group.length; i++) {
          contour.addLineTo(group[i].x, group[i].y);
        }

        contours.addChild(contour);
      }

      RG.saveShape(System.getProperty("user.dir") + "/contours.svg", contours);
    } else if (key == 'd') {
      editMode = !editMode;
      cam.setActive(!editMode);
    } else if (key == 'x') {
      if (selected != null) {
        circles.remove(selected);
        selected = null;
      }
    }
  }

  @Override
  public void keyReleased() {
  }

  public void draw() {
    background(0);

    float step = startStep;
    float threshold = startThreshold;

//    particles.update();

    contourSpace.resetGrid();
    contourSpace.addNoise(noiseScale, noiseHeight, 0);

    for (Circle c : circles) {
      contourSpace.addMetaBall(c.position, c.radius, c.height);
    }

    if (selected != null) {
      selected.height = cp5.getValue("circleHeight");
      selected.radius = cp5.getValue("circleRadius");
      fill(255, 100);
      stroke(255);
      ellipse(selected.position.x, selected.position.y, selected.radius * 2, selected.radius * 2);
    }

    contourSpace.clearLineSegments();

    while (threshold < maxThreshold) {
      contourSpace.drawIsoContour(threshold);
      threshold += step;
      step *= stepMultiplier;
    }

    stroke(255);
    noFill();

    List<List<LineSegment>> layers = contourSpace.getLayers();

    if (editMode) {
      camera();
      for (List<LineSegment> layer : layers) {
        for (LineSegment lineSegment : layer) {
          line(lineSegment.p1.x, lineSegment.p1.y, lineSegment.p2.x, lineSegment.p2.y);
        }
      }
    } else {
      for (List<LineSegment> layer : layers) {
        translate(0, 0, layerHeight);
        for (LineSegment lineSegment : layer) {
          line(lineSegment.p1.x, lineSegment.p1.y, lineSegment.p2.x, lineSegment.p2.y);
        }
      }
    }
  }


  class Circle {
    private float radius = 100;
    private PVector position;
    private float height;

    Circle(PVector position, float radius, float height) {
      this.position = position;
      this.radius = radius;
      this.height = height;
    }
  }
}

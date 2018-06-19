package spacefiller;

import controlP5.ControlP5;
import geomerative.RG;
import geomerative.RShape;
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
  float circleRadius = 100;
  float maxThreshold = 2;
  float startThreshold = 0.2f;
  float startStep = 0.02f;
  float stepMultiplier = 1.5f;

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

    cp5.addSlider("circleRadius")
        .setPosition(20,20)
        .setRange(0,255);

    cp5.addSlider("maxThreshold")
        .setPosition(100,20)
        .setRange(1,5f);

    cp5.addSlider("startThreshold")
        .setPosition(20,40)
        .setRange(0,1f);

    cp5.addSlider("startStep")
        .setPosition(100,40)
        .setRange(0.0001f,0.1f);

    cp5.addSlider("stepMultiplier")
        .setPosition(20,60)
        .setRange(1.01f,2f);

  }

  public void mousePressed() {
    if (mouseY > 100) {
      PVector mouse = new PVector(mouseX, mouseY);
      for (Circle c : circles) {
        float distance = mouse.dist(c.position);
        if (distance < c.radius) {
          selected = c;
          break;
        }
      }

      if (selected == null) {
        Circle circle = new Circle(mouse.copy(), 100);
        circles.add(circle);
      }
    }
  }

  public void mouseDragged() {
    if (selected != null) {
      selected.position.x = mouseX;
      selected.position.y = mouseY;
    }
  }

  public void mouseReleased() {
    selected = null;
  }

  public void keyPressed() {
    RShape contours = new RShape();

    VectorGroupBuilder builder = new VectorGroupBuilder(2);
    for (LineSegment segment : contourSpace.getLineSegments()) {
      builder.addGroup(new PVector[] { segment.p1, segment.p2 });
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
  }

  public void draw() {
    background(0);

    float step = startStep;
    float threshold = startThreshold;

//    particles.update();

    contourSpace.resetGrid();

    for (Circle c : circles) {
      contourSpace.addMetaBall(c.position, circleRadius);
    }

    contourSpace.clearLineSegments();

    while (threshold < maxThreshold) {
      contourSpace.drawIsoContour(threshold);
      threshold += step;
      step *= stepMultiplier;
    }

    stroke(255);
    noFill();

    for (LineSegment lineSegment : contourSpace.getLineSegments()) {
      line(lineSegment.p1.x, lineSegment.p1.y, lineSegment.p2.x, lineSegment.p2.y);
    }
  }

  class Circle {
    float radius = 100;
    PVector position;

    Circle(PVector position, float radius) {
      this.position = position;
      this.radius = radius;
    }
  }
}

package spacefiller;

import geomerative.RG;
import geomerative.RShape;
import spacefiller.particles.*;
import processing.core.PApplet;
import processing.core.PVector;
import spacefiller.particles.behaviors.*;

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

  public void settings() {
    fullScreen(P3D, 2);
  }

  public void setup() {
    contourSpace = new ContourSpace(width, height, 100);
    circles = new ArrayList<>();
    RG.init(this);
//    particles = new ParticleSystem(new Bounds(width, height), 100);
//    particles.addBehavior(new ReflectiveBounds());
//    //particles.addBehavior(new FlockParticles(2, 1, 1, 50, 150, 200, 0.1f, 10));
//    particles.addBehavior(new AttractParticles(50,1));
//    particles.addBehavior(new RepelParticles(25,2));
//    particles.addBehavior(new ParticleFriction(0.99f));
//    particles.fillWithParticles(100, 2);
  }

  public void mousePressed() {
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

    VectorGroupBuilder builder = new VectorGroupBuilder(1);
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

    float step = 0.01f;
    float threshold = 0.2f;

//    particles.update();

    contourSpace.resetGrid();

    for (Circle c : circles) {
      contourSpace.addMetaBall(c.position, c.radius);
    }

//    for (Particle p : particles.getParticles()) {
//      contourSpace.addMetaBall(new PVector(p.position.x + width/2, p.position.y + height/2), 50);
//    }

    contourSpace.clearLineSegments();

    while (threshold < 2) {
      contourSpace.drawIsoContour(threshold);
      threshold += step;
      step *= 1.5;
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

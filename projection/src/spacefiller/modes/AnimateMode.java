package spacefiller.modes;

import de.looksgood.ani.Ani;
import de.looksgood.ani.AniSequence;
import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import megamu.mesh.Delaunay;
import megamu.mesh.Voronoi;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import spacefiller.*;
import spacefiller.particles.Bounds;
import spacefiller.particles.Particle;
import spacefiller.particles.ParticleSystem;
import spacefiller.particles.behaviors.*;
import spacefiller.sensor.Sensor;
import toxi.color.ReadonlyTColor;
import toxi.color.TColor;

import java.util.ArrayList;
import java.util.List;

public class AnimateMode extends Mode {
  private static final float MAX_RIPPLE_STRENGTH = 30;
  private static final float LINE_PULSE = 20;
  private static final int NUM_PARTICLES = 500;

  private Delaunay delaunay;
  private Voronoi voronoi;

  private List<Ripple> ripples;
  private ParticleSystem particles;
  private RepelFixedPoints repelFixedPoints;
  private FlockParticles flockParticles;

  private List<BlackHole> blackHoles;

  private float tangentLineSize;
  private float tangentLineOffset;
  private float tangentLineSpeed;

  private AniSequence currentIdleSequence;

  private RShape maskShape;

  public AnimateMode(MooYoung mooYoung) {
    super(mooYoung);

    setupMaskShape();

    ripples = new ArrayList<>();
    blackHoles = new ArrayList<>();
    particles = new ParticleSystem(new Bounds(mooYoung.width, mooYoung.height), 500);

    RShape boundingShape = new RShape(mooYoung.getShapes().get(0));

    particles.addBehavior(new CustomFatalBounds(new Bounds(boundingShape.getWidth(), boundingShape.getHeight())));
    particles.addBehavior(new ParticleFriction(0.99f));

    flockParticles = new FlockParticles(1, 2, 1, 15, 30, 30, 0.01f, 1);
    particles.addBehavior(flockParticles);

    repelFixedPoints = new RepelFixedPoints(10, 0.01f);
    for (RShape shape : mooYoung.getShapes()) {
      int numPoints = (int) (shape.getCurveLength() / 18);

      for (int i = 0; i < numPoints; i++) {
        RPoint point = shape.getPoint((float) i / numPoints);
        repelFixedPoints.addFixedPoint(new PVector(point.x - mooYoung.width / 2, point.y - mooYoung.height / 2));
      }
    }

    float[][] points = new float[NUM_PARTICLES][2];
    for (int i = 0; i < points.length; i++) {
      RShape shape = mooYoung.getShapes().get((int) (Math.random() * mooYoung.getShapes().size()));
      RPoint p = shape.getPoint((float) Math.random());
      points[i][0] = p.x;
      points[i][1] = p.y;
    }

    delaunay = new Delaunay(points);
    voronoi = new Voronoi(points);

    particles.addBehavior(repelFixedPoints);

    for (Sensor sensor : mooYoung.getSensors()) {
      BlackHole hole = new BlackHole(sensor.getPosition().x - mooYoung.width / 2, sensor.getPosition().y - mooYoung.height / 2);
      blackHoles.add(hole);
      particles.addBehavior(hole);
    }

    particles.fillWithParticles(NUM_PARTICLES, 2);
    particles.createAreaSource(1, 2);
  }

  private void setupMaskShape() {
    maskShape = RG.loadShape(System.getProperty("user.dir") + "/contours-inverse.svg");
    maskShape.scale(MooYoung.SHAPE_SCALE);
    maskShape.translate(-maskShape.getTopLeft().x, -maskShape.getTopLeft().y);
    maskShape.translate(mooYoung.width / 2 - maskShape.getWidth() / 2, mooYoung.height / 2 - maskShape.getHeight() / 2);
  }

  @Override
  public void draw() {
    float totalEnergy = 0;

    for (int i = 0; i < mooYoung.getSensors().size(); i++) {
      Sensor sensor = mooYoung.getSensors().get(i);

      if (sensor.checkUp()) {
        onSensorUp(sensor);
      }

      if (sensor.checkDown()) {
        onSensorDown(sensor);
      }

      blackHoles.get(i).setStrength(sensor.getSmoothedValue() / 10f);

      totalEnergy += sensor.getSmoothedValue();
    }

    PGraphics canvas = mooYoung.getCanvas();
    PGraphics graphics = mooYoung.getGraphics();

    canvas.beginDraw();

    updateFlock(totalEnergy);
    purgeRipples();
    drawContours(canvas);
    updateAndDrawParticles(canvas, totalEnergy);
    updateTangentLines();

//
//    float[][] edges = delaunay.getEdges();
//    for(int i=0; i<edges.length; i++)
//    {
//      float startX = edges[i][0];
//      float startY = edges[i][1];
//      float endX = edges[i][2];
//      float endY = edges[i][3];
//      canvas.line( startX, startY, endX, endY );
//    }

//    MPolygon[] myRegions = voronoi.getRegions();
//    PVector center = new PVector(mooYoung.width / 2, mooYoung.height / 2);
//    for(int i=0; i<myRegions.length; i++)
//    {
//      // an array of points
//      float[][] regionCoordinates = myRegions[i].getCoords();
//
//      PVector p = new PVector(regionCoordinates[0][0], regionCoordinates[0][1]);
//
//      float opacity = (float) Math.sin(center.dist(p) / 500f - mooYoung.frameCount / 40f) * 255;
//
//      canvas.strokeWeight(5);
//      canvas.stroke(255, opacity);
//      canvas.fill(255, 0, 255, opacity);
//      canvas.noFill();
//      myRegions[i].draw(canvas); // draw this shape
//    }

    drawMask(canvas);


    canvas.endDraw();
    mooYoung.getCornerPinSurface().render(graphics, canvas, false);

    graphics.text(mooYoung.frameRate, 20, 20);
  }

  private void onSensorDown(Sensor sensor) {
    startTangentLineAnimation();
  }

  private void onSensorUp(Sensor sensor) {
    Ripple ripple = new Ripple(sensor.getPosition());
    ripples.add(ripple);
    Ani.to(ripple, 4f, "radius", mooYoung.width, Ani.QUAD_OUT);
  }

  private boolean bothSensorsDown() {
    for (Sensor sensor : mooYoung.getSensors()) {
      if (!sensor.isDepressed()) {
        return false;
      }
    }
    return true;
  }

  private void drawMask(PGraphics canvas) {
    canvas.ortho();
    canvas.pushMatrix();
    canvas.translate(0, 0, 2);
    canvas.fill(0);
    canvas.noStroke();
    maskShape.draw(canvas);
    canvas.popMatrix();

    canvas.pushMatrix();
    canvas.translate(0, 0, 2);
    canvas.stroke(0 );
    canvas.strokeWeight(40);
    canvas.noFill();
    canvas.rect(
        mooYoung.width / 2 - maskShape.getWidth() / 2 - 18,
        mooYoung.height / 2 - maskShape.getHeight() / 2 - 18,
        maskShape.getWidth() + 30,
        maskShape.getHeight() + 35);
    canvas.popMatrix();
  }

  private void updateFlock(float totalEnergy) {
    flockParticles.setDesiredSeparation(totalEnergy * 20 + 20);
    flockParticles.separationWeight = totalEnergy * 4 + 1;
    flockParticles.setCohesionThreshold(totalEnergy * 20 + 40);
    flockParticles.setCohesionWeight(totalEnergy * 5 + 1);
    flockParticles.setMaxSpeed(totalEnergy * 5 + 1);
    flockParticles.setAlignmentThreshold(totalEnergy * 100 + 30);
  }

  private void purgeRipples() {
    for (int i = ripples.size() - 1; i >= 0; i--) {
      Ripple ripple = ripples.get(i);
      if (ripple.radius >= mooYoung.width) {
        ripples.remove(i);
      }
    }
  }

  private void updateAndDrawParticles(PGraphics canvas, float totalEnergy) {
    particles.update();

    canvas.pushMatrix();
    canvas.translate(mooYoung.width / 2, mooYoung.height / 2);
    canvas.fill(255);

    List<Particle> particleList = particles.getParticles();
    for (int i = 0; i < particleList.size(); i++) {
      Particle p1 = particleList.get(i);

      for (Ripple ripple : ripples) {
        PVector ripplePosition = ripple.position.copy();
        ripplePosition.add(-mooYoung.width / 2, -mooYoung.height / 2);

        float dx = ripplePosition.x - p1.position.x;
        float dy = ripplePosition.y - p1.position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float disp = Math.min(1 / Math.abs(dist - ripple.radius), MAX_RIPPLE_STRENGTH);
        PVector radial = ripplePosition;
        radial.sub(p1.position);
        radial.normalize();
        radial.mult(-disp);
        p1.applyForce(new PVector(radial.x, radial.y));
      }

      canvas.strokeWeight(2);
      canvas.stroke(255, 100);

      for (int j = i + 1; j < particleList.size(); j++) {
        Particle p2 = particleList.get(j);
        float dist = p1.position.dist(p2.position);
        if (dist < flockParticles.desiredSeparation + 5) {
          canvas.stroke(255, (p1.velocity.mag() + p2.velocity.mag()) / 2f * 100);
          canvas.line(p1.position.x, p1.position.y, p2.position.x, p2.position.y);
        }
      }
    }


    canvas.noFill();
    canvas.strokeWeight(2);
    float radius = totalEnergy * 3 + 4;
    for (Particle p : particles.getParticles()) {
      canvas.pushMatrix();
      canvas.translate(p.position.x, p.position.y);
      canvas.stroke(255, p.velocity.mag() * 2 * 255);
      canvas.ellipse(0, 0, radius, radius);
      canvas.popMatrix();
    }

//    for (PVector p : repelFixedPoints.getFixedPoints()) {
//      canvas.strokeWeight(1);
//      canvas.noFill();
//      canvas.ellipse(p.x, p.y, repelFixedPoints.repelThreshold * 2, repelFixedPoints.repelThreshold * 2);
//    }

    canvas.popMatrix();
  }

  private void drawContours(PGraphics canvas) {
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
  }

  private void updateTangentLines() {
    tangentLineOffset += tangentLineSpeed;

    PGraphics canvas = mooYoung.getCanvas();
    canvas.stroke(255);
    canvas.strokeWeight(3);

    for (RShape shape : mooYoung.getShapes()) {
      int numPoints = (int) (shape.getCurveLength() / 18);

      for (int i = 0; i < numPoints; i++) {
        try {
          float pointParameter = ((float) i / numPoints + tangentLineOffset / (float) numPoints) % 1f;
          RPoint point = shape.getPoint(pointParameter);
          RPoint tangent = shape.getTangent(pointParameter);
          tangent.normalize();
          tangent.scale(tangentLineSize);

          canvas.pushMatrix();
          canvas.translate(point.x, point.y);
          canvas.rotate((float) Math.PI / 2);
          canvas.line(0, 0, tangent.x, tangent.y);
          canvas.popMatrix();
        } catch (ArrayIndexOutOfBoundsException e) {

        }
      }
    }
  }

  private void transitionOutIdleSequence() {

  }

  private void chooseRandomIdleSequence() {

  }

  private void startTangentLineAnimation () {
    AniSequence seq = new AniSequence(mooYoung);

    seq.beginSequence();

    seq.add(Ani.to(this, 1f, "tangentLineSize", 10, Ani.SINE_OUT));
    seq.add(Ani.to(this, 1f, "tangentLineSpeed", 0.03f, Ani.EXPO_IN));

    seq.beginStep();
    seq.add(Ani.to(this, 2f, 6f, "tangentLineSize", 0f, Ani.SINE_IN));
    seq.add(Ani.to(this, 2f, 5f, "tangentLineSpeed", 0f, Ani.SINE_IN, "onEnd:chooseRandomIdleSequence"));
    seq.endStep();

    seq.endSequence();
    seq.start();

    currentIdleSequence = seq;
  }

  private void startVoronoiAnimation() {

  }
}

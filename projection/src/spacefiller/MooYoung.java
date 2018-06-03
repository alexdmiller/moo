package spacefiller;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;

import java.util.*;
import java.util.ArrayList;

public class MooYoung extends PApplet {
  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung");
  }


  private Keystone ks;
  private List<ShapeRenderer> shapeRenderers;
  private int visibleContour = 0;
  private float t;

  public void settings() {
    fullScreen(P3D);
  }

  public void setup() {
    shapeRenderers = new ArrayList<ShapeRenderer>();
    ks = new Keystone(this);
    PShape shape = loadShape("shoe-platform.svg");
    shape.scale(0.4f);

    int i = 0;
    for (PShape child : shape.getChildren()) {
      PGraphics canvas = createGraphics(700, 700, P3D);
      CornerPinSurface surface = ks.createCornerPinSurface(canvas, 10);
      ShapeRenderer renderer = new ShapeRenderer(child, surface, i);
      shapeRenderers.add(renderer);
      i++;
    }
  }

  public void draw() {
    background(0);

    if (!ks.isCalibrating()) {
      t += 0.1f;
    } else {
      fill(255);
      ellipse(mouseX, mouseY, 20, 20);
    }


    for (ShapeRenderer contour : shapeRenderers) {
      contour.draw();
    }
  }

  class ShapeRenderer {
    PShape shape;
    CornerPinSurface surface;
    int i;

    public ShapeRenderer(PShape shape, CornerPinSurface surface, int i) {
      this.shape = shape;
      this.surface = surface;
      this.i = i;
    }

    void draw() {
      shape.disableStyle();
      PGraphics graphics = surface.getCanvas();
      if (ks.isCalibrating() && surface.visible) {
        graphics.beginDraw();
        graphics.clear();
        graphics.noFill();
        graphics.stroke(255);
        graphics.strokeWeight(20);
        graphics.shape(this.shape);
        graphics.endDraw();
      } else if (!ks.isCalibrating()) {
        graphics.beginDraw();
        graphics.clear();
        graphics.stroke(255);
        graphics.strokeWeight(sin(i  + t) * 5 + 5);
        //graphics.noStroke();
        graphics.noFill();
        //graphics.fill((sin(i  + t) + 1) / 2 * 255);
        graphics.shape(this.shape);
        graphics.endDraw();
      }
    }
  }

  public void keyPressed() {
    if (key == ' ') {
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
}

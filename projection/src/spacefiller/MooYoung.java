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


  private Keystone keystone;
  private List<ShapeRenderer> shapeRenderers;
  private int visibleContour = 0;
  private float t;

  public void settings() {
    fullScreen(P3D);
  }

  public void setup() {
    shapeRenderers = new ArrayList<ShapeRenderer>();
    keystone = new Keystone(this);
    PShape shape = loadShape("shoe-platform.svg");
    shape.scale(0.4f);

    int i = 0;
    for (PShape child : shape.getChildren()) {
      PGraphics canvas = createGraphics(700, 700, P3D);
      CornerPinSurface surface = keystone.createCornerPinSurface(canvas, 10);
      ShapeRenderer renderer = new ShapeRenderer(child, surface, i);
      shapeRenderers.add(renderer);
      i++;
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
      if (keystone.isCalibrating() && surface.visible) {
        graphics.beginDraw();
        graphics.clear();
        graphics.noFill();
        graphics.stroke(255);
        graphics.strokeWeight(20);
        graphics.shape(this.shape);
        graphics.endDraw();
      } else if (!keystone.isCalibrating()) {
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
    if (key == 's') {
      keystone.setSkewMode();
    } else if (key == 'x') {
      keystone.setScaleMode();
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
}

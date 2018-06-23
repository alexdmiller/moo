package spacefiller.modes;

import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import spacefiller.Transformable;

public class ScaleMode extends TransformerMode {
  private PVector lastMouse;

  public ScaleMode(Transformable target) {
    super(target);
    lastMouse = new PVector();
  }

  @Override
  public void draw(PGraphics graphics) {
    graphics.fill(255);
    if (target != null) {
      PVector center = target.getCenter();
      graphics.ellipse(center.x, center.y, 30, 30);
    }
  }

  @Override
  public void mouseEvent(MouseEvent e) {
    PVector mouse = new PVector(e.getX(), e.getY());

    switch (e.getAction()) {
      case MouseEvent.PRESS:
        lastMouse = mouse;
        break;

      case MouseEvent.DRAG:
        if (target != null) {
          PVector center = target.getCenter();

          float lastDistance = center.dist(lastMouse);
          float currentDistance = center.dist(mouse);

          float scale = currentDistance / lastDistance;
          target.scale(scale);

          lastMouse = mouse;
        }

        break;
    }
  }
}

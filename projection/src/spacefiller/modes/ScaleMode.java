package spacefiller.modes;

import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import spacefiller.CornerPinSurface;
import spacefiller.MooYoung2;
import spacefiller.Transformable;

public class ScaleMode extends EditMode {
  private PVector lastMouse;

  public ScaleMode(MooYoung2 mooYoung) {
    super(mooYoung);
    lastMouse = new PVector();
  }

  @Override
  public void drawEditingUI() {
    PGraphics canvas = mooYoung.getCanvas();
    Transformable target = mooYoung.getTransformTarget();

    canvas.fill(255);
    if (target != null) {
      PVector center = target.getCenter();
      canvas.ellipse(center.x, center.y, 30, 30);
    }
  }

  @Override
  public void mouseEvent(MouseEvent e) {
    PVector mouse = new PVector(e.getX(), e.getY());
    Transformable target = mooYoung.getTransformTarget();

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

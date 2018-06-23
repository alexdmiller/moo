package spacefiller.modes;

import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.Draggable;
import spacefiller.Transformable;

public class RotateMode extends TransformerMode {
  private PVector lastMouse;

  public RotateMode(Transformable target) {
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

          float initialAngle = (float) Math.atan2(center.y - lastMouse.y, center.x - lastMouse.x);
          float currentAngle = (float) Math.atan2(center.y - mouse.y, center.x - mouse.x);

          target.rotate(currentAngle - initialAngle);
          lastMouse = mouse;
        }
        break;
    }
  }
}

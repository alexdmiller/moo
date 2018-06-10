package spacefiller.modes;

import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.Draggable;
import spacefiller.Keystone;

public class RotateMode extends KeystoneMode {
  private CornerPinSurface target;
  private PVector lastMouse;

  public RotateMode(Keystone keystone) {
    super(keystone);
    lastMouse = new PVector();
  }

  public void draw(PGraphics graphics) {
    graphics.fill(255);
    if (target != null) {
      PVector center = target.getCenter();
      center.add(target.getPosition());
      graphics.ellipse(center.x, center.y, 30, 30);
    }
  }

  public void mouseEvent(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    switch (e.getAction()) {
      case MouseEvent.PRESS:
        CornerPinSurface top = null;
        // navigate the list backwards, as to select
        for (int i = keystone.getSurfaces().size()-1; i >= 0; i--) {
          CornerPinSurface s = keystone.getSurfaces().get(i);
          if (s.isVisible()) {
            Draggable draggable = s.select(x, y);
            if (draggable != null) {
              target = s;
              break;
            }
          }
        }
        lastMouse.set(x, y);
        break;

      case MouseEvent.DRAG:
        if (target != null) {
          PVector currentMouse = new PVector(x, y);

          PVector center = target.getCenter();
          center.add(target.getPosition());

          float initialAngle = (float) Math.atan2(center.y - lastMouse.y, center.x - lastMouse.x);
          float currentAngle = (float) Math.atan2(center.y - currentMouse.y, center.x - currentMouse.x);

          target.rotate(target.getCenter(), currentAngle - initialAngle);
          lastMouse = currentMouse;
        }
        break;

      case MouseEvent.RELEASE:
        target = null;
        break;
    }
  }
}

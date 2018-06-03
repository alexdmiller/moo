package spacefiller.modes;

import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.Draggable;
import spacefiller.Keystone;

public class SkewMode extends KeystoneMode {
  private Draggable dragged;

  public SkewMode(Keystone keystone) {
    super(keystone);
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
            dragged = s.select(x, y);
            if (dragged != null) {
              top = s;
              break;
            }
          }
        }
        break;

      case MouseEvent.DRAG:
        if (dragged != null)
          dragged.moveTo(x, y);
        break;

      case MouseEvent.RELEASE:
        dragged = null;
        break;
    }
  }
}

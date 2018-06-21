package spacefiller.modes;

import processing.core.PVector;
import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.Draggable;
import spacefiller.Transformable;

public class WarpMode extends TransformerMode {
  private Draggable dragged;

  public WarpMode(Transformable target) {
    super(target);
  }

  @Override
  public void mouseEvent(MouseEvent e) {
    PVector mouse = new PVector(e.getX(), e.getY());
    switch (e.getAction()) {
      case MouseEvent.PRESS:
        dragged = target.select(mouse);
        break;

      case MouseEvent.DRAG:
        if (dragged != null)
          dragged.moveTo(mouse.x, mouse.y);
        break;

      case MouseEvent.RELEASE:
        dragged = null;
        break;
    }
  }
}

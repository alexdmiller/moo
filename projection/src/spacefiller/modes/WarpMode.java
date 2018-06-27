package spacefiller.modes;

import processing.core.PVector;
import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.Draggable;
import spacefiller.MooYoung2;
import spacefiller.Transformable;

public class WarpMode extends EditMode {
  private Draggable dragged;

  public WarpMode(MooYoung2 mooYoung) {
    super(mooYoung);
  }

  @Override
  public void mouseEvent(MouseEvent e) {
    PVector mouse = new PVector(e.getX(), e.getY());
    Transformable target = mooYoung.getTransformTarget();

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

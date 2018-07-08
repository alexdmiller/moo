package spacefiller.modes;

import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.Draggable;
import spacefiller.MooYoung;
import spacefiller.Transformable;

public class WarpMode extends EditMode {
  private Draggable dragged;
  private PVector clickedPointDelta;
  private boolean forceDrag;

  public WarpMode(MooYoung mooYoung) {
    super(mooYoung);
  }

  @Override
  public void mouseEvent(MouseEvent e) {
    PVector mouse = new PVector(e.getX(), e.getY());
    Transformable target = mooYoung.getTransformTarget();

    mouse = target.getRelativePoint(mouse);

    switch (e.getAction()) {
      case MouseEvent.PRESS:
        if (forceDrag) {
          dragged = target.selectClosestPin(mouse);
          clickedPointDelta = PVector.sub(dragged.getPosition(), mouse);
        } else {
          dragged = target.select(mouse);
        }
        break;

      case MouseEvent.DRAG:
        if (dragged != null)
          if (clickedPointDelta != null) {
            dragged.moveTo(mouse.x + clickedPointDelta.x, mouse.y + clickedPointDelta.y);
          } else {
            dragged.moveTo(mouse.x, mouse.y);
          }
        break;

      case MouseEvent.RELEASE:
        dragged = null;
        clickedPointDelta = null;
        break;
    }
  }

  @Override
  public void keyEvent(KeyEvent keyEvent) {
    if (keyEvent.getKey() == 'f') {
      if (keyEvent.getAction() == KeyEvent.PRESS) {
        forceDrag = true;
      } else if (keyEvent.getAction() == KeyEvent.RELEASE) {
        forceDrag = false;
      }
    }
  }

  @Override
  protected void drawEditingUI() {
    mooYoung.getTransformTarget().renderControlPoints(mooYoung.getGraphics(), mooYoung.getCanvas());
  }
}

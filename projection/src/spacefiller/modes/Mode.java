package spacefiller.modes;

import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.CornerPinSurface;
import spacefiller.MooYoung2;

public class Mode {
  protected MooYoung2 mooYoung;

  public Mode(MooYoung2 mooYoung) {
    this.mooYoung = mooYoung;
  }

  public void draw() { }
  public void mouseEvent(MouseEvent mouseEvent) {}
  public void keyEvent(KeyEvent keyEvent) {}
}

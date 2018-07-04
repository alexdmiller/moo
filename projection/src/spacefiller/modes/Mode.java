package spacefiller.modes;

import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.MooYoung;

public class Mode {
  protected MooYoung mooYoung;

  public Mode(MooYoung mooYoung) {
    this.mooYoung = mooYoung;
  }

  public void draw() { }
  public void mouseEvent(MouseEvent mouseEvent) {}
  public void keyEvent(KeyEvent keyEvent) {}
}

package spacefiller.modes;

import processing.core.PGraphics;
import processing.event.MouseEvent;
import spacefiller.Keystone;

public abstract class KeystoneMode {
  protected Keystone keystone;

  public KeystoneMode(Keystone keystone) {
    this.keystone = keystone;
  }

  public void draw(PGraphics graphics) {}
  public abstract void mouseEvent(MouseEvent mouseEvent);

  public boolean isCalibratingMode() {
    return true;
  }
}

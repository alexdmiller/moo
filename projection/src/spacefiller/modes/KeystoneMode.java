package spacefiller.modes;

import processing.event.MouseEvent;
import spacefiller.Keystone;

public abstract class KeystoneMode {
  protected Keystone keystone;

  public KeystoneMode(Keystone keystone) {
    this.keystone = keystone;
  }

  public abstract void mouseEvent(MouseEvent mouseEvent);

  public boolean isCalibratingMode() {
    return true;
  }
}

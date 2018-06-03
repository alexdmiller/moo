package spacefiller.modes;

import processing.event.MouseEvent;
import spacefiller.Keystone;

public class NoOpMode extends KeystoneMode {
  public NoOpMode(Keystone keystone) {
    super(keystone);
  }

  @Override
  public void mouseEvent(MouseEvent mouseEvent) {}

  public boolean isCalibratingMode() {
    return false;
  }

}

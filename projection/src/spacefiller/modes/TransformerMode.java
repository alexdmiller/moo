package spacefiller.modes;

import processing.core.PGraphics;
import spacefiller.Transformable;

public class TransformerMode extends Mode {
  protected Transformable target;

  public TransformerMode(Transformable target) {
    this.target = target;
  }
}

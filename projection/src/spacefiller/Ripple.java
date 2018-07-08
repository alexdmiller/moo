package spacefiller;

import geomerative.RPoint;
import processing.core.PVector;

public class Ripple {
  public PVector position;
  public float radius;

  public Ripple(RPoint position) {
    this.position = new PVector(position.x, position.y);
  }

  public Ripple(PVector position) {
    this.position = position;
  }
}

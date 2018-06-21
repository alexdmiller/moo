package spacefiller;

import processing.core.PGraphics;
import processing.core.PVector;

public interface Transformable {
  void scale(PVector origin, float scale);
  void rotate(PVector origin, float theta);
  Draggable select(PVector point);
  PVector getCenter();
}

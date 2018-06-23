package spacefiller;

import processing.core.PVector;

public interface Transformable {
  void scale(float scale);
  void rotate(float theta);
  Draggable select(PVector point);
  PVector getCenter();
}

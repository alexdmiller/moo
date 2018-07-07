package spacefiller;

import geomerative.RShape;
import processing.core.PVector;
import spacefiller.particles.Bounds;
import spacefiller.particles.Particle;
import spacefiller.particles.behaviors.ParticleBehavior;

import java.util.Iterator;
import java.util.List;

public class FatalShapeBounds extends ParticleBehavior {
  private float buffer;
  private RShape shape;

  public FatalShapeBounds(RShape shape, float buffer) {
    this.shape = shape;
    this.buffer = buffer;
  }


  @Override
  public void apply(List<Particle> list) {
    Iterator<Particle> iter = list.iterator();
    Bounds bounds = getParticleSystem().getBounds();

    while(iter.hasNext()) {
      Particle p = iter.next();
      PVector pos = p.position;
      if (pos.x < -bounds.getWidth() / 2 + buffer ||
          pos.x > bounds.getWidth() / 2 - buffer ||
          pos.y < -bounds.getHeight() / 2 + buffer ||
          pos.y > bounds.getHeight() / 2 - buffer) {
        if (!shape.contains(pos.x, pos.y)) {
          iter.remove();
          this.getParticleSystem().notifyRemoved(p);
        }
      }
    }
  }
}

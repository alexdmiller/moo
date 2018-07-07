package spacefiller;

import processing.core.PVector;
import spacefiller.particles.Particle;
import spacefiller.particles.behaviors.ParticleBehavior;

import java.util.Iterator;
import java.util.List;

public class BlackHole extends ParticleBehavior {
  private PVector position;
  private float strength;
  private float killRadius = 20;

  public BlackHole(float x, float y) {
    position = new PVector(x, y);
  }

  public float getStrength() {
    return strength;
  }

  public void setStrength(float strength) {
    this.strength = strength;
  }

  @Override
  public void apply(List<Particle> list) {
    Iterator<Particle> iterator = list.iterator();
    while(iterator.hasNext()) {
      Particle p1 = iterator.next();

      PVector delta = PVector.sub(p1.position, position);
      float dist = delta.mag();
      if (dist < killRadius) {
        iterator.remove();
      } else {
        delta.setMag(-strength * 50 / delta.mag());
        p1.applyForce(delta);
      }
    }
  }
}

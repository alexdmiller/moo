package spacefiller;

import geomerative.RShape;

import java.util.Comparator;

public class ShapeComparator implements Comparator<RShape> {
  @Override
  public int compare(RShape o1, RShape o2) {
    return (int) (o2.getArea() - o1.getArea());
  }
}

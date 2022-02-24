package org.mvavrill.logicSolvers.image;

public class Vertex implements Comparable<Vertex> {
  private final double x;
  private final double y;

  public Vertex(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public Vertex weightedAverage(final Vertex end, final double weight) {
    return new Vertex((1-weight)*x + weight*end.x, (1-weight)*y + weight*end.y);
  }

  public double distance(final Vertex v) {
    return Math.sqrt((v.x-x)*(v.x-x) + (v.y-y)*(v.y-y));
  }
  
  @Override
  public String toString() {
    return "(" + x + " " + y + ")";
  }

  @Override
  public int compareTo(Vertex v) {
    if (x < v.x)
      return -1;
    else if (x > v.x)
      return 1;
    else if (y < v.y)
      return -1;
    else if (y > v.y)
      return 1;
    else
      return 0;
  }
}

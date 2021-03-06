package Utils;

public class Segment {
  protected Point first;
  protected Point second;
  protected Vector direction;
  
  public Segment(Point a, Point b) {
    first = a;
    second = b;
    direction = new Vector(a, b);
  }

  public Line getPerpendicularBisector() {
    double start_x = (first.getX() + second.getX()) / 2.0;
    double start_y = (first.getY() + second.getY()) / 2.0;
    Point start = new Point(start_x, start_y);

    Vector n = Geometry.getNormalizedPerpendicular(direction);
    Point end = new Point(start.getX() + n.getX(), start.getY() + n.getY());
    return new Line(start, end);
  }

}

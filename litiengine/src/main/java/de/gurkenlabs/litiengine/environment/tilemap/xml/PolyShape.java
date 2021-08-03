package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyShape;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PolyShape implements IPolyShape {
  @XmlAttribute
  @XmlJavaTypeAdapter(PolylineAdapter.class)
  private List<Point2D> points;

  /** Instantiates a new {@code PolyShape} instance. */
  public PolyShape() {
    super();
    this.points = new ArrayList<>();
  }

  /**
   * Instantiates a new {@code PolyShape} instance by copying from the specified original.
   *
   * @param original The poly line to be copied.
   */
  public PolyShape(IPolyShape original) {
    this();
    if (original == null) {
      return;
    }
    for (Point2D point : original.getPoints()) {
      this.points.add(new Point2D.Float((float) point.getX(), (float) point.getY()));
    }
  }

  @Override
  public List<Point2D> getPoints() {
    return this.points;
  }

  @Override
  public List<Point2D> getAbsolutePoints(double x, double y) {
    List<Point2D> absolutePoints = new ArrayList<>();

    for (int i = 1; i < this.getPoints().size(); i++) {
      Point2D point = this.getPoints().get(i);
      absolutePoints.add(new Point2D.Double(x + point.getX(), y + point.getY()));
    }

    return absolutePoints;
  }

  @Override
  public List<Point2D> getAbsolutePoints(Point2D origin) {
    return this.getAbsolutePoints(origin.getX(), origin.getY());
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (!(anObject instanceof IPolyShape)) {
      return false;
    }
    IPolyShape other = (IPolyShape) anObject;
    return this.getPoints().equals(other.getPoints());
  }

  @Override
  public int hashCode() {
    return this.getPoints().hashCode();
  }
}

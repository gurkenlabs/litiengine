package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyShape;

public abstract class Polyshape implements IPolyShape, Serializable {
  private static final long serialVersionUID = -9046398175130339L;

  @XmlAttribute
  @XmlJavaTypeAdapter(PolylineAdapter.class)
  private List<Point2D> points;

  public Polyshape() {
    super();
    this.points = new ArrayList<>();
  }

  public Polyshape(IPolyShape polyLineToBeCopied) {
    this();
    if (polyLineToBeCopied == null) {
      return;
    }
    for (Point2D point : polyLineToBeCopied.getPoints()) {
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

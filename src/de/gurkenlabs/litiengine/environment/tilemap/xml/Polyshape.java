package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyShape;

public abstract class Polyshape implements IPolyShape, Serializable {
  private static final Logger log = Logger.getLogger(Polyshape.class.getName());
  private static final long serialVersionUID = -9046398175130339L;

  @XmlAttribute(name = "points")
  private String rawPoints;

  private final transient List<Point2D> points;

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
    if (this.points.isEmpty()) {
      this.populateList();
    }
    return points;
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

  private void populateList() {
    if (this.rawPoints == null || this.rawPoints.isEmpty()) {
      return;
    }

    String[] arr = this.rawPoints.split(" ");
    for (String point : arr) {
      if (point == null || point.isEmpty()) {
        continue;
      }

      String[] coords = point.split(",");
      if (coords.length == 2 && coords[0] != null && coords[1] != null) {
        try {
          float x = Float.parseFloat(coords[0]);
          float y = Float.parseFloat(coords[1]);
          this.points.add(new Point2D.Float(x, y));
        } catch (NumberFormatException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }
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

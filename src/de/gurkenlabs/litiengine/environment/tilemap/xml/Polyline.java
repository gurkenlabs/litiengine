package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyline;

@XmlRootElement(name = "polyline")
public class Polyline implements IPolyline {
  @XmlAttribute(name = "points")
  private String rawPoints;

  @XmlTransient
  private final List<Point2D> points;

  public Polyline() {
    this.points = new ArrayList<>();
  }

  public List<Point2D> getPoints() {
    if (this.points.isEmpty()) {
      this.populateList();
    }
    return points;
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
          continue;
        }
      }
    }

  }
}

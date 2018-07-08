package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

@XmlRootElement(name = "polyline")
public class Polyline implements Serializable {
  private static final Logger log = Logger.getLogger(Polyline.class.getName());
  private static final long serialVersionUID = -9046398175130339L;

  @XmlAttribute(name = "points")
  private String rawPoints;

  private final transient List<Point2D> points;

  public Polyline() {
    this.points = new ArrayList<>();
  }

  /**
   * Gets all points of a polyline. The points are relative to the x and y
   * coordinate of the parent {@link IMapObject}.
   * 
   * @return A list containing all points of the polyline.
   */
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
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }

  }
}

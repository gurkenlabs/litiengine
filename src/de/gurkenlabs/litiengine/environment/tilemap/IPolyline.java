package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.util.List;

public interface IPolyline {
  /**
   * Gets all points of a polyline. The points are relative to the x and y
   * coordiante of the parent {@link IMapObject}.
   * 
   * @return A list containing all points of the polyline.
   */
  public List<Point2D> getPoints();
}

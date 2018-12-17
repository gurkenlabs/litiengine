package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.util.List;

public interface IPolyline {
  /**
   * Gets all points of a polyline. The points are relative to the x and y
   * coordinate of the parent {@link IMapObject}.
   * 
   * @return A list containing all points of the polyline.
   */
  public List<Point2D> getPoints();

  /**
   * Tests for equality between two polylines. Two polylines are <i>equal</i>
   * if they have the same points.
   * @param anObject The polyline to test equality for
   * @return Whether the two polylines are equal, or {@code false} if {@code
   * anObject} is not a polyline
   */
  public boolean equals(Object anObject);

  /**
   * Computes a hash code for this polyline. A polyline's hash code is equal
   * to the hash code of its points.
   * @return The hash code for this polyline
   */
  public int hashCode();
}

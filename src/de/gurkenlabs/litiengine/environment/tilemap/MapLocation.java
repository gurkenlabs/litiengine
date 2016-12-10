package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class MapLocation implements Serializable {
  private static final long serialVersionUID = -5179086173322235599L;

  private final int mapId;
  private final Point2D point;

  public MapLocation(final int mapId, final Point2D point) {
    this.mapId = mapId;
    this.point = point;
  }

  public int getMapId() {
    return this.mapId;
  }

  public Point2D getPoint() {
    return this.point;
  }
}

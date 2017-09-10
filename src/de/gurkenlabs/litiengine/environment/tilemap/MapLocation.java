package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Point2D;

public class MapLocation {
  private final int mapId;
  private String name;
  private final Point2D point;

  public MapLocation(final int mapId, final Point2D point) {
    this.mapId = mapId;
    this.point = point;
  }

  public int getMapId() {
    return this.mapId;
  }

  public String getName() {
    return this.name;
  }

  public Point2D getPoint() {
    return this.point;
  }

  public void setName(final String name) {
    this.name = name;
  }
}

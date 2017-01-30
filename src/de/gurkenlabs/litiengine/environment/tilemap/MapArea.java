package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.geom.Rectangle2D;

public class MapArea extends Rectangle2D.Double {
  private static final long serialVersionUID = -1620359172839104835L;
  
  private final int id;
  private final String name;

  public MapArea(final int id, final String name, final double x, final double y, final double width, final double height) {
    super(x, y, width, height);
    this.id = id;
    this.name = name;
  }

  public int getMapId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }
}

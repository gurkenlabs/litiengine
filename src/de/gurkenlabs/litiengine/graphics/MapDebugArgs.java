package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;

public class MapDebugArgs {
  private final IMap map;
  private final Graphics2D graphics;

  public MapDebugArgs(final IMap map, final Graphics2D graphics) {
    this.map = map;
    this.graphics = graphics;
  }

  public IMap getMap() {
    return map;
  }

  public Graphics2D getGraphics() {
    return graphics;
  }
}

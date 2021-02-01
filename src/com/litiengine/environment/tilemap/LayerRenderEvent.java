package com.litiengine.environment.tilemap;

import java.awt.Graphics2D;
import java.util.EventObject;

public class LayerRenderEvent extends EventObject {
  private static final long serialVersionUID = -2474186082878891828L;
  private final transient Graphics2D graphics;
  private final transient IMap map;
  private final transient ILayer layer;

  public LayerRenderEvent(final Graphics2D graphics, IMap map, ILayer layer) {
    super(layer);
    this.graphics = graphics;
    this.map = map;
    this.layer = layer;
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  public IMap getMap() {
    return this.map;
  }

  public ILayer getLayer() {
    return this.layer;
  }
}

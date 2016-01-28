/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.utilities.IMapRenderer;

/**
 * The Interface IGraphicsEngine.
 */
public interface IRenderEngine {

  public IMapRenderer getMapRenderer();

  public void onEntityRendered(Consumer<RenderEvent<IEntity>> entity);

  public void onMapRendered(Consumer<RenderEvent<IMap>> map);

  public void renderEntities(Graphics g, List<IEntity> entities);

  public void renderEntities(Graphics g, List<IEntity> entities, IVision vision);

  /**
   * Render map.
   *
   * @param g
   *          the g
   */
  public void renderMap(Graphics g, final IMap map);
}

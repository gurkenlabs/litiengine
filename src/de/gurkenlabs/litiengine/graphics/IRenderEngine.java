/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.MapOrientation;
import de.gurkenlabs.tiled.tmx.utilities.IMapRenderer;

/**
 * The Interface IGraphicsEngine.
 */
public interface IRenderEngine {

  public IMapRenderer getMapRenderer(MapOrientation orientation);

  public void onEntityRendered(Consumer<RenderEvent<IEntity>> entity);

  public void onMapRendered(Consumer<RenderEvent<IMap>> map);

  public void render(Graphics g, List<? extends IRenderable> renderables);

  public void render(Graphics g, List<? extends IRenderable> renderables, IVision vision);

  public void renderEntities(Graphics g, List<? extends IEntity> entities);

  public void renderEntities(Graphics g, List<? extends IEntity> entities, IVision vision);

  public void render(Graphics g, IRenderable renderable);

  public void renderEntity(Graphics g, IEntity entity);

  /**
   * Render map.
   *
   * @param g
   *          the g
   */
  public void renderMap(Graphics g, final IMap map);
}

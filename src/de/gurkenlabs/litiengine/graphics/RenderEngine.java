/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.GraphicConfiguration;
import de.gurkenlabs.litiengine.entities.EntityYComparator;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.tiled.tmx.OrthogonalMapRenderer;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.MapOrientation;
import de.gurkenlabs.tiled.tmx.utilities.IMapRenderer;

/**
 * The Class GraphicsEngine.
 */
public class RenderEngine implements IRenderEngine {
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderedConsumer;

  private final List<Consumer<RenderEvent<IMap>>> mapRenderedConsumer;

  /** The map renderer. */
  private final IMapRenderer mapRenderer;

  /**
   * Instantiates a new graphics engine.
   *
   * @param mapRenderer
   *          the map renderer
   */
  public RenderEngine(final GraphicConfiguration config, final MapOrientation mapOrientation) {
    this.entityRenderedConsumer = new CopyOnWriteArrayList<>();
    this.mapRenderedConsumer = new CopyOnWriteArrayList<>();

    IMapRenderer renderer;
    switch (mapOrientation) {
    case orthogonal:
      renderer = new OrthogonalMapRenderer();
      break;
    default:
      throw new IllegalArgumentException("The map orientation " + mapOrientation + " is not supported!");
    }

    this.mapRenderer = renderer;
  }

  /**
   * Gets the image by the specified relative path.
   *
   * @param absolutPath
   *          the image
   * @return the image
   */
  public static BufferedImage getImage(final String absolutPath) {
    final String cacheKey = absolutPath.hashCode() + "";
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      return ImageCache.IMAGES.get(cacheKey);
    }

    try {
      final InputStream imageFile = new FileInputStream(absolutPath);
      final BufferedImage img = ImageIO.read(imageFile);
      final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice device = env.getDefaultScreenDevice();
      final GraphicsConfiguration config = device.getDefaultConfiguration();
      final BufferedImage compatibleImg = config.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
      compatibleImg.getGraphics().drawImage(img, 0, 0, null);

      ImageCache.IMAGES.putPersistent(cacheKey, compatibleImg);
      return compatibleImg;
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void renderImage(final Graphics g, final Image image, final Point2D renderLocation) {
    final AffineTransform t = new AffineTransform();
    t.translate(renderLocation.getX(), renderLocation.getY());
    t.scale(1, 1);
    ((Graphics2D) g).drawImage(image, t, null);
  }

  @Override
  public IMapRenderer getMapRenderer() {
    return this.mapRenderer;
  }

  @Override
  public void onEntityRendered(final Consumer<RenderEvent<IEntity>> entity) {
    if (!this.entityRenderedConsumer.contains(entity)) {
      this.entityRenderedConsumer.add(entity);
    }
  }

  @Override
  public void onMapRendered(final Consumer<RenderEvent<IMap>> map) {
    if (!this.mapRenderedConsumer.contains(map)) {
      this.mapRenderedConsumer.add(map);
    }
  }

  @Override
  public void render(final Graphics g, final List<? extends IRenderable> renderables) {
    renderables.forEach(r -> r.render(g));
  }

  @Override
  public void render(final Graphics g, final List<? extends IRenderable> renderables, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(vision.getRenderVisionShape());

    renderables.forEach(r -> r.render(g));

    g.setClip(oldClip);
  }

  @Override
  public void renderEntities(final Graphics g, final List<? extends IEntity> entities) {
    // in order to render the entities in a 2.5D manner, we sort them by their
    // max Y Coordinate
    Collections.sort(entities, new EntityYComparator());

    for (final IEntity entity : entities) {
      if (!Game.getScreenManager().getCamera().getViewPort().intersects(entity.getBoundingBox())) {
        continue;
      }

      this.renderEntity(g, entity);
    }
  }

  @Override
  public void renderEntities(final Graphics g, final List<? extends IEntity> entities, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(vision.getRenderVisionShape());

    this.renderEntities(g, entities);

    g.setClip(oldClip);
  }

  @Override
  public void renderEntity(final Graphics g, final IEntity entity) {
    if (entity.getAnimationController() != null) {
      entity.getAnimationController().updateAnimation();

      final BufferedImage img = entity.getAnimationController().getCurrentSprite();
      RenderEngine.renderImage(g, img, Game.getScreenManager().getCamera().getViewPortLocation(entity));
    } else if (entity instanceof IRenderable) {
      ((IRenderable) entity).render(g);
    } else {
      return;
    }

    for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderedConsumer) {
      consumer.accept(new RenderEvent<IEntity>(g, entity));
    }
  }

  /**
   * Draws the tile layers of the mapcontainer and the animations.
   *
   * @param g
   *          the g
   * @see de.gurkenlabs.litiengine.graphics.IRenderEngine#renderMap(java.awt.Graphics)
   */
  @Override
  public void renderMap(final Graphics g, final IMap map) {
    if (map == null) {
      return;
    }

    // draw tile layers
    this.mapRenderer.render(g, Game.getScreenManager().getCamera().getViewPortLocation(0, 0), map);

    for (final Consumer<RenderEvent<IMap>> consumer : this.mapRenderedConsumer) {
      consumer.accept(new RenderEvent<IMap>(g, map));
    }
  }
}

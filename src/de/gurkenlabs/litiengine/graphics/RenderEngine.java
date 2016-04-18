/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.Game;
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
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderingConsumer;
  private final List<Predicate<RenderEvent<IEntity>>> entityRenderingConditions;
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderedConsumer;

  private final List<Consumer<RenderEvent<IMap>>> mapRenderedConsumer;

  /** The map renderer. */
  private final Map<MapOrientation, IMapRenderer> mapRenderer;

  /**
   * Instantiates a new graphics engine.
   *
   * @param mapRenderer
   *          the map renderer
   */
  public RenderEngine() {
    this.entityRenderedConsumer = new CopyOnWriteArrayList<>();
    this.entityRenderingConsumer = new CopyOnWriteArrayList<>();
    this.entityRenderingConditions = new CopyOnWriteArrayList<>();
    this.mapRenderedConsumer = new CopyOnWriteArrayList<>();
    this.mapRenderer = new HashMap<>();

    this.mapRenderer.put(MapOrientation.orthogonal, new OrthogonalMapRenderer());
  }

  /**
   * Gets the image by the specified relative path. This method supports both,
   * loading images from a folder and loading them from the resources.
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

    // try to get image from resource folder first and as a fallback get it from
    // a normal folder
    BufferedImage img;
    try {
      InputStream imageFromResource = ClassLoader.getSystemResourceAsStream(absolutPath);
      if (imageFromResource != null) {
        img = ImageIO.read(imageFromResource);
      } else {
        try (final InputStream imageFile = new FileInputStream(absolutPath)) {
          img = ImageIO.read(imageFile);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice device = env.getDefaultScreenDevice();
    final GraphicsConfiguration config = device.getDefaultConfiguration();
    final BufferedImage compatibleImg = config.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
    compatibleImg.getGraphics().drawImage(img, 0, 0, null);

    ImageCache.IMAGES.putPersistent(cacheKey, compatibleImg);
    return compatibleImg;
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation) {
    final AffineTransform t = new AffineTransform();
    t.translate(renderLocation.getX(), renderLocation.getY());
    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, final float angle) {
    final AffineTransform t = new AffineTransform();

    t.translate(renderLocation.getX(), renderLocation.getY());
    t.rotate(Math.toRadians(angle), image.getWidth(null) * 0.5, image.getHeight(null) * 0.5);

    g.drawImage(image, t, null);
  }

  public static void drawText(final Graphics2D g, final String text, final double x, final double y) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    g.drawString(text, (int) x, (int) y);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  public static void drawTextWithShadow(final Graphics2D g, final String text, final double x, final double y, Color shadow) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    Color old = g.getColor();
    g.setColor(shadow);
    g.drawString(text, (int) x + 1, (int) y + 1);
    g.drawString(text, (int) x + 1, (int) y - 1);
    g.drawString(text, (int) x - 1, (int) y - 1);
    g.drawString(text, (int) x - 1, (int) y + 1);
    g.setColor(old);
    g.drawString(text, (int) x, (int) y);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  public static BufferedImage createCompatibleImage(final int width, final int height) {
    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice device = env.getDefaultScreenDevice();
    final GraphicsConfiguration config = device.getDefaultConfiguration();
    final BufferedImage img = config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);

    return img;
  }

  @Override
  public IMapRenderer getMapRenderer(final MapOrientation mapOrientation) {
    if (!this.mapRenderer.containsKey(mapOrientation)) {
      throw new IllegalArgumentException("The map orientation " + mapOrientation + " is not supported!");
    }

    return this.mapRenderer.get(mapOrientation);
  }

  @Override
  public void onEntityRendered(final Consumer<RenderEvent<IEntity>> entity) {
    if (!this.entityRenderedConsumer.contains(entity)) {
      this.entityRenderedConsumer.add(entity);
    }
  }

  @Override
  public void onEntityRendering(final Consumer<RenderEvent<IEntity>> entity) {
    if (!this.entityRenderingConsumer.contains(entity)) {
      this.entityRenderingConsumer.add(entity);
    }
  }

  @Override
  public void onMapRendered(final Consumer<RenderEvent<IMap>> map) {
    if (!this.mapRenderedConsumer.contains(map)) {
      this.mapRenderedConsumer.add(map);
    }
  }

  @Override
  public void entityRenderingCondition(Predicate<RenderEvent<IEntity>> predicate) {
    if (!this.entityRenderingConditions.contains(predicate)) {
      this.entityRenderingConditions.add(predicate);
    }
  }

  @Override
  public void render(final Graphics2D g, final List<? extends IRenderable> renderables) {
    renderables.forEach(r -> this.render(g, r));
  }

  @Override
  public void render(final Graphics2D g, final List<? extends IRenderable> renderables, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(vision.getRenderVisionShape());

    renderables.forEach(r -> r.render(g));

    g.setClip(oldClip);
  }

  @Override
  public void renderEntities(final Graphics2D g, final List<? extends IEntity> entities) {
    // in order to render the entities in a 2.5D manner, we sort them by their
    // max Y Coordinate

    // TODO: THIS COSTS THE MOST TIME OF THE RENDERING LOOP... MAYBE USE A
    // BETTER DATASTRUCTURE FOR THE (HEAP)
    // AND UPDATE THE HEAP WHENEVER AN ENTITY MOVES.
    Collections.sort(entities, new EntityYComparator());

    for (final IEntity entity : entities) {
      this.renderEntity(g, entity);
    }
  }

  @Override
  public void renderEntities(final Graphics2D g, final List<? extends IEntity> entities, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    if (vision != null) {
      g.setClip(vision.getRenderVisionShape());
    }

    this.renderEntities(g, entities);

    g.setClip(oldClip);
  }

  @Override
  public void render(final Graphics2D g, final IRenderable renderable) {
    if (renderable == null) {
      return;
    }

    renderable.render(g);
  }

  @Override
  public void renderEntity(final Graphics2D g, final IEntity entity) {
    if (entity == null) {
      return;
    }

    if (!Game.getScreenManager().getCamera().getViewPort().intersects(entity.getBoundingBox())) {
      return;
    }

    for (final Predicate<RenderEvent<IEntity>> consumer : this.entityRenderingConditions) {
      if (!consumer.test(new RenderEvent<IEntity>(g, entity))) {
        return;
      }
    }

    for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderingConsumer) {
      consumer.accept(new RenderEvent<IEntity>(g, entity));
    }

    boolean rendered = false;
    if (entity.getAnimationController() != null) {
      final BufferedImage img = entity.getAnimationController().getCurrentSprite();
      renderImage(g, img, Game.getScreenManager().getCamera().getViewPortLocation(entity));
      rendered = true;
    }

    if (entity instanceof IRenderable) {
      ((IRenderable) entity).render(g);
      rendered = true;
    }

    if (rendered) {
      for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderedConsumer) {
        consumer.accept(new RenderEvent<IEntity>(g, entity));
      }
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
  public void renderMap(final Graphics2D g, final IMap map) {
    if (map == null) {
      return;
    }

    // draw tile layers
    this.mapRenderer.get(map.getOrientation()).render(g, Game.getScreenManager().getCamera().getViewPortLocation(0, 0), map);

    for (final Consumer<RenderEvent<IMap>> consumer : this.mapRenderedConsumer) {
      consumer.accept(new RenderEvent<IMap>(g, map));
    }
  }
}

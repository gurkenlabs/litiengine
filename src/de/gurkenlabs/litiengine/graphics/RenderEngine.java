/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityYComparator;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.OrthogonalMapRenderer;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

/**
 * The Class GraphicsEngine.
 */
public final class RenderEngine implements IRenderEngine {

  public static BufferedImage createCompatibleImage(final int width, final int height) {
    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice device = env.getDefaultScreenDevice();
    final GraphicsConfiguration config = device.getDefaultConfiguration();
    return config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
  }

  /**
   * Draws the given string to the specified map location.
   *
   * @param g
   * @param text
   * @param x
   * @param y
   */
  public static void drawMapText(final Graphics2D g, final String text, final double x, final double y) {
    if (text == null || text.isEmpty()) {
      return;
    }

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    final Point2D viewPortLocation = Game.getScreenManager().getCamera().getViewPortLocation(x, y);
    g.drawString(text, (int) viewPortLocation.getX() * Game.getInfo().getRenderScale(), (int) viewPortLocation.getY() * Game.getInfo().getRenderScale());
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  public static void drawMapText(final Graphics2D g, final String text, final Point2D location) {
    drawMapText(g, text, location.getX(), location.getY());
  }

  public static void drawRotatedText(final Graphics2D g, final double x, final double y, final int angle, final String text) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.rotate(Math.toRadians(angle), x, y);
    RenderEngine.drawText(g2, text, x, y);
    g2.dispose();

  }

  public static void drawShape(final Graphics2D g, final Shape shape) {
    drawShape(g, shape, new BasicStroke(1 / Game.getInfo().getRenderScale()));
  }

  public static void drawShape(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }

    final AffineTransform oldTransForm = g.getTransform();
    final Stroke oldStroke = g.getStroke();
    final AffineTransform t = new AffineTransform();
    t.scale(Game.getInfo().getRenderScale(), Game.getInfo().getRenderScale());
    t.translate(Game.getScreenManager().getCamera().getPixelOffsetX(), Game.getScreenManager().getCamera().getPixelOffsetY());

    g.setTransform(t);
    g.setStroke(stroke);
    g.draw(shape);
    g.setTransform(oldTransForm);
    g.setStroke(oldStroke);
  }

  public static void drawText(final Graphics2D g, final String text, final double x, final double y) {
    if (text == null || text.isEmpty()) {
      return;
    }

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    g.drawString(text, (float) x, (float) y);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  /**
   * PERFORMANCE HINT: The larger the text is, the more time it needs to render
   * especially with antialiasing turned on.
   *
   * @param g
   * @param text
   * @param x
   * @param y
   * @param shadow
   */
  public static void drawTextWithShadow(final Graphics2D g, final String text, final double x, final double y, final Color shadow) {
    if (text == null || text.isEmpty()) {
      return;
    }

    final Color old = g.getColor();
    g.setColor(shadow);
    g.drawString(text, (float) x + 1, (float) y + 1);
    g.drawString(text, (float) x + 1, (float) y - 1);
    g.drawString(text, (float) x - 1, (float) y - 1);
    g.drawString(text, (float) x - 1, (float) y + 1);
    g.setColor(old);
    g.drawString(text, (float) x, (float) y);
  }

  public static void fillShape(final Graphics2D g, final Shape shape) {
    if (shape == null) {
      return;
    }

    final AffineTransform oldTransForm = g.getTransform();
    final AffineTransform t = new AffineTransform();
    t.scale(Game.getInfo().getRenderScale(), Game.getInfo().getRenderScale());
    t.translate(Game.getScreenManager().getCamera().getPixelOffsetX(), Game.getScreenManager().getCamera().getPixelOffsetY());

    g.setTransform(t);
    g.setStroke(new BasicStroke(1 / Game.getInfo().getRenderScale()));
    g.fill(shape);
    g.setTransform(oldTransForm);
  }

  public static BufferedImage getImage(final String absolutPath) {
    return getImage(absolutPath, false);
  }

  /**
   * Gets the image by the specified relative path. This method supports both,
   * loading images from a folder and loading them from the resources.
   *
   * @param absolutPath
   *          the image
   * @return the image
   */
  public static BufferedImage getImage(final String absolutPath, final boolean forceLoad) {
    if (absolutPath == null || absolutPath.isEmpty()) {
      return null;
    }

    final String cacheKey = Integer.toString(absolutPath.hashCode());
    if (!forceLoad && ImageCache.IMAGES.containsKey(cacheKey)) {
      return ImageCache.IMAGES.get(cacheKey);
    }

    // try to get image from resource folder first and as a fallback get it from
    // a normal folder
    BufferedImage img = null;
    final InputStream imageFile = FileUtilities.getGameResource(absolutPath);
    if (imageFile != null) {
      try {
        img = ImageIO.read(imageFile);
      } catch (final IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    if (img == null) {
      return null;
    }

    final BufferedImage compatibleImg = ImageProcessing.getCompatibleImage(img.getWidth(), img.getHeight());
    compatibleImg.createGraphics().drawImage(img, 0, 0, null);

    ImageCache.IMAGES.put(cacheKey, compatibleImg);
    return compatibleImg;
  }

  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y) {
    if (image == null) {
      return;
    }

    final AffineTransform t = new AffineTransform();
    t.translate(x, y);
    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y, final float angle) {
    if (image == null) {
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.rotate(Math.toRadians(angle), image.getWidth(null) * 0.5, image.getHeight(null) * 0.5);

    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY());
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, final float angle) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY(), angle);
  }

  private final EntityYComparator entityComparator;

  private final List<Consumer<RenderEvent<IEntity>>> entityRenderedConsumer;

  private final List<Predicate<IEntity>> entityRenderingConditions;

  private final List<Consumer<RenderEvent<IEntity>>> entityRenderingConsumer;

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
    this.entityComparator = new EntityYComparator();

    this.mapRenderer.put(MapOrientation.orthogonal, new OrthogonalMapRenderer());
  }

  @Override
  public boolean canRender(final IEntity entity) {
    if (!this.entityRenderingConditions.isEmpty()) {
      for (final Predicate<IEntity> consumer : this.entityRenderingConditions) {
        if (!consumer.test(entity)) {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void entityRenderingCondition(final Predicate<IEntity> predicate) {
    if (!this.entityRenderingConditions.contains(predicate)) {
      this.entityRenderingConditions.add(predicate);
    }
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
  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables) {
    renderables.forEach(r -> this.render(g, r));
  }

  @Override
  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(vision.getRenderVisionShape());

    renderables.forEach(r -> r.render(g));

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
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities) {
    this.renderEntities(g, entities, true);
  }

  @Override
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final boolean sort) {
    // in order to render the entities in a 2.5D manner, we sort them by their
    // max Y Coordinate

    final List<? extends IEntity> entitiesToRender = entities.stream().filter(x -> Game.getScreenManager().getCamera().getViewPort().intersects(x.getBoundingBox())).collect(Collectors.toList());

    if (sort) {
      // TODO: THIS COSTS THE MOST TIME OF THE RENDERING LOOP... MAYBE USE A
      // BETTER DATASTRUCTURE FOR THE (HEAP)
      // AND UPDATE THE HEAP WHENEVER AN ENTITY MOVES.
      try {
        Collections.sort(entitiesToRender, this.entityComparator);
      } catch (final IllegalArgumentException e) {
        for (final IEntity entity : entities) {
          this.renderEntity(g, entity);
        }

        return;
      }
    }

    for (final IEntity entity : entitiesToRender) {
      this.renderEntity(g, entity);
    }
  }

  @Override
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final boolean sort, final IVision vision) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    if (vision != null) {
      g.setClip(vision.getRenderVisionShape());
    }

    this.renderEntities(g, entities, sort);

    g.setClip(oldClip);
  }

  @Override
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final IVision vision) {
    this.renderEntities(g, entities, true, vision);
  }

  @Override
  public void renderEntity(final Graphics2D g, final IEntity entity) {
    if (entity == null) {
      return;
    }

    if (entity.getRenderType() == RenderType.NONE || !this.canRender(entity)) {
      return;
    }
    final RenderEvent<IEntity> renderEvent = new RenderEvent<>(g, entity);
    if (!this.entityRenderingConsumer.isEmpty()) {
      for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderingConsumer) {
        consumer.accept(renderEvent);
      }
    }

    final IAnimationController animationController = Game.getEntityControllerManager().getAnimationController(entity);
    if (animationController != null) {
      final BufferedImage img = animationController.getCurrentSprite();
      renderImage(g, img, Game.getScreenManager().getCamera().getViewPortLocation(entity));
    }

    if (entity instanceof IRenderable) {
      ((IRenderable) entity).render(g);
    }

    if (!this.entityRenderedConsumer.isEmpty()) {
      for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderedConsumer) {
        consumer.accept(renderEvent);
      }
    }
  }

  @Override
  public void renderLayers(final Graphics2D g, final IMap map, final RenderType type) {
    if (map == null) {
      return;
    }

    // draw tile layers
    this.mapRenderer.get(map.getOrientation()).renderOverlay(g, map, Game.getScreenManager().getCamera().getViewPort());

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
    this.mapRenderer.get(map.getOrientation()).render(g, map, Game.getScreenManager().getCamera().getViewPort());

    for (final Consumer<RenderEvent<IMap>> consumer : this.mapRenderedConsumer) {
      consumer.accept(new RenderEvent<>(g, map));
    }
  }
}

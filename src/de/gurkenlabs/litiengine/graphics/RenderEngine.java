package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityYComparator;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.OrthogonalMapRenderer;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;

public final class RenderEngine implements IRenderEngine {
  private static final float DEFAULT_RENDERSCALE = 3.0f;

  private final EntityYComparator entityComparator;
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderedConsumer;
  private final List<Predicate<IEntity>> entityRenderingConditions;
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderingConsumer;
  private final EnumMap<MapOrientation, IMapRenderer> mapRenderer;

  private float baseRenderScale;

  public RenderEngine() {
    this.entityRenderedConsumer = new CopyOnWriteArrayList<>();
    this.entityRenderingConsumer = new CopyOnWriteArrayList<>();
    this.entityRenderingConditions = new CopyOnWriteArrayList<>();
    this.mapRenderer = new EnumMap<>(MapOrientation.class);
    this.entityComparator = new EntityYComparator();

    this.mapRenderer.put(MapOrientation.ORTHOGONAL, new OrthogonalMapRenderer());

    this.baseRenderScale = DEFAULT_RENDERSCALE;
  }

  /**
   * Draws the given string to the specified map location.
   *
   * @param g
   *          The graphics object to draw on.
   * @param text
   *          The text to be drawn
   * @param x
   *          The x-coordinate of the text.
   * @param y
   *          The y-coordinate of the text
   */
  @Override
  public void renderText(final Graphics2D g, final String text, final double x, final double y) {
    if (text == null || text.isEmpty()) {
      return;
    }

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    final Point2D viewPortLocation = Game.getCamera().getViewPortLocation(x, y);
    g.drawString(text, (float) viewPortLocation.getX() * Game.getCamera().getRenderScale(), (float) viewPortLocation.getY() * Game.getCamera().getRenderScale());
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  @Override
  public void renderText(final Graphics2D g, final String text, final Point2D location) {
    this.renderText(g, text, location.getX(), location.getY());
  }

  public static void drawRotatedText(final Graphics2D g, final double x, final double y, final int angle, final String text) {
    // TODO: does this actually work as expected if we're rendering on the copy?
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.rotate(Math.toRadians(angle), x, y);
    drawText(g2, text, x, y);
    g2.dispose();
  }

  @Override
  public void renderShape(final Graphics2D g, final Shape shape) {
    if (shape == null) {
      return;
    }

    final AffineTransform oldTransForm = g.getTransform();
    final AffineTransform t = new AffineTransform();
    t.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());
    t.translate(Game.getCamera().getPixelOffsetX(), Game.getCamera().getPixelOffsetY());

    g.setTransform(t);
    g.fill(shape);
    g.setTransform(oldTransForm);
  }

  @Override
  public void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, new BasicStroke(1 / Game.getCamera().getRenderScale()));
  }

  @Override
  public void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }

    final AffineTransform oldTransForm = g.getTransform();
    final Stroke oldStroke = g.getStroke();
    final AffineTransform t = new AffineTransform();
    t.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());
    t.translate(Game.getCamera().getPixelOffsetX(), Game.getCamera().getPixelOffsetY());

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
   *          The graphics object to draw on.
   * @param text
   *          The text to be drawn
   * @param x
   *          The x-coordinate of the text.
   * @param y
   *          The y-coordinate of the text
   * 
   * @param shadow
   *          The color of the shadow to be drawn
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

  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y) {
    if (image == null) {
      return;
    }

    final AffineTransform t = AffineTransform.getTranslateInstance(x, y);
    g.drawImage(image, t, null);
  }

  /***
   * Note that rotating an image with 90/180/270 degree is way more performant.
   * than rotating with in other degrees.
   * 
   * @param g
   *          The graphics object to draw on.
   * @param image
   *          The image to be drawn
   * @param x
   *          The x-coordinate of the image.
   * @param y
   *          The y-coordinate of the image
   * @param angle
   *          The angle by which the image will be rotated.s
   */
  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y, final double angle) {
    if (image == null) {
      return;
    }

    if (angle == 0 || angle % 360 == 0) {
      renderImage(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.rotate(Math.toRadians(angle), image.getWidth(null) * 0.5, image.getHeight(null) * 0.5);

    g.drawImage(image, t, null);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final double x, final double y, final double scale) {
    renderScaledImage(g, image, x, y, scale, scale);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final Point2D location, final double scale) {
    renderScaledImage(g, image, location.getX(), location.getY(), scale, scale);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final Point2D location, final double scaleX, final double scaleY) {
    renderScaledImage(g, image, location.getX(), location.getY(), scaleX, scaleY);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final double x, final double y, final double scaleX, final double scaleY) {
    if (image == null) {
      return;
    }

    if (scaleX == 1 && scaleY == 1) {
      renderImage(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.scale(scaleX, scaleY);

    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY());
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, final double angle) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY(), angle);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, AffineTransform transform) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY(), transform);
  }

  public static void renderImage(final Graphics2D g, final Image image, double x, double y, AffineTransform transform) {
    if (transform == null) {
      renderImage(g, image, x, y);
      return;
    }

    AffineTransform t = new AffineTransform();
    t.translate(x, y);
    t.concatenate(transform);

    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, AffineTransform transform) {
    if (transform == null) {
      return;
    }

    g.drawImage(image, transform, null);
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
  public float getBaseRenderScale() {
    return this.baseRenderScale;
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
  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables) {
    renderables.forEach(r -> this.render(g, r));
  }

  @Override
  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables, final Shape clip) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(clip);

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

    final List<? extends IEntity> entitiesToRender = entities.stream().filter(x -> Game.getCamera().getViewPort().intersects(x.getBoundingBox())).collect(Collectors.toList());

    if (sort) {
      // THIS COSTS THE MOST TIME OF THE RENDERING LOOP... MAYBE USE A
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
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final boolean sort, final Shape clip) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    if (clip != null) {
      g.setClip(clip);
    }

    this.renderEntities(g, entities, sort);

    g.setClip(oldClip);
  }

  @Override
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final Shape clip) {
    this.renderEntities(g, entities, true, clip);
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

    final IAnimationController animationController = entity.getAnimationController();
    if (animationController != null) {
      final BufferedImage img = animationController.getCurrentSprite();
      if (img == null) {
        return;
      }

      if (animationController instanceof IEntityAnimationController && ((IEntityAnimationController) animationController).isAutoScaling()) {
        final double ratioX = entity.getWidth() / img.getWidth();
        final double ratioY = entity.getHeight() / img.getHeight();
        renderScaledImage(g, img, Game.getCamera().getViewPortLocation(entity.getLocation()), ratioX, ratioY);
      } else {
        float deltaX = (entity.getWidth() - img.getWidth()) / 2.0f;
        float deltaY = (entity.getHeight() - img.getHeight()) / 2.0f;

        renderImage(g, img, Game.getCamera().getViewPortLocation(entity.getX() + deltaX, entity.getY() + deltaY), animationController.getAffineTransform());
      }
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
  public void render(final Graphics2D g, final IMap map, final RenderType... renderTypes) {
    if (map == null) {
      return;
    }

    // draw layers
    this.mapRenderer.get(map.getOrientation()).render(g, map, Game.getCamera().getViewPort(), renderTypes);
  }

  @Override
  public void setBaseRenderScale(float scale) {
    this.baseRenderScale = scale;
  }
}

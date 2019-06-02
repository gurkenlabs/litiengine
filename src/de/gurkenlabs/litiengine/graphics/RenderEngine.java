package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityYComparator;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;

/**
 * The 2D Render Engine is used to render texts, shapes and entities at their location in the
 * <code>Environment</code> and with respect to the <code>Camera</code> location and zoom.
 * 
 * <p>
 * <i>Internally, it uses the static renderer implementations to actually execute the rendering process.
 * This class basically prepares the specified render subject and passed them to a renderer with the current correct context.</i>
 * </p>
 * 
 * @see GameWorld#environment()
 * @see GameWorld#camera()
 * @see IEntity#getLocation()
 * @see ShapeRenderer
 * @see TextRenderer
 * @see ImageRenderer
 */
public final class RenderEngine {
  public static final float DEFAULT_RENDERSCALE = 3.0f;

  private final EntityYComparator entityComparator = new EntityYComparator();
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderedConsumer = new CopyOnWriteArrayList<>();
  private final List<Predicate<IEntity>> entityRenderingConditions = new CopyOnWriteArrayList<>();
  private final List<Consumer<RenderEvent<IEntity>>> entityRenderingConsumer = new CopyOnWriteArrayList<>();

  private float baseRenderScale = DEFAULT_RENDERSCALE;

  /**
   * Instantiates a new RenderEngine instance.
   * 
   * <p>
   * <b>You should never call this manually! Instead use the <code>Game.graphics()</code> instance.</b>
   * </p>
   * 
   * @see Game#graphics()
   */
  public RenderEngine() {
    if (Game.graphics() != null) {
      throw new UnsupportedOperationException("Never initialize a RenderEngine manually. Use Game.graphics() instead.");
    }
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
   * @param antialias
   *          Configure whether or not to render the text with antialiasing.
   */
  public static void renderText(final Graphics2D g, final String text, final double x, final double y, boolean antialias) {
    if (text == null || text.isEmpty()) {
      return;
    }

    final Point2D viewPortLocation = Game.world().camera().getViewportLocation(x, y);
    double viewPortX = (float) viewPortLocation.getX() * Game.world().camera().getRenderScale();
    double yiewPortY = (float) viewPortLocation.getY() * Game.world().camera().getRenderScale();

    TextRenderer.render(g, text, viewPortX, yiewPortY, antialias);
  }

  public static void renderText(final Graphics2D g, final String text, final double x, final double y) {
    renderText(g, text, x, y, false);
  }

  public static void renderText(final Graphics2D g, final String text, final Point2D location, boolean antialias) {
    renderText(g, text, location.getX(), location.getY(), antialias);
  }

  public static void renderText(final Graphics2D g, final String text, final Point2D location) {
    renderText(g, text, location, false);
  }

  public static void renderShape(final Graphics2D g, final Shape shape) {
    renderShape(g, shape, false);
  }

  public static void renderShape(final Graphics2D g, final Shape shape, boolean antialiasing) {
    if (shape == null) {
      return;
    }

    Object hint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    final AffineTransform t = new AffineTransform();
    t.scale(Game.world().camera().getRenderScale(), Game.world().camera().getRenderScale());
    t.translate(Game.world().camera().getPixelOffsetX(), Game.world().camera().getPixelOffsetY());

    ShapeRenderer.renderTransformed(g, shape, t);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
  }

  public static void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, new BasicStroke(1 / Game.world().camera().getRenderScale()));
  }

  public static void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }

    final AffineTransform t = new AffineTransform();
    t.scale(Game.world().camera().getRenderScale(), Game.world().camera().getRenderScale());
    t.translate(Game.world().camera().getPixelOffsetX(), Game.world().camera().getPixelOffsetY());

    ShapeRenderer.renderOutlineTransformed(g, shape, t, stroke);
  }

  public static void renderImage(Graphics2D g, final Image image, double x, double y) {
    renderImage(g, image, new Point2D.Double(x, y));
  }

  public static void renderImage(Graphics2D g, final Image image, Point2D location) {
    Point2D viewPortLocation = Game.world().camera().getViewportLocation(location);
    ImageRenderer.render(g, image, viewPortLocation.getX() * Game.world().camera().getRenderScale(), viewPortLocation.getY() * Game.world().camera().getRenderScale());
  }

  public boolean canRender(final IEntity entity) {
    for (final Predicate<IEntity> consumer : this.entityRenderingConditions) {
      if (!consumer.test(entity)) {
        return false;
      }
    }

    return true;
  }

  public void entityRenderingCondition(final Predicate<IEntity> predicate) {
    if (!this.entityRenderingConditions.contains(predicate)) {
      this.entityRenderingConditions.add(predicate);
    }
  }

  /**
   * Gets the base render scale of the game.
   * 
   * @return The base render scale.
   */
  public float getBaseRenderScale() {
    return this.baseRenderScale;
  }

  public void onEntityRendered(final Consumer<RenderEvent<IEntity>> entity) {
    if (!this.entityRenderedConsumer.contains(entity)) {
      this.entityRenderedConsumer.add(entity);
    }
  }

  public void onEntityRendering(final Consumer<RenderEvent<IEntity>> entity) {
    if (!this.entityRenderingConsumer.contains(entity)) {
      this.entityRenderingConsumer.add(entity);
    }
  }

  public static void render(final Graphics2D g, final Collection<? extends IRenderable> renderables) {
    renderables.forEach(r -> render(g, r));
  }

  public static void render(final Graphics2D g, final Collection<? extends IRenderable> renderables, final Shape clip) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(clip);

    renderables.forEach(r -> r.render(g));

    g.setClip(oldClip);
  }

  public static void render(final Graphics2D g, final IRenderable renderable) {
    if (renderable == null) {
      return;
    }

    renderable.render(g);
  }

  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities) {
    this.renderEntities(g, entities, true);
  }

  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final boolean sort) {
    // in order to render the entities in a 2.5D manner, we sort them by their
    // max Y Coordinate

    final List<? extends IEntity> entitiesToRender = entities.stream().filter(x -> Game.world().camera().getViewport().intersects(x.getBoundingBox())).collect(Collectors.toList());

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

  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final boolean sort, final Shape clip) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    if (clip != null) {
      g.setClip(clip);
    }

    this.renderEntities(g, entities, sort);

    g.setClip(oldClip);
  }

  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities, final Shape clip) {
    this.renderEntities(g, entities, true, clip);
  }

  public void renderEntity(final Graphics2D g, final IEntity entity) {
    if (entity == null) {
      return;
    }

    if (entity.getRenderType() == RenderType.NONE || !this.canRender(entity)) {
      return;
    }
    final RenderEvent<IEntity> renderEvent = new RenderEvent<>(g, entity);
    for (final Consumer<RenderEvent<IEntity>> consumer : this.entityRenderingConsumer) {
      consumer.accept(renderEvent);
    }

    final IEntityAnimationController<?> animationController = entity.getAnimationController();
    if (animationController != null) {
      final BufferedImage img = animationController.getCurrentSprite();
      if (img == null) {
        return;
      }

      if (animationController.isAutoScaling()) {
        final double ratioX = entity.getWidth() / img.getWidth();
        final double ratioY = entity.getHeight() / img.getHeight();
        ImageRenderer.renderScaled(g, img, Game.world().camera().getViewportLocation(entity.getLocation()), ratioX, ratioY);
      } else {
        double deltaX = (entity.getWidth() - img.getWidth()) / 2.0;
        double deltaY = (entity.getHeight() - img.getHeight()) / 2.0;

        ImageRenderer.renderTransformed(g, img, Game.world().camera().getViewportLocation(entity.getX() + deltaX, entity.getY() + deltaY), animationController.getAffineTransform());
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

  /**
   * Sets the global base scale that is used to calculate the actual render scale of the game.
   * 
   * @param scale
   *          The base render scale for the game.
   * 
   * @see ICamera#getRenderScale()
   */
  public void setBaseRenderScale(float scale) {
    this.baseRenderScale = scale;
  }
}

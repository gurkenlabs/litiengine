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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityRenderEvent;
import de.gurkenlabs.litiengine.entities.EntityRenderListener;
import de.gurkenlabs.litiengine.entities.EntityRenderedListener;
import de.gurkenlabs.litiengine.entities.EntityYComparator;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
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
  private final List<EntityRenderedListener> entityRenderedListener = new CopyOnWriteArrayList<>();
  private final List<EntityRenderListener> entityRenderListener = new CopyOnWriteArrayList<>();

  private float baseRenderScale = DEFAULT_RENDERSCALE;

  /**
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
   * Adds the specified entity rendered listener to receive events when entities were rendered.
   * 
   * <p>
   * This is the global equivalent to <code>IEntity.addEntityRenderedListener</code>
   * </p>
   * 
   * @param listener
   *          The listener to add.
   * 
   * @see IEntity#onRendered(EntityRenderedListener)
   */
  public void addEntityRenderedListener(final EntityRenderedListener listener) {
    this.entityRenderedListener.add(listener);
  }

  /**
   * Removes the specified entity rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeEntityRenderedListener(final EntityRenderedListener listener) {
    this.entityRenderedListener.remove(listener);
  }

  /**
   * Adds the specified entity render listener to receive events and callbacks about the rendering process of entities.
   * 
   * <p>
   * This is the global equivalent to <code>IEntity.addEntityRenderListener</code>
   * </p>
   * 
   * @param listener
   *          The listener to add.
   * @see IEntity#addEntityRenderListener(EntityRenderListener)
   */
  public void addEntityRenderListener(final EntityRenderListener listener) {
    this.entityRenderListener.add(listener);
  }

  /**
   * Removes the specified entity render listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeEntityRenderListener(final EntityRenderListener listener) {
    this.entityRenderListener.remove(listener);
  }

  /**
   * Gets the base render scale of the game.
   * 
   * @return The base render scale.
   */
  public float getBaseRenderScale() {
    return this.baseRenderScale;
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

  /**
   * Renders the specified text to the defined map location.
   *
   * @param g
   *          The graphics object to render on.
   * @param text
   *          The text to be rendered
   * @param x
   *          The x-coordinate of the text.
   * @param y
   *          The y-coordinate of the text
   * @param antialias
   *          Configure whether or not to render the text with antialiasing.
   */
  public void renderText(final Graphics2D g, final String text, final double x, final double y, boolean antialias) {
    if (text == null || text.isEmpty()) {
      return;
    }

    final Point2D viewPortLocation = Game.world().camera().getViewportLocation(x, y);
    double viewPortX = (float) viewPortLocation.getX() * Game.world().camera().getRenderScale();
    double yiewPortY = (float) viewPortLocation.getY() * Game.world().camera().getRenderScale();

    TextRenderer.render(g, text, viewPortX, yiewPortY, antialias);
  }

  /**
   * Renders the specified text to the defined map location.
   *
   * @param g
   *          The graphics object to render on.
   * @param text
   *          The text to be rendered
   * @param x
   *          The x-coordinate of the text.
   * @param y
   *          The y-coordinate of the text
   */
  public void renderText(final Graphics2D g, final String text, final double x, final double y) {
    renderText(g, text, x, y, false);
  }

  /**
   * Renders the specified text to the defined map location.
   *
   * @param g
   *          The graphics object to render on.
   * @param text
   *          The text to be rendered.
   * @param location
   *          The location on the map.
   * @param antialias
   *          Configure whether or not to render the text with antialiasing.
   */
  public void renderText(final Graphics2D g, final String text, final Point2D location, boolean antialias) {
    renderText(g, text, location.getX(), location.getY(), antialias);
  }

  /**
   * Renders the specified text to the defined map location.
   *
   * @param g
   *          The graphics object to render on.
   * @param text
   *          The text to be rendered.
   * @param location
   *          The location on the map.
   */
  public void renderText(final Graphics2D g, final String text, final Point2D location) {
    renderText(g, text, location, false);
  }

  /**
   * Renders the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   */
  public void renderShape(final Graphics2D g, final Shape shape) {
    renderShape(g, shape, false);
  }

  /**
   * Renders the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   * @param antialiasing
   *          Configure whether or not to render the shape with antialiasing.
   */
  public void renderShape(final Graphics2D g, final Shape shape, boolean antialiasing) {
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

  /**
   * Renders the outline of the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   */
  public void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, new BasicStroke(1 / Game.world().camera().getRenderScale()));
  }

  /**
   * Renders the outline of the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   * @param antialiasing
   *          Configure whether or not to render the shape with antialiasing.
   */
  public void renderOutline(final Graphics2D g, final Shape shape, boolean antialiasing) {
    renderOutline(g, shape, new BasicStroke(1 / Game.world().camera().getRenderScale()), antialiasing);
  }

  /**
   * Renders the outline with the defined <code>Stroke</code> of the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   * @param stroke
   *          The stroke that is used to render the shape.
   * 
   * @see Stroke
   */
  public void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    this.renderOutline(g, shape, stroke, false);
  }

  /**
   * Renders the outline with the defined <code>Stroke</code> of the specified shape to the translated location in the game world.
   * 
   * @param g
   *          The graphics object to render on.
   * @param shape
   *          The shape to be rendered.
   * @param stroke
   *          The stroke that is used to render the shape.
   * @param antialiasing
   *          Configure whether or not to render the shape with antialiasing.
   * 
   * @see Stroke
   */
  public void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke, boolean antialiasing) {
    if (shape == null) {
      return;
    }

    Object hint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    final AffineTransform t = new AffineTransform();
    t.scale(Game.world().camera().getRenderScale(), Game.world().camera().getRenderScale());
    t.translate(Game.world().camera().getPixelOffsetX(), Game.world().camera().getPixelOffsetY());

    ShapeRenderer.renderOutlineTransformed(g, shape, t, stroke);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
  }

  /**
   * Renders the specified image at the defined map location.
   * 
   * @param g
   *          The graphics object to render on.
   * @param image
   *          The image to be rendered.
   * @param x
   *          The x-coordinate of the image.
   * @param y
   *          The y-coordinate of the image
   */
  public void renderImage(Graphics2D g, final Image image, double x, double y) {
    renderImage(g, image, new Point2D.Double(x, y));
  }

  /**
   * Renders the specified image at the defined map location.
   * 
   * @param g
   *          The graphics object to render on.
   * @param image
   *          The image to be rendered.
   * @param location
   *          The location of the image.
   */
  public void renderImage(Graphics2D g, final Image image, Point2D location) {
    Point2D viewPortLocation = Game.world().camera().getViewportLocation(location);
    ImageRenderer.render(g, image, viewPortLocation.getX() * Game.world().camera().getRenderScale(), viewPortLocation.getY() * Game.world().camera().getRenderScale());
  }

  /**
   * Renders the specified entities at their current location in the environment.
   * 
   * @param g
   *          The graphics object to render on.
   * @param entities
   *          The entities to be rendered.
   */
  public void renderEntities(final Graphics2D g, final Collection<? extends IEntity> entities) {
    this.renderEntities(g, entities, true);
  }

  /**
   * Renders the specified entities at their current location in the environment.
   * <p>
   * This method sorts the specified entities by their y-coordinate unless the <code>sort</code> parameter is set to false.
   * </p>
   * 
   * @param g
   *          The graphics object to render on.
   * @param entities
   *          The entities to be rendered.
   * @param sort
   *          Defines whether the entities should be sorted by the <code>EntityYComparator</code> to simulate 2.5D graphics.
   * 
   * @see EntityYComparator
   */
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

  /**
   * Renders the specified entity at its current location in the environment.
   * <p>
   * This method uses the <code>IEntityAnimationController</code> to render the appropriate <code>Animation</code>.<br>
   * If the entity implements the <code>IRenderable</code> interface, its render method is being called afterwards.
   * </p>
   * 
   * <p>
   * To listen to events about this process, you can add a <code>EntityRenderListener</code> or <code>EntityRenderedListener</code> to the render
   * engine.
   * </p>
   * 
   * @param g
   *          The graphics object to render on.
   * @param entity
   *          The entity to be rendered.
   * 
   * @see IEntity#animations()
   * @see IAnimationController#getCurrentImage()
   * @see IRenderable#render(Graphics2D)
   * 
   * @see #canRender(IEntity)
   * @see EntityRenderListener#canRender(IEntity)
   * @see EntityRenderedListener#rendered(EntityRenderEvent)
   */
  public void renderEntity(final Graphics2D g, final IEntity entity) {
    if (entity == null) {
      return;
    }

    if (!this.canRender(entity)) {
      return;
    }

    final EntityRenderEvent renderEvent = new EntityRenderEvent(g, entity);

    if (entity instanceof EntityRenderListener) {
      ((EntityRenderListener) entity).rendering(renderEvent);
    }

    for (final EntityRenderListener listener : this.entityRenderListener) {
      listener.rendering(renderEvent);
    }

    final IEntityAnimationController<?> animationController = entity.animations();
    if (animationController != null) {
      final BufferedImage img = animationController.getCurrentImage();
      if (img != null) {
        if (animationController.isAutoScaling()) {
          final double ratioX = entity.getWidth() / img.getWidth();
          final double ratioY = entity.getHeight() / img.getHeight();
          ImageRenderer.renderScaled(g, img, Game.world().camera().getViewportLocation(entity.getLocation()), ratioX, ratioY);
        } else {
          // center the image relative to the entity dimensions -> the pivot point for rendering is the center of the entity
          double deltaX = (entity.getWidth() - img.getWidth()) / 2.0;
          double deltaY = (entity.getHeight() - img.getHeight()) / 2.0;

          final AffineTransform transform = animationController.getAffineTransform();
          if (transform != null) {
            // center the scaled image relative to the desired render location if the transform provides a scaling element
            deltaX += (img.getWidth() - (img.getWidth() * transform.getScaleX())) / 2.0;
            deltaY += (img.getHeight() - (img.getHeight() * transform.getScaleY())) / 2.0;
          }

          Point2D renderLocation = Game.world().camera().getViewportLocation(entity.getX() + deltaX, entity.getY() + deltaY);
          ImageRenderer.renderTransformed(g, img, renderLocation.getX(), renderLocation.getY(), transform);

          if (Game.config().debug().renderBoundingBoxes()) {
            g.setColor(new Color(255, 0, 0, 50));
            ShapeRenderer.renderOutlineTransformed(g, new Rectangle2D.Double(renderLocation.getX(), renderLocation.getY(), img.getWidth(), img.getWidth()), animationController.getAffineTransform(), 0.25f);
          }
        }
      }
    }

    if (entity instanceof IRenderable) {
      ((IRenderable) entity).render(g);
    }

    if (entity instanceof EntityRenderListener) {
      ((EntityRenderListener) entity).rendered(renderEvent);
    }

    for (final EntityRenderListener listener : this.entityRenderListener) {
      listener.rendered(renderEvent);
    }

    for (final EntityRenderedListener listener : this.entityRenderedListener) {
      listener.rendered(renderEvent);
    }
  }

  /**
   * Determines whether the specified entity can be rendered by evaluating the callbacks to all registered <code>EntityRenderListeners</code>.
   *
   * <p>
   * If the <code>RenderType</code> of the specified entity is set to <code>NONE</code> or there are any callbacks that prevent the entity from being
   * rendered, this method will return false.
   * </p>
   * 
   * @param entity
   *          The entity to check whether it can be rendered or not.
   * 
   * @return True if the entity can be rendered; otherwise false.
   * 
   * @see IEntity#getRenderType()
   * @see RenderType#NONE
   * @see EntityRenderListener#canRender(IEntity)
   */
  public boolean canRender(final IEntity entity) {
    if (entity.getRenderType() == RenderType.NONE) {
      return false;
    }

    if (entity instanceof EntityRenderListener && !((EntityRenderListener) entity).canRender(entity)) {
      return false;
    }

    for (final EntityRenderListener listener : this.entityRenderListener) {
      if (!listener.canRender(entity)) {
        return false;
      }
    }

    return true;
  }
}

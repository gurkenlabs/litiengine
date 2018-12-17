package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
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
import de.gurkenlabs.litiengine.environment.tilemap.HexagonalMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.OrthogonalMapRenderer;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.gui.GuiProperties;

public final class RenderEngine {
  public static final float DEFAULT_RENDERSCALE = 3.0f;

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
    this.mapRenderer.put(MapOrientation.HEXAGONAL, new HexagonalMapRenderer());

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
  public void renderText(final Graphics2D g, final String text, final double x, final double y) {
    if (text == null || text.isEmpty()) {
      return;
    }
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    final Point2D viewPortLocation = Game.world().camera().getViewportLocation(x, y);
    g.drawString(text, (float) viewPortLocation.getX() * Game.world().camera().getRenderScale(), (float) viewPortLocation.getY() * Game.world().camera().getRenderScale());
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, GuiProperties.getDefaultAppearance().getTextAntialiasing());
  }

  public void renderText(final Graphics2D g, final String text, final Point2D location) {
    this.renderText(g, text, location.getX(), location.getY());
  }

  public void renderShape(final Graphics2D g, final Shape shape) {
    if (shape == null) {
      return;
    }

    ShapeRenderer.renderTransformed(g, shape, AffineTransform.getTranslateInstance(Game.world().camera().getPixelOffsetX(), Game.world().camera().getPixelOffsetY()));
  }

  public void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, new BasicStroke(1 / Game.world().camera().getRenderScale()));
  }

  public void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }

    ShapeRenderer.renderOutlineTransformed(g, shape, AffineTransform.getTranslateInstance(Game.world().camera().getPixelOffsetX(), Game.world().camera().getPixelOffsetY()), stroke);
  }

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

  public IMapRenderer getMapRenderer(final MapOrientation mapOrientation) {
    if (!this.mapRenderer.containsKey(mapOrientation)) {
      throw new IllegalArgumentException("The map orientation " + mapOrientation + " is not supported!");
    }

    return this.mapRenderer.get(mapOrientation);
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

  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables) {
    renderables.forEach(r -> this.render(g, r));
  }

  public void render(final Graphics2D g, final Collection<? extends IRenderable> renderables, final Shape clip) {
    // set render shape according to the vision
    final Shape oldClip = g.getClip();

    g.setClip(clip);

    renderables.forEach(r -> r.render(g));

    g.setClip(oldClip);
  }

  public void render(final Graphics2D g, final IRenderable renderable) {
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
        ImageRenderer.renderScaled(g, img, Game.world().camera().getViewportLocation(entity.getLocation()), ratioX, ratioY);
      } else {
        float deltaX = (entity.getWidth() - img.getWidth()) / 2.0f;
        float deltaY = (entity.getHeight() - img.getHeight()) / 2.0f;

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

  public void render(final Graphics2D g, final IMap map, final RenderType... renderTypes) {
    if (map == null) {
      return;
    }

    // draw layers
    this.mapRenderer.get(map.getOrientation()).render(g, map, Game.world().camera().getViewport(), renderTypes);
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

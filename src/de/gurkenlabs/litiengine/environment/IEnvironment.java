package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment extends IInitializable, IRenderable {
  /**
   * Adds the specified entity to the environment container. This also loads the
   * entity (register entity and controllers for update) if the environment has
   * already been loaded.
   *
   * @param entity
   *          The entity to add to the environment.
   */
  public void add(IEntity entity);

  public void add(IRenderable renderable, RenderType type);

  public void clear();

  public List<ICombatEntity> findCombatEntities(Shape shape);

  /**
   * Searches for all combat entities whose hitBox intersect the specified
   * shape.
   * 
   * @param shape
   *          The shape to check intersection for.
   * @param condition
   *          An additional condition that allows to specify a condition which
   *          determines if a {@link ICombatEntity} should be considered.
   * @return A list of all combat entities that intersect the specified
   *         {@link Shape}.
   */
  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public List<IEntity> findEntities(Shape shape);

  public IEntity get(final int mapId);

  public IEntity get(final String name);

  public Collection<IEntity> getByTag(final String tag);

  public <T extends IEntity> Collection<T> getByTag(Class<T> clss, final String tag);

  public AmbientLight getAmbientLight();

  public StaticShadowLayer getStaticShadowLayer();

  public MapArea getArea(int mapId);

  public MapArea getArea(String name);

  public Collection<MapArea> getAreas();

  public Collection<Emitter> getEmitters();

  public Emitter getEmitter(int mapId);

  public Emitter getEmitter(String name);

  public Collection<CollisionBox> getCollisionBoxes();

  public CollisionBox getCollisionBox(final int mapId);

  public CollisionBox getCollisionBox(final String name);

  public Collection<StaticShadow> getStaticShadows();

  public StaticShadow getStaticShadow(int mapId);

  public StaticShadow getStaticShadow(String name);

  public Collection<ICombatEntity> getCombatEntities();

  public Collection<Prop> getProps();

  public Prop getProp(final int mapId);

  public Prop getProp(final String name);

  public ICombatEntity getCombatEntity(final int mapId);

  public ICombatEntity getCombatEntity(final String name);

  public Collection<IEntity> getEntities();

  public Collection<IEntity> getEntities(RenderType renderType);

  public <T extends IEntity> Collection<T> getEntitiesByType(Class<T> clss);

  public LightSource getLightSource(int mapId);

  public LightSource getLightSource(final String name);

  public Collection<LightSource> getLightSources();

  /**
   * Gets the next unique local map id. (All local map ids are negative).
   * 
   * @return The next unique local map id.
   */
  public int getLocalMapId();

  public IMap getMap();

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);

  public IMovableEntity getMovableEntity(String name);

  /**
   * Gets the next unique global map id.
   *
   * @return The next unique global map id.
   */
  public int getNextMapId();

  public Spawnpoint getSpawnpoint(int mapId);

  public Spawnpoint getSpawnpoint(String name);

  public Collection<Spawnpoint> getSpawnPoints();

  public List<String> getUsedTags();

  public Trigger getTrigger(int mapId);

  public Trigger getTrigger(String name);

  public Collection<Trigger> getTriggers();

  public boolean isLoaded();

  public void load();

  public void loadFromMap(final int mapId);

  public void onEntityAdded(final Consumer<IEntity> consumer);

  public void onEntityRemoved(final Consumer<IEntity> consumer);

  public void onEntitiesRendered(final Consumer<Graphics2D> consumer);

  public void onInitialized(final Consumer<IEnvironment> consumer);

  public void onLoaded(final Consumer<IEnvironment> consumer);

  public void onMapRendered(final Consumer<Graphics2D> consumer);

  public void onOverlayRendered(final Consumer<Graphics2D> consumer);

  public void reloadFromMap(final int mapId);

  public void remove(final IEntity entity);

  public <T extends IEntity> void remove(final Collection<T> entities);

  public void remove(final int mapId);

  public void removeRenderable(IRenderable renderable);

  public void unload();
}

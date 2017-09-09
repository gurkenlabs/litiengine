package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;

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
   */
  public void add(IEntity entity);

  public void add(IRenderable renderable, RenderType type);

  public void clear();

  public List<ICombatEntity> findCombatEntities(Shape shape);

  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public List<IEntity> findEntities(Shape shape);

  public IEntity get(final int mapId);

  public IEntity get(final String name);

  public List<IEntity> getByTag(final String tag);

  public AmbientLight getAmbientLight();

  public MapArea getArea(int mapId);

  public MapArea getArea(String name);

  public List<MapArea> getAreas();

  public Collection<Collider> getColliders();

  public Collection<StaticShadow> getStaticShadows();

  public Collection<ICombatEntity> getCombatEntities();

  public ICombatEntity getCombatEntity(final int mapId);

  public Collection<IEntity> getEntities();

  public Collection<IEntity> getEntities(RenderType renderType);

  public <T extends IEntity> Collection<T> getEntitiesByType(Class<T> clss);

  public LightSource getLightSource(int mapId);

  public Collection<LightSource> getLightSources();

  /**
   * Gets the next unique local map id. (All local map ids are negative).
   */
  public int getLocalMapId();

  /**
   * Gets the map.
   *
   * @return the map
   */
  public IMap getMap();

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);

  /**
   * Gets the next unique global map id.
   *
   * @return
   */
  public int getNextMapId();

  public Spawnpoint getSpawnpoint(int mapId);

  public Spawnpoint getSpawnpoint(String name);

  public List<Spawnpoint> getSpawnPoints();

  public Trigger getTrigger(int mapId);

  public Trigger getTrigger(String name);

  public Collection<Trigger> getTriggers();

  public Collection<Trigger> getTriggers(String name);

  public void load();

  public void loadFromMap(final int mapId);

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

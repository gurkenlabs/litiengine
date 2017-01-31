/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
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
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapLocation;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.IMapObject;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment extends IInitializable, IRenderable {
  public void clear();

  /**
   * Adds the specified entity to the environment container. This also loads the
   * entity (register entity and controllers for update) if the environment has
   * already been loaded.
   * 
   * @param entity
   */
  public void add(IEntity entity);

  public void add(IRenderable renderable, RenderType type);

  public void reloadFromMap(final int mapId);

  public void loadFromMap(final int mapId);

  public List<ICombatEntity> findCombatEntities(Shape shape);

  public List<IEntity> findEntities(Shape shape);

  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public Collection<ICombatEntity> getCombatEntities();

  public Collection<IMovableEntity> getMovableEntities();

  public Collection<IEntity> getEntities();

  public Collection<IEntity> getEntities(RenderType renderType);

  public Collection<Collider> getColliders();

  public ICombatEntity getCombatEntity(final int mapId);

  public IMovableEntity getMovableEntity(final int mapId);

  public IEntity get(final int mapId);

  public IEntity get(final String name);

  public Collection<LightSource> getLightSources();

  public Collection<Trigger> getTriggers();

  public Collection<Trigger> getTriggers(String name);

  public Trigger getTrigger(int mapId);

  public Trigger getTrigger(String name);

  public LightSource getLightSource(int mapId);

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

  /**
   * Gets the next unique global map id.
   *
   * @return
   */
  public int getNextMapId();

  public List<MapLocation> getSpawnPoints();

  public MapLocation getSpawnpoint(String name);

  public MapLocation getSpawnpoint(int mapId);

  public List<MapArea> getAreas();

  public MapArea getArea(String name);

  public MapArea getArea(int mapId);

  public AmbientLight getAmbientLight();

  public void onEntitiesRendered(final Consumer<Graphics2D> consumer);

  public void onMapRendered(final Consumer<Graphics2D> consumer);

  public void onOverlayRendered(final Consumer<Graphics2D> consumer);

  public void onInitialized(final Consumer<IEnvironment> consumer);

  public void remove(final int mapId);

  public void remove(final IEntity entity);

  public void removeRenderable(IRenderable renderable);

  public void load();

  public void unload();
}

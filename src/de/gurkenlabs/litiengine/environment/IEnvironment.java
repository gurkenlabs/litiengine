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
  
  public void add(IEntity entity);

  public void add(IRenderable renderable, RenderType type);
  
  public Collection<ICombatEntity> getCombatEntities();

  public Collection<IMovableEntity> getMovableEntities();

  public Collection<IEntity> getEntities();
  
  public Collection<IEntity> getEntities(RenderType renderType);
  
  public List<ICombatEntity> findCombatEntities(Shape shape);

  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public Collection<Collider> getColliders();

  public ICombatEntity getCombatEntity(final int mapId);

  public IMovableEntity getMovableEntity(final int mapId);
  
  public IEntity get(final int mapId);

  public Collection<LightSource> getLightSources();
  
  public Collection<Trigger> getTriggers();

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
  public int getMapId();

  public List<MapLocation> getSpawnPoints();

  public WeatherType getWeather();

  public AmbientLight getAmbientLight();

  public void onEntitiesRendered(final Consumer<Graphics2D> consumer);

  public void onMapRendered(final Consumer<Graphics2D> consumer);

  public void onOverlayRendered(final Consumer<Graphics2D> consumer);

  public void remove(final int mapId);
  
  public void remove(final IEntity entity);

  public void removeRenderable(IRenderable renderable);

  public void setWeather(WeatherType weather);
  
  public void addCollisionBox(IMapObject mapObject);

  public void addDecorMob(final IMapObject mapObject);

  public void addEmitter(final IMapObject mapObject);

  public void addLightSource(IMapObject mapObject);

  public void addMapObject(final IMapObject mapObject);

  public void addMob(final IMapObject mapObject);

  public void addProp(final IMapObject mapObject);

  public void addSpawnpoint(final IMapObject mapObject);
}

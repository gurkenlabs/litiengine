/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapObject;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment extends IInitializable, IRenderable {

  public void add(final int mapId, final IMovableCombatEntity entity);

  public void add(IRenderable renderable, RenderType type);

  public void addAmbientLight();

  public void addCollisionBox(IMapObject mapObject);

  public void addCombatEntity(final int mapId, final ICombatEntity entity);

  public void addDecorMob(final IMapObject mapObject);

  public void addEmitter(final IMapObject mapObject);

  public void addLightSource(IMapObject mapObject);

  public void addMapObject(final IMapObject mapObject);

  public void addMob(final IMapObject mapObject);

  public void addMovableEntity(final int mapId, final IMovableEntity entity);

  public void addProp(IMapObject mapObject);

  public void addSpawnpoint(IMapObject mapObject);

  public void clear();

  public List<ICombatEntity> findCombatEntities(Shape shape);

  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public List<IMapObject> getCollisionBoxes();

  public Collection<ICombatEntity> getCombatEntities();

  public ICombatEntity getCombatEntity(final int mapId);

  public List<Emitter> getEmitters();

  public List<Emitter> getGroundEmitters();

  public List<IRenderable> getGroundRenderable();

  public List<LightSource> getLightSources();

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

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);

  public List<Emitter> getOverlayEmitters();

  public List<IRenderable> getOverlayRenderable();

  public List<Prop> getProps();

  public List<MapLocation> getSpawnPoints();

  public WeatherType getWeather();

  public void onEntitiesRendered(final Consumer<Graphics2D> consumer);

  public void onMapRendered(final Consumer<Graphics2D> consumer);

  public void onOverlayRendered(final Consumer<Graphics2D> consumer);

  public void remove(final int mapId);

  public void remove(IRenderable renderable);

  public void setWeather(WeatherType weather);

  AmbientLight getAmbientLight();

}

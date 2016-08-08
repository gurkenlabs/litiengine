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
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapObject;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment extends IInitializable, IRenderable {

  public void addCombatEntity(final int mapId, final ICombatEntity entity);

  public void addMovableEntity(final int mapId, final IMovableEntity entity);

  public void remove(final int mapId);

  public void add(final int mapId, final IMovableCombatEntity entity);

  public Collection<ICombatEntity> getCombatEntities();

  public List<LightSource> getLightSources();

  public ICombatEntity getCombatEntity(final int mapId);

  public List<ICombatEntity> findCombatEntities(Shape shape);

  public List<ICombatEntity> findCombatEntities(Shape shape, Predicate<ICombatEntity> condition);

  public List<Emitter> getEmitters();

  public List<Emitter> getGroundEmitters();

  public List<Emitter> getOverlayEmitters();

  /**
   * Gets the map.
   *
   * @return the map
   */
  public IMap getMap();

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);

  /**
   * Gets the next unique local map id. (All local map ids are negative).
   */
  public int getLocalMapId();

  /**
   * Gets the next unique global map id.
   *
   * @return
   */
  public int getMapId();

  public List<Prop> getProps();

  public void addProp(IMapObject mapObject);

  public void addEffect(final IMapObject mapObject);

  public void addMob(final IMapObject mapObject);

  public void addDecorMob(final IMapObject mapObject);

  public WeatherType getWeather();

  public void setWeather(WeatherType weather);

  public void onMapRendered(final Consumer<Graphics2D> consumer);

  public void onEntitiesRendered(final Consumer<Graphics2D> consumer);

  public void onOverlayRendered(final Consumer<Graphics2D> consumer);

  public void clear();

  public List<IMapObject> getShadowBoxes();

  public List<IMapObject> getCollisionBoxes();

  public void addCollisionBoxes(IMapObject mapObject);

  public void addShadowBoxes(IMapObject mapObject);

  public void addAmbientLight();

}

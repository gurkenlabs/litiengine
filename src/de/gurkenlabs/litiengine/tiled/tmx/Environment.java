/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapLoader;
import de.gurkenlabs.tiled.tmx.IMapObject;
import de.gurkenlabs.tiled.tmx.IMapObjectLayer;
import de.gurkenlabs.tiled.tmx.TmxMapLoader;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {

  /** The map. */
  private final IMap map;

  private final Map<Integer, ICombatEntity> combatEntities;

  private final Map<Integer, IMovableEntity> movableEntities;

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    final IMapLoader tmxLoader = new TmxMapLoader();
    this.map = tmxLoader.LoadMap(mapPath);

    this.combatEntities = new ConcurrentHashMap<>();
    this.movableEntities = new ConcurrentHashMap<>();
  }

  @Override
  public void init() {
    this.loadMapObjects();
  }

  @Override
  public void add(final int mapId, final ICombatEntity entity) {
    this.combatEntities.put(mapId, entity);
  }

  @Override
  public void add(final int mapId, final IMovableEntity entity) {
    this.movableEntities.put(mapId, entity);
  }

  @Override
  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  @Override
  public ICombatEntity getCombatEntity(final int mapId) {
    if (this.combatEntities.containsKey(mapId)) {
      return this.combatEntities.get(mapId);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.map.IMapContainer#getMap()
   */
  @Override
  public IMap getMap() {
    return this.map;
  }

  @Override
  public Collection<IMovableEntity> getMovableEntities() {
    return this.movableEntities.values();
  }

  @Override
  public IMovableEntity getMovableEntity(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      return this.movableEntities.get(mapId);
    }

    return null;
  }

  protected void addMapObject(final IMapObject mapObject) {

  }

  private void loadMapObjects() {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject.getType() == null || mapObject.getType().isEmpty()) {
          continue;
        }

        this.addMapObject(mapObject);
      }
    }
  }
}
/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapLoader;
import de.gurkenlabs.tiled.tmx.TmxMapLoader;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {

  /** The map. */
  private final IMap map;

  private final Map<Integer, ICombatEntity> combatEntities;

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
  }

  @Override
  public void add(final int mapId, final ICombatEntity entity) {
    this.getCombatEntityMap().put(mapId, entity);
  }

  @Override
  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  @Override
  public ICombatEntity getEntity(final int mapId) {
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

  private Map<Integer, ICombatEntity> getCombatEntityMap() {
    return this.combatEntities;
  }
}
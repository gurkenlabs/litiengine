/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

  /** The mobs. */
  private final CopyOnWriteArrayList<ICombatEntity> combatEntities;

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    final IMapLoader tmxLoader = new TmxMapLoader();
    this.map = tmxLoader.LoadMap(mapPath);

    this.combatEntities = new CopyOnWriteArrayList<>();
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
  public List<ICombatEntity> getCombatEntities() {
    return this.combatEntities;
  }
}
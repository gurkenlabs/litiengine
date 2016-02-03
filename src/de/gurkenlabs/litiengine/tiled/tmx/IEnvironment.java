/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.List;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.tiled.tmx.IMap;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment {

  /**
   * Gets the map.
   *
   * @return the map
   */
  public IMap getMap();

  public List<ICombatEntity> getCombatEntities();
}

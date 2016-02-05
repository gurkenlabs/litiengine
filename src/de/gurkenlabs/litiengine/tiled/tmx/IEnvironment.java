/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.tiled.tmx.IMap;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment {

  public void add(final int mapId, final ICombatEntity entity);

  public Collection<ICombatEntity> getCombatEntities();

  public ICombatEntity getCombatEntity(final int mapId);

  
  public void add(final int mapId, final IMovableEntity entity);

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);

  /**
   * Gets the map.
   *
   * @return the map
   */
  public IMap getMap();
}

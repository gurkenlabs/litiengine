/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.Shape;
import java.util.Collection;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.tiled.tmx.IMap;

/**
 * The Interface IMapContainer.
 */
public interface IEnvironment extends IInitializable {

  public void addCombatEntity(final int mapId, final ICombatEntity entity);

  public void addMovableEntity(final int mapId, final IMovableEntity entity);
  
  public void remove(final int mapId);

  public void add(final int mapId, final IMovableCombatEntity entity);

  public Collection<ICombatEntity> getCombatEntities();

  public ICombatEntity getCombatEntity(final int mapId);

  public Collection<ICombatEntity> findCombatEntities(Shape shape);

  /**
   * Gets the map.
   *
   * @return the map
   */
  public IMap getMap();

  public Collection<IMovableEntity> getMovableEntities();

  public IMovableEntity getMovableEntity(final int mapId);
  
  public int getLocalMapId();
}

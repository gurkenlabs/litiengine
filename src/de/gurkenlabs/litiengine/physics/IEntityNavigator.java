/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.pathfinding.IPathFinder;

/**
 * The Interface IEntityNavigator.
 */
public interface IEntityNavigator extends IUpdateable {
  public IMovableEntity getEntity();

  public Path getPath();

  public IPathFinder getPathFinder();

  public void navigate(Point2D target);

  public void stop();

  public void rotateTowards(Point2D target);

  public boolean isNavigating();

  public void cancelNavigation(Predicate<IMovableEntity> predicate);
}

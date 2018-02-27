package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.pathfinding.IPathFinder;

/**
 * The Interface IEntityNavigator.
 */
public interface IEntityNavigator extends IUpdateable {
  public void cancelNavigation(Predicate<IMobileEntity> predicate);

  public IMobileEntity getEntity();

  public Path getPath();

  public IPathFinder getPathFinder();

  public boolean isNavigating();

  public void navigate(Path2D path);

  public void navigate(Point2D target);

  public void rotateTowards(Point2D target);

  public void stop();
}

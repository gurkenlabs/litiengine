package de.gurkenlabs.litiengine.pathfinding;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * The Interface IEntityNavigator.
 */
public interface IEntityNavigator extends IUpdateable, IRenderable {
  public void addNavigationListener(NavigationListener listener);

  public void removeNavigationListener(NavigationListener listener);

  public void cancelNavigation(Predicate<IMobileEntity> predicate);

  public IMobileEntity getEntity();

  public Path getPath();

  public IPathFinder getPathFinder();

  public boolean isNavigating();

  public boolean navigate(Path2D path);

  public boolean navigate(Point2D target);

  public void rotateTowards(Point2D target);

  public void stop();
}

package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;

public interface IMovementController<T extends IMobileEntity> extends IUpdateable, IEntityController<T> {
  public void apply(Force force);

  public List<Force> getActiceForces();

  public void onMovementCheck(Predicate<T> predicate);

  public void onMoved(Consumer<Point2D> cons);
}

package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntityEmitter {

  public IEntity getEntity();

  public Point2D getLocation();

  public void setLocation(final Point2D location);
}

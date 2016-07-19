package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

public abstract class Prop extends CollisionEntity {

  public Prop(final Point2D location) {
    super();
    this.setLocation(location);
    this.setAnimationController(new PropAnimationController(this));
  }
}

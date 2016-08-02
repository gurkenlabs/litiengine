package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.graphics.animation.DecorMobAnimationController;


@CombatAttributesInfo(velocityFactor = 0.1f)
public class DecorMob extends MovableEntity {
  private String mobType;

  public DecorMob(final Point2D location, String mobType) {
    super();
    this.mobType = mobType;
    this.setLocation(location);
    this.setAnimationController(new DecorMobAnimationController(this));
  }

  public String getMobType() {
    return this.mobType;
  }
}

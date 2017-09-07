/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

/**
 * The Class Destructable.
 */
public class Prop extends CombatEntity {
  private Material material;
  private String spritePath;
  private boolean isObstacle;

  /**
   * Instantiates a new destructible.
   */
  public Prop(final Point2D location, final String spritesheetName, final Material mat) {
    super();
    this.spritePath = spritesheetName;
    this.material = mat;
    this.setLocation(location);
    Game.getEntityControllerManager().addController(this, new PropAnimationController(this));
  }

  public Material getMaterial() {
    return this.material;
  }

  public String getSpritePath() {
    return this.spritePath;
  }

  /**
   * Gets the state.
   *
   * @return the state
   */
  public PropState getState() {
    if (this.getAttributes().getHealth().getCurrentValue() <= 0) {
      return PropState.DESTROYED;
    } else if (this.getAttributes().getHealth().getCurrentValue() <= this.getAttributes().getHealth().getMaxValue() * 0.5) {
      return PropState.DAMAGED;
    } else {
      return PropState.INTACT;
    }
  }

  public void setMaterial(final Material material) {
    this.material = material;
  }

  public void setSpritePath(final String spritePath) {
    this.spritePath = spritePath;
  }

  public boolean isObstacle() {
    return this.isObstacle;
  }

  public void setObstacle(boolean isObstacle) {
    this.isObstacle = isObstacle;
  }
}

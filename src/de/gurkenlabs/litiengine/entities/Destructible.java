/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.graphics.animation.DestructibleAnimationController;

// TODO: Auto-generated Javadoc
/**
 * The Class Destructable.
 */
public abstract class Destructible extends CombatEntity {
  private Material material;

  /**
   * Instantiates a new destructible.
   */
  protected Destructible(Point2D location) {
    super();
    this.setLocation(location);
    this.setAnimationController(new DestructibleAnimationController(this));
  }

  public Material getMaterial() {
    return this.material;
  }

  /**
   * Gets the state.
   *
   * @return the state
   */
  public DestructibleState getState() {
    if (this.getAttributes().getHealth().getCurrentValue() <= 0) {
      return DestructibleState.Destroyed;
    } else if (this.getAttributes().getHealth().getCurrentValue() <= this.getAttributes().getHealth().getMaxValue() * 0.5) {
      return DestructibleState.Damaged;
    } else {
      return DestructibleState.Intact;
    }
  }

  protected void setMaterial(Material material) {
    this.material = material;
  }
}

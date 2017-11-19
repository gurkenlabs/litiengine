package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

/**
 * The Class Destructable.
 */
public class Prop extends CombatEntity {
  private Material material;
  private String spritesheetName;
  private boolean isObstacle;
  private boolean addShadow;

  /**
   * Instantiates a new destructible.
   */
  public Prop(final Point2D location, final String spritesheetName, final Material mat) {
    super();
    this.spritesheetName = spritesheetName;
    this.material = mat;
    this.setLocation(location);
    this.updateAnimationController();
  }

  public Material getMaterial() {
    return this.material;
  }

  public String getSpritesheetName() {
    return this.spritesheetName;
  }

  public void updateAnimationController() {
    PropAnimationController controller = new PropAnimationController(this);
    Game.getEntityControllerManager().addController(this, controller);
    if (Game.getEnvironment() != null && Game.getEnvironment().isLoaded()) {
      Game.getLoop().attach(controller);
    }
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

  public void setSpritesheetName(final String spriteName) {
    this.spritesheetName = spriteName;
    this.updateAnimationController();
  }

  public boolean isObstacle() {
    return this.isObstacle;
  }

  public void setObstacle(boolean isObstacle) {
    this.isObstacle = isObstacle;
  }

  public boolean isAddShadow() {
    return this.addShadow;
  }

  public void setAddShadow(boolean addShadow) {
    this.addShadow = addShadow;
  }
}

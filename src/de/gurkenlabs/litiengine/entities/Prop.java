package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

@AnimationInfo(spritePrefix = "prop-")
public class Prop extends CombatEntity {
  private Material material;
  private String spritesheetName;
  private boolean isObstacle;
  private boolean addShadow;
  private boolean flipHorizontally;
  private boolean flipVertically;
  private Rotation rotation;

  public Prop(final String spritesheetName) {
    this(0, 0, spritesheetName);
  }

  public Prop(double x, double y, final String spritesheetName) {
    this(x, y, spritesheetName, Material.UNDEFINED);
  }

  public Prop(double x, double y, final String spritesheetName, final Material mat) {
    this(new Point2D.Double(x, y), spritesheetName, mat);
  }

  public Prop(final Point2D location, final String spritesheetName) {
    this(location, spritesheetName, Material.UNDEFINED);
  }

  public Prop(final Point2D location, final String spritesheetName, final Material mat) {
    super();
    this.rotation = Rotation.NONE;
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

  /**
   * Gets the state.
   *
   * @return the state
   */
  public PropState getState() {
    if (!this.isIndestructible() && this.getAttributes().getHealth().getCurrentValue() <= 0) {
      return PropState.DESTROYED;
    } else if (!this.isIndestructible() && this.getAttributes().getHealth().getCurrentValue() <= this.getAttributes().getHealth().getMaxValue() * 0.5) {
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

  @Override
  public boolean isDead() {
    if(this.isIndestructible()) {
      return false;
    }
    return this.getAttributes().getHealth().getCurrentValue() <= 0;
  }

  public Rotation getSpriteRotation() {
    return rotation;
  }

  public void setSpriteRotation(Rotation spriteRotation) {
    this.rotation = spriteRotation;
  }

  public boolean flipHorizontally() {
    return flipHorizontally;
  }

  public void setFlipHorizontally(boolean flipHorizontally) {
    this.flipHorizontally = flipHorizontally;
  }

  public boolean flipVertically() {
    return flipVertically;
  }

  public void setFlipVertically(boolean flipVertically) {
    this.flipVertically = flipVertically;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.getName() != null && !this.getName().isEmpty()) {
      sb.append(this.getName());
    } else {
      sb.append(Prop.class.getSimpleName());
    }
    sb.append(" (");
    sb.append(this.getSpritesheetName());

    sb.append(") #");
    sb.append(this.getMapId());
    return sb.toString();
  }

  private void updateAnimationController() {
    PropAnimationController controller = new PropAnimationController(this);
    Game.getEntityControllerManager().addController(this, controller);
    if (Game.getEnvironment() != null && Game.getEnvironment().isLoaded()) {
      Game.getLoop().attach(controller);
    }
  }
}

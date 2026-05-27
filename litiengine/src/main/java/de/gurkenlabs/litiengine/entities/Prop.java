package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import java.awt.geom.Point2D;

/**
 * A static/destructible scenery entity rendered from a sprite. Props extend {@link CombatEntity} so they can take damage and be destroyed, and
 * support optional shadow rendering, sprite flipping/rotation and quality-based scaling.
 */
@AnimationInfo(spritePrefix = PropAnimationController.PROP_IDENTIFIER)
@TmxType(MapObjectType.PROP)
public class Prop extends CombatEntity {

  @TmxProperty(name = MapObjectProperty.PROP_MATERIAL)
  private Material material;

  @TmxProperty(name = MapObjectProperty.PROP_ADDSHADOW)
  private boolean addShadow;

  @TmxProperty(name = MapObjectProperty.PROP_FLIPHORIZONTALLY)
  private boolean flipHorizontally;

  @TmxProperty(name = MapObjectProperty.PROP_FLIPVERTICALLY)
  private boolean flipVertically;

  @TmxProperty(name = MapObjectProperty.SCALE_SPRITE)
  private boolean scaling;

  @TmxProperty(name = MapObjectProperty.PROP_ROTATION)
  private Rotation rotation;

  @TmxProperty(name = MapObjectProperty.SPRITESHEETNAME)
  private String spritesheetName;

  /**
   * Instantiates a new {@code Prop} entity.
   *
   * @param spritesheetName
   *          The spritesheet name of this prop.
   */
  public Prop(final String spritesheetName) {
    this(0, 0, spritesheetName);
  }

  /**
   * Instantiates a new {@code Prop} entity.
   *
   * @param x
   *          The x-coordinate of this prop.
   * @param y
   *          The y-coordinate of this prop.
   * @param spritesheetName
   *          The spritesheet name of this prop.
   */
  public Prop(double x, double y, final String spritesheetName) {
    this(x, y, spritesheetName, Material.UNDEFINED);
  }

  /**
   * Instantiates a new {@code Prop} entity.
   *
   * @param x
   *          The x-coordinate of this prop.
   * @param y
   *          The y-coordinate of this prop.
   * @param spritesheetName
   *          The spritesheet name of this prop.
   * @param material
   *          The material of this prop.
   */
  public Prop(double x, double y, final String spritesheetName, final Material material) {
    this(new Point2D.Double(x, y), spritesheetName, material);
  }

  /**
   * Instantiates a new {@code Prop} entity.
   *
   * @param location
   *          The location of this prop.
   * @param spritesheetName
   *          The spritesheet name of this prop.
   */
  public Prop(final Point2D location, final String spritesheetName) {
    this(location, spritesheetName, Material.UNDEFINED);
  }

  /**
   * Instantiates a new {@code Prop} entity.
   *
   * @param location
   *          The location of this prop.
   * @param spritesheetName
   *          The spritesheet name of this prop.
   * @param material
   *          The material of this prop.
   */
  public Prop(final Point2D location, final String spritesheetName, final Material material) {
    super();
    this.rotation = Rotation.NONE;
    this.spritesheetName = spritesheetName;
    this.material = material;
    this.setLocation(location);
    this.updateAnimationController();
  }

  /**
   * Gets the material of this prop.
   *
   * @return the material
   */
  public Material getMaterial() {
    return this.material;
  }

  /**
   * Gets the name of the spritesheet used to render this prop.
   *
   * @return the spritesheet name
   */
  public String getSpritesheetName() {
    return this.spritesheetName;
  }

  /**
   * Gets the state.
   *
   * @return the state
   */
  public PropState getState() {
    if (!this.isIndestructible() && this.getHitPoints().getModifiedValue() <= 0) {
      return PropState.DESTROYED;
    } else if (!this.isIndestructible()
      && this.getHitPoints().getModifiedValue() <= this.getHitPoints().getModifiedMax() * 0.5) {
      return PropState.DAMAGED;
    } else {
      return PropState.INTACT;
    }
  }

  /**
   * Returns whether this prop should cast a shadow.
   *
   * @return {@code true} if a shadow is rendered
   */
  public boolean isAddShadow() {
    return this.addShadow;
  }

  /**
   * Returns whether this prop's sprite is scaled to its bounding box.
   *
   * @return {@code true} if scaling is enabled
   */
  public boolean isScaling() {
    return this.scaling;
  }

  /**
   * Returns whether this prop's sprite is flipped horizontally.
   *
   * @return {@code true} if flipped horizontally
   */
  public boolean flipHorizontally() {
    return flipHorizontally;
  }

  /**
   * Returns whether this prop's sprite is flipped vertically.
   *
   * @return {@code true} if flipped vertically
   */
  public boolean flipVertically() {
    return flipVertically;
  }

  /**
   * Gets the sprite rotation applied to this prop.
   *
   * @return the sprite rotation
   */
  public Rotation getSpriteRotation() {
    return rotation;
  }

  /**
   * Sets the material of this prop.
   *
   * @param material the material to set
   */
  public void setMaterial(final Material material) {
    this.material = material;
  }

  /**
   * Sets the name of the spritesheet used to render this prop and updates the animation controller.
   *
   * @param spriteName the new spritesheet name
   */
  public void setSpritesheetName(final String spriteName) {
    this.spritesheetName = spriteName;
    this.updateAnimationController();
  }

  /**
   * Sets whether this prop should cast a shadow.
   *
   * @param addShadow {@code true} to render a shadow
   */
  public void setAddShadow(boolean addShadow) {
    this.addShadow = addShadow;
  }

  /**
   * Sets whether this prop's sprite should be scaled to its bounding box.
   *
   * @param scaling {@code true} to enable scaling
   */
  public void setScaling(boolean scaling) {
    this.scaling = scaling;
  }

  @Override
  public boolean isDead() {
    if (this.isIndestructible()) {
      return false;
    }
    return this.getHitPoints().getModifiedValue() <= 0;
  }

  /**
   * Sets the sprite rotation applied to this prop.
   *
   * @param spriteRotation the rotation to set
   */
  public void setSpriteRotation(Rotation spriteRotation) {
    this.rotation = spriteRotation;
  }

  /**
   * Sets whether this prop's sprite is flipped horizontally.
   *
   * @param flipHorizontally {@code true} to flip horizontally
   */
  public void setFlipHorizontally(boolean flipHorizontally) {
    this.flipHorizontally = flipHorizontally;
  }

  /**
   * Sets whether this prop's sprite is flipped vertically.
   *
   * @param flipVertically {@code true} to flip vertically
   */
  public void setFlipVertically(boolean flipVertically) {
    this.flipVertically = flipVertically;
  }

  @Override
  public String toString() {
    // don't use a StringBuilder due to bad runtime performance
    String str = "#" + this.getMapId() + ": ";
    if (this.getName() != null && !this.getName().isEmpty()) {
      str += this.getName();
    } else {
      str += Prop.class.getSimpleName();
    }

    str += " (" + this.getSpritesheetName() + ")";

    return str;
  }

  /**
   * Creates the animation controller used to render this prop. Subclasses may override this method to provide a custom controller implementation.
   *
   * @return the new animation controller
   */
  protected IEntityAnimationController<?> createAnimationController() {
    return new PropAnimationController<>(this);
  }

  private void updateAnimationController() {
    IEntityAnimationController<?> controller = this.createAnimationController();
    this.getControllers().addController(controller);
    if (Game.world().environment() != null && Game.world().environment().isLoaded()) {
      Game.loop().attach(controller);
    }
  }
}

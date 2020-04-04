package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

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
   * Instantiates a new <code>Prop</code> entity.
   *
   * @param spritesheetName
   *          The spritesheet name of this prop.
   */
  public Prop(final String spritesheetName) {
    this(0, 0, spritesheetName);
  }

  /**
   * Instantiates a new <code>Prop</code> entity.
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
   * Instantiates a new <code>Prop</code> entity.
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
   * Instantiates a new <code>Prop</code> entity.
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
   * Instantiates a new <code>Prop</code> entity.
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
    if (!this.isIndestructible() && this.getHitPoints().get() <= 0) {
      return PropState.DESTROYED;
    } else if (!this.isIndestructible() && this.getHitPoints().get() <= this.getHitPoints().getMax() * 0.5) {
      return PropState.DAMAGED;
    } else {
      return PropState.INTACT;
    }
  }

  public boolean isAddShadow() {
    return this.addShadow;
  }

  public boolean isScaling() {
    return this.scaling;
  }

  public boolean flipHorizontally() {
    return flipHorizontally;
  }

  public boolean flipVertically() {
    return flipVertically;
  }

  public Rotation getSpriteRotation() {
    return rotation;
  }

  public void setMaterial(final Material material) {
    this.material = material;
  }

  public void setSpritesheetName(final String spriteName) {
    this.spritesheetName = spriteName;
    this.updateAnimationController();
  }

  public void setAddShadow(boolean addShadow) {
    this.addShadow = addShadow;
  }

  public void setScaling(boolean scaling) {
    this.scaling = scaling;
  }

  @Override
  public boolean isDead() {
    if (this.isIndestructible()) {
      return false;
    }
    return this.getHitPoints().get() <= 0;
  }

  public void setSpriteRotation(Rotation spriteRotation) {
    this.rotation = spriteRotation;
  }

  public void setFlipHorizontally(boolean flipHorizontally) {
    this.flipHorizontally = flipHorizontally;
  }

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

  private void updateAnimationController() {
    PropAnimationController<Prop> controller = new PropAnimationController<>(this);
    this.getControllers().addController(controller);
    if (Game.world().environment() != null && Game.world().environment().isLoaded()) {
      Game.loop().attach(controller);
    }
  }
}

package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.AbilityBuilder;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.abilities.DynamicAbility;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.tweening.TweenType;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/// The `Creature` class extends the `CombatEntity` class and implements the `IMobileEntity` interface. It represents a creature
/// entity in the game with movement and combat capabilities.
@MovementInfo
@TmxType(MapObjectType.CREATURE)
public class Creature extends CombatEntity implements IMobileEntity {
  private static final int IDLE_DELAY = 100;
  private final Collection<EntityMovedListener> movedListeners = ConcurrentHashMap.newKeySet();
  private final Map<String, Ability> abilities = new ConcurrentHashMap<>();

  @TmxProperty(name = MapObjectProperty.MOVEMENT_ACCELERATION)
  private int acceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_DECELERATION)
  private int deceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_TURNONMOVE)
  private boolean turnOnMove;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_VELOCITY)
  private Attribute<Float> velocity;

  @TmxProperty(name = MapObjectProperty.SPRITESHEETNAME)
  private String spritesheetName;

  @TmxProperty(name = MapObjectProperty.SCALE_SPRITE)
  private boolean scaling;

  private long lastMoved;

  /// Instantiates a new `Creature` entity with default settings.
  public Creature() {
    this(null);
  }

  /// Instantiates a new `Creature` entity.
  ///
  /// @param spritesheetName The spritesheet name that identifies the sprites bound to this instance.
  /// @see CreatureAnimationController#getSpriteName(Creature, de.gurkenlabs.litiengine.graphics.CreatureAnimationState)
  public Creature(String spritesheetName) {
    super();
    final MovementInfo movementInfo = getClass().getAnnotation(MovementInfo.class);
    if (movementInfo != null) {
      this.velocity = new Attribute<>(movementInfo.velocity());
      this.acceleration = movementInfo.acceleration();
      this.deceleration = movementInfo.deceleration();
      this.setTurnOnMove(movementInfo.turnOnMove());
      this.addController(this.createMovementController());
    }

    if (spritesheetName != null) {
      this.setSpritesheetName(spritesheetName);
    } else {
      this.setSpritesheetName(Game.random().choose(EntityAnimationController.getDefaultSpritePrefixes(getClass())));
    }
  }

  @Override
  public void onMoved(EntityMovedListener listener) {
    this.movedListeners.add(listener);
  }

  @Override
  public void removeMovedListener(EntityMovedListener listener) {
    this.movedListeners.remove(listener);
  }

  @Override
  public float[] getTweenValues(TweenType tweenType) {
    if (tweenType == TweenType.VELOCITY) {
      return new float[] {getVelocity().getModifiedValue()};
    }
    return super.getTweenValues(tweenType);
  }

  @Override
  public void setTweenValues(TweenType tweenType, float[] newValues) {
    if (tweenType == TweenType.VELOCITY) {
      getVelocity().setValue(newValues[0]);
    } else {
      super.setTweenValues(tweenType, newValues);
    }
  }

  @Override
  public int getAcceleration() {
    return this.acceleration;
  }

  @Override
  public int getDeceleration() {
    return this.deceleration;
  }

  /// Gets the facing direction of the creature based on its current angle.
  ///
  /// @return The direction the creature is facing.
  public Direction getFacingDirection() {
    return Direction.fromAngle(getAngle());
  }

  @Override
  public IMovementController movement() {
    return getController(IMovementController.class);
  }

  /// Gets the current spritesheet name of this instance. Overwriting this allows for a more sophisticated logic that determines the sprite to be used;
  /// e.g. This method could append certain properties of the creature (state, weapon, ...) to the default string.
  ///
  /// The value of this method will be used e.g. by the [CreatureAnimationController] to determine the animation that it should play.
  ///
  /// @return The current spritesheet name of this instance.
  public String getSpritesheetName() {
    return this.spritesheetName;
  }

  @Override
  public float getTickVelocity() {
    // pixels per ms multiplied by the passed ms
    // ensure that entities don't travel too far in case of lag
    return Math.min(Game.loop().getDeltaTime(), GameLoop.TICK_DELTATIME_LAG)
      * 0.001F
      * getVelocity().getModifiedValue()
      * Game.loop().getTimeScale();
  }

  @Override
  public Attribute<Float> getVelocity() {
    return this.velocity;
  }

  /// Checks if the creature's sprite is being scaled with the entity dimensions.
  ///
  /// @return true if the sprite is being scaled, false otherwise.
  public boolean isScaling() {
    return this.scaling;
  }

  /// Checks if the creature is idle.
  ///
  /// @return true if the creature has not moved for a duration longer than the idle delay, false otherwise.
  public boolean isIdle() {
    return Game.time().since(this.lastMoved) > IDLE_DELAY;
  }

  @Override
  public void setAcceleration(final int acceleration) {
    this.acceleration = acceleration;
  }

  @Override
  public void setDeceleration(final int deceleration) {
    this.deceleration = deceleration;
  }

  /// Sets the facing direction of the creature.
  ///
  /// @param facingDirection The direction to set the creature's facing angle to.
  public void setFacingDirection(final Direction facingDirection) {
    this.setAngle(facingDirection.toAngle());
  }

  @Override
  public void setLocation(final Point2D location) {
    if (isDead() || location == null) {
      return;
    }

    final Point2D oldLocation = getLocation();
    super.setLocation(location);

    if (Game.hasStarted() && this.isLoaded()) {
      this.lastMoved = Game.time().now();
      this.fireMovedEvent(
        new EntityMovedEvent(
          this, getX() - oldLocation.getX(), getY() - oldLocation.getY()));
    }
  }

  @Override
  public void setTurnOnMove(final boolean turn) {
    this.turnOnMove = turn;
  }

  /// Sets the spritesheet name for this creature.
  ///
  /// @param spritesheetName The name of the spritesheet to set.
  public void setSpritesheetName(String spritesheetName) {
    if (this.spritesheetName != null && this.spritesheetName.equals(spritesheetName)) {
      return;
    }

    this.spritesheetName = spritesheetName;
    this.updateAnimationController();
  }

  /// Sets whether the creature's sprite should be scaled with the entity dimensions.
  ///
  /// @param scaling true to scale the sprite, false otherwise.
  public void setScaling(boolean scaling) {
    this.scaling = scaling;
  }

  @Override
  public void setVelocity(float velocity) {
    getVelocity().setValue(velocity);
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("#").append(getMapId()).append(": ");
    if (getName() != null && !getName().isEmpty()) {
      sb.append(getName());
    } else {
      sb.append(Creature.class.getSimpleName());
    }
    sb.append(" (").append(getSpritesheetName()).append(")");

    return sb.toString();
  }

  /// Updates the animation controller for the creature. This method creates a new animation controller and adds it to the creature's controllers. If
  /// the game world environment is loaded, the new controller is attached to the game loop.
  protected void updateAnimationController() {
    IEntityAnimationController<? extends Creature> controller = this.createAnimationController();
    getControllers().addController(controller);
    if (Game.world().environment() != null && Game.world().environment().isLoaded()) {
      Game.loop().attach(controller);
    }
  }

  /// Creates a new animation controller for the creature.
  ///
  /// @return A new instance of [IEntityAnimationController] for the creature.
  protected IEntityAnimationController<? extends Creature> createAnimationController() {
    return new CreatureAnimationController<>(this, true);
  }

  /// Creates a new movement controller for the creature.
  ///
  /// @return A new instance of [IMovementController] for the creature.
  protected IMovementController createMovementController() {
    return new MovementController<>(this);
  }

  /// Gets all abilities currently registered on this creature.
  ///
  /// @return A collection of registered abilities.
  public Collection<Ability> getAbilities() {
    return List.copyOf(this.abilities.values());
  }

  /// Gets an ability registered on this creature by its name.
  ///
  /// @param name The name of the ability to retrieve.
  /// @return An [Optional] containing the ability if found, or empty otherwise.
  public Optional<Ability> getAbility(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.abilities.get(name));
  }

  /// Gets the first ability of the specified class registered on this creature.
  ///
  /// @param <T> The ability type.
  /// @param abilityClass The class of the ability.
  /// @return An [Optional] containing the ability if found, or empty otherwise.
  @SuppressWarnings("unchecked")
  public <T extends Ability> Optional<T> getAbility(Class<T> abilityClass) {
    if (abilityClass == null) {
      return Optional.empty();
    }
    for (Ability ability : this.abilities.values()) {
      if (abilityClass.isInstance(ability)) {
        return Optional.of((T) ability);
      }
    }
    return Optional.empty();
  }

  /// Checks whether this creature has an ability with the specified name.
  ///
  /// @param name The ability name.
  /// @return true if the ability exists, false otherwise.
  public boolean hasAbility(String name) {
    return name != null && this.abilities.containsKey(name);
  }

  /// Checks whether this creature has an ability of the specified class.
  ///
  /// @param abilityClass The class of the ability.
  /// @return true if an ability of the class is registered, false otherwise.
  public boolean hasAbility(Class<? extends Ability> abilityClass) {
    return this.getAbility(abilityClass).isPresent();
  }

  /// Adds an ability to this creature.
  ///
  /// @param ability The ability to register.
  public void addAbility(Ability ability) {
    if (ability == null) {
      return;
    }
    String key = ability.getName() != null && !ability.getName().isBlank()
        ? ability.getName()
        : ability.getClass().getSimpleName();
    this.abilities.put(key, ability);
  }

  /// Removes an ability from this creature.
  ///
  /// @param ability The ability to remove.
  public void removeAbility(Ability ability) {
    if (ability == null) {
      return;
    }
    this.abilities.values().remove(ability);
  }

  /// Removes an ability with the specified name from this creature.
  ///
  /// @param name The name of the ability to remove.
  public void removeAbility(String name) {
    if (name == null) {
      return;
    }
    this.abilities.remove(name);
  }

  /// Casts a registered ability by name.
  ///
  /// @param name The name of the ability to cast.
  /// @return The [AbilityExecution] if cast successfully, or null otherwise.
  public AbilityExecution cast(String name) {
    return this.getAbility(name).map(Ability::cast).orElse(null);
  }

  /// Casts a registered ability of the specified class.
  ///
  /// @param <T> The ability type.
  /// @param abilityClass The class of the ability to cast.
  /// @return The [AbilityExecution] if cast successfully, or null otherwise.
  public <T extends Ability> AbilityExecution cast(Class<T> abilityClass) {
    return this.getAbility(abilityClass).map(Ability::cast).orElse(null);
  }

  /// Checks whether a registered ability with the specified name can currently be cast.
  ///
  /// @param name The name of the ability.
  /// @return true if the ability exists and can be cast, false otherwise.
  public boolean canCast(String name) {
    return this.getAbility(name).map(Ability::canCast).orElse(false);
  }

  /// Checks whether a registered ability of the specified class can currently be cast.
  ///
  /// @param abilityClass The class of the ability.
  /// @return true if the ability exists and can be cast, false otherwise.
  public boolean canCast(Class<? extends Ability> abilityClass) {
    return this.getAbility(abilityClass).map(Ability::canCast).orElse(false);
  }

  /// Checks whether a registered ability with the specified name is currently on cooldown.
  ///
  /// @param name The name of the ability.
  /// @return true if the ability exists and is on cooldown, false otherwise.
  public boolean isOnCooldown(String name) {
    return this.getAbility(name).map(Ability::isOnCooldown).orElse(false);
  }

  /// Checks whether a registered ability of the specified class is currently on cooldown.
  ///
  /// @param abilityClass The class of the ability.
  /// @return true if the ability exists and is on cooldown, false otherwise.
  public boolean isOnCooldown(Class<? extends Ability> abilityClass) {
    return this.getAbility(abilityClass).map(Ability::isOnCooldown).orElse(false);
  }

  /// Begins fluently building a new [de.gurkenlabs.litiengine.abilities.DynamicAbility] for this creature.
  ///
  /// @param name The name of the ability.
  /// @return An [AbilityBuilder] configured for this creature.
  public AbilityBuilder createAbility(String name) {
    return new AbilityBuilder(this, name);
  }

  private void fireMovedEvent(EntityMovedEvent event) {
    for (EntityMovedListener listener : this.movedListeners) {
      listener.moved(event);
    }
  }
}

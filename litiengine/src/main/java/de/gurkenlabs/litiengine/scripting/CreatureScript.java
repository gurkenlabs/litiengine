package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedAbilityBuilder;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;

/// Convenient base class for creature behavior scripts with movement and combat helpers.
public abstract class CreatureScript extends EntityScript<Creature> {
  /// Moves the creature towards a target point in the game world.
  /// @param target The target in map coordinates; `null` has no effect.
  public void moveTowards(Point2D target) {
    if (target == null || this.host() == null) return;
    double angle = GeometricUtilities.calcRotationAngleInDegrees(this.host().getCenter(), target);
    Game.physics().move(this.host(), angle, this.host().getTickVelocity());
  }

  /// Moves the creature towards a target entity.
  /// @param target The target entity; `null` has no effect.
  public void moveTowards(IEntity target) {
    if (target != null) {
      this.moveTowards(target.getCenter());
    }
  }

  /// Moves the creature in a specific compass direction.
  /// @param direction The direction; `null` has no effect.
  public void moveInDirection(Direction direction) {
    if (direction != null && this.host() != null) {
      Game.physics().move(this.host(), direction.toAngle(), this.host().getTickVelocity());
    }
  }

  /// Moves the creature at a specific angle in degrees (0 = North, 90 = East, 180 = South, 270 = West).
  /// @param angleDegrees The movement angle in degrees.
  public void moveInAngle(double angleDegrees) {
    if (this.host() != null) {
      Game.physics().move(this.host(), angleDegrees, this.host().getTickVelocity());
    }
  }

  /// Checks if the creature is currently dead.
  /// @return `true` if the attached creature is dead.
  @Override
  public boolean isDead() {
    return this.host() != null && this.host().isDead();
  }

  /// Returns current health / hitpoints.
  /// @return The current hit points, or zero when detached.
  @Override
  public int getHealth() {
    return this.host() != null ? this.host().getHitPoints().getModifiedValue() : 0;
  }

  /// Returns maximum health / hitpoints.
  /// @return The maximum hit points, or zero when detached.
  @Override
  public int getMaxHealth() {
    return this.host() != null ? this.host().getHitPoints().getMax() : 0;
  }

  /// Begins building a scripted ability executed by this creature.
  /// @param name The ability name.
  /// @return A builder for the new ability.
  public ScriptedAbilityBuilder createAbility(String name) {
    if (this.host() == null) {
      throw new IllegalStateException("Creature host is not attached.");
    }
    return new ScriptedAbilityBuilder(this.host(), name);
  }

  /// Casts an ability registered on this creature by its name.
  /// @param name The ability name.
  /// @return The execution, or `null` when detached or not castable.
  public de.gurkenlabs.litiengine.abilities.AbilityExecution cast(String name) {
    return this.host() != null ? this.host().cast(name) : null;
  }

  /// Gets an ability registered on this creature by its name.
  /// @param name The ability name.
  /// @return The ability, or an empty optional.
  public java.util.Optional<de.gurkenlabs.litiengine.abilities.Ability> getAbility(String name) {
    return this.host() != null ? this.host().getAbility(name) : java.util.Optional.empty();
  }

  /// Checks if this creature has an ability registered with the specified name.
  /// @param name The ability name.
  /// @return `true` if it is registered.
  public boolean hasAbility(String name) {
    return this.host() != null && this.host().hasAbility(name);
  }

  /// Checks if an ability registered with the specified name can currently be cast.
  /// @param name The ability name.
  /// @return `true` if it can currently be cast.
  public boolean canCast(String name) {
    return this.host() != null && this.host().canCast(name);
  }

  /// Checks if an ability registered with the specified name is currently on cooldown.
  /// @param name The ability name.
  /// @return `true` if it is cooling down.
  public boolean isOnCooldown(String name) {
    return this.host() != null && this.host().isOnCooldown(name);
  }

  /// Configures top-down WASD keyboard movement for this creature and binds its lifecycle to this script.
  /// @return The installed movement controller.
  public de.gurkenlabs.litiengine.input.KeyboardEntityController<Creature> enableTopDownMovement() {
    return this.enableTopDownMovement(java.awt.event.KeyEvent.VK_W, java.awt.event.KeyEvent.VK_S, java.awt.event.KeyEvent.VK_A, java.awt.event.KeyEvent.VK_D);
  }

  /// Configures top-down keyboard movement with custom keys for this creature and binds its lifecycle to this script.
  /// @param up The key code for upward movement.
  /// @param down The key code for downward movement.
  /// @param left The key code for left movement.
  /// @param right The key code for right movement.
  /// @return The installed movement controller.
  public de.gurkenlabs.litiengine.input.KeyboardEntityController<Creature> enableTopDownMovement(int up, int down, int left, int right) {
    if (this.host() == null) {
      throw new IllegalStateException("Creature host is not attached.");
    }
    var controller = new de.gurkenlabs.litiengine.input.KeyboardEntityController<>(this.host(), up, down, left, right);
    this.host().setController(de.gurkenlabs.litiengine.physics.IMovementController.class, controller);
    this.context().manage(controller::detach);
    return controller;
  }

  /// Configures platforming movement (A/D/Space) for this creature and binds its lifecycle to this script.
  /// @return The installed movement controller.
  public de.gurkenlabs.litiengine.input.PlatformingMovementController<Creature> enablePlatformingMovement() {
    return this.enablePlatformingMovement(java.awt.event.KeyEvent.VK_A, java.awt.event.KeyEvent.VK_D, java.awt.event.KeyEvent.VK_SPACE);
  }

  /// Configures platforming movement with custom keys for this creature and binds its lifecycle to this script.
  /// @param left The key code for left movement.
  /// @param right The key code for right movement.
  /// @param jump The key code for jumping.
  /// @return The installed movement controller.
  public de.gurkenlabs.litiengine.input.PlatformingMovementController<Creature> enablePlatformingMovement(int left, int right, int jump) {
    if (this.host() == null) {
      throw new IllegalStateException("Creature host is not attached.");
    }
    var controller = new de.gurkenlabs.litiengine.input.PlatformingMovementController<>(this.host(), jump);
    controller.setLeftKeys(left);
    controller.setRightKeys(right);
    this.host().setController(de.gurkenlabs.litiengine.physics.IMovementController.class, controller);
    this.context().manage(controller::detach);
    return controller;
  }

  /// Disables and removes active movement controllers on this creature.
  public void disableMovementController() {
    if (this.host() != null) {
      var current = this.host().getController(de.gurkenlabs.litiengine.physics.IMovementController.class);
      if (current != null) {
        current.detach();
      }
    }
  }


  /// Called when the platforming movement controller executes a jump action.
  /// @throws Exception if handling fails.
  protected void onJump() throws Exception {}

  @Override
  final void dispatchAction(String action) throws Exception {
    if ("jump".equalsIgnoreCase(action)) {
      this.onJump();
    }
    super.dispatchAction(action);
  }
}



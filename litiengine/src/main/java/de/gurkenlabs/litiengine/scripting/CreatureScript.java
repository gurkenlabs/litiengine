package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedAbilityBuilder;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;

/** Convenient base class for creature behavior scripts with movement and combat helpers. */
public abstract class CreatureScript extends EntityScript<Creature> {
  /** Moves the creature towards a target point in the game world. */
  public void moveTowards(Point2D target) {
    if (target == null || this.host() == null) return;
    double angle = GeometricUtilities.calcRotationAngleInDegrees(this.host().getCenter(), target);
    Game.physics().move(this.host(), angle, this.host().getTickVelocity());
  }

  /** Moves the creature towards a target entity. */
  public void moveTowards(IEntity target) {
    if (target != null) {
      this.moveTowards(target.getCenter());
    }
  }

  /** Moves the creature in a specific compass direction. */
  public void moveInDirection(Direction direction) {
    if (direction != null && this.host() != null) {
      Game.physics().move(this.host(), direction.toAngle(), this.host().getTickVelocity());
    }
  }

  /** Moves the creature at a specific angle in degrees (0 = North, 90 = East, 180 = South, 270 = West). */
  public void moveInAngle(double angleDegrees) {
    if (this.host() != null) {
      Game.physics().move(this.host(), angleDegrees, this.host().getTickVelocity());
    }
  }

  /** Checks if the creature is currently dead. */
  @Override
  public boolean isDead() {
    return this.host() != null && this.host().isDead();
  }

  /** Returns current health / hitpoints. */
  @Override
  public int getHealth() {
    return this.host() != null ? this.host().getHitPoints().getModifiedValue() : 0;
  }

  /** Returns maximum health / hitpoints. */
  @Override
  public int getMaxHealth() {
    return this.host() != null ? this.host().getHitPoints().getMax() : 0;
  }

  /** Begins building a scripted ability executed by this creature. */
  public ScriptedAbilityBuilder createAbility(String name) {
    if (this.host() == null) {
      throw new IllegalStateException("Creature host is not attached.");
    }
    return new ScriptedAbilityBuilder(this.host(), name);
  }

  /** Casts an ability registered on this creature by its name. */
  public de.gurkenlabs.litiengine.abilities.AbilityExecution cast(String name) {
    return this.host() != null ? this.host().cast(name) : null;
  }

  /** Gets an ability registered on this creature by its name. */
  public java.util.Optional<de.gurkenlabs.litiengine.abilities.Ability> getAbility(String name) {
    return this.host() != null ? this.host().getAbility(name) : java.util.Optional.empty();
  }

  /** Checks if this creature has an ability registered with the specified name. */
  public boolean hasAbility(String name) {
    return this.host() != null && this.host().hasAbility(name);
  }

  /** Checks if an ability registered with the specified name can currently be cast. */
  public boolean canCast(String name) {
    return this.host() != null && this.host().canCast(name);
  }

  /** Checks if an ability registered with the specified name is currently on cooldown. */
  public boolean isOnCooldown(String name) {
    return this.host() != null && this.host().isOnCooldown(name);
  }
}

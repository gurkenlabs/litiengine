package de.gurkenlabs.litiengine.entities;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.CombatInfo;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;

/**
 * The Class AttackableEntity.
 */
@CombatInfo
@CollisionInfo(collision = true)
public class CombatEntity extends CollisionEntity implements ICombatEntity {
  public static final int DEFAULT_HITPOINTS = 100;

  private final List<Effect> appliedEffects;
  private final List<CombatEntityListener> listeners;
  private final List<CombatEntityDeathListener> deathListeners;
  private final List<CombatEntityHitListener> hitListeners;
  private final RangeAttribute<Integer> hitPoints;

  @TmxProperty(name = MapObjectProperty.COMBAT_INDESTRUCTIBLE)
  private boolean isIndestructible;

  @TmxProperty(name = MapObjectProperty.COMBAT_TEAM)
  private int team;
  
  @TmxProperty(name = MapObjectProperty.COMBAT_HITPOINTS)
  private int initialHitpoints;

  private ICombatEntity target;
  private long lastHit;

  public CombatEntity() {
    super();
    this.listeners = new CopyOnWriteArrayList<>();
    this.deathListeners = new CopyOnWriteArrayList<>();
    this.hitListeners = new CopyOnWriteArrayList<>();
    this.appliedEffects = new CopyOnWriteArrayList<>();

    final CombatInfo info = this.getClass().getAnnotation(CombatInfo.class);
    this.initialHitpoints = info.hitpoints();
    this.setTeam(info.team());
    this.setIndestructible(info.isIndestructible());

    this.hitPoints = new RangeAttribute<>(this.initialHitpoints, 0, this.initialHitpoints);
  }

  @Override
  public void addCombatEntityListener(CombatEntityListener listener) {
    this.listeners.add(listener);
    this.hitListeners.add(listener);
    this.deathListeners.add(listener);
  }

  @Override
  public void removeCombatEntityListener(CombatEntityListener listener) {
    this.listeners.remove(listener);
    this.hitListeners.remove(listener);
    this.deathListeners.remove(listener);
  }

  @Override
  public void addHitListener(CombatEntityHitListener listener) {
    this.hitListeners.add(listener);
  }

  @Override
  public void removeHitListener(CombatEntityHitListener listener) {
    this.hitListeners.remove(listener);
  }

  @Override
  public void addDeathListener(CombatEntityDeathListener listener) {
    this.deathListeners.add(listener);
  }

  @Override
  public void removeDeathListener(CombatEntityDeathListener listener) {
    this.deathListeners.remove(listener);
  }

  @Override
  public void die() {
    if (this.isDead()) {
      return;
    }

    this.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SET, 0));
    this.fireDeathEvent();

    this.setCollision(false);
  }

  @Override
  public List<Effect> getAppliedEffects() {
    return this.appliedEffects;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  @Override
  public RangeAttribute<Integer> getHitPoints() {
    return this.hitPoints;
  }

  /**
   * Gets the hit box.
   *
   * @return the hit box
   */
  @Override
  public Shape getHitBox() {
    return new Ellipse2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
  }

  @Override
  public ICombatEntity getTarget() {
    return this.target;
  }

  @Override
  public int getTeam() {
    return this.team;
  }

  @Override
  public boolean hit(int damage) {
    return this.hit(damage, null);
  }

  @Override
  public boolean hit(final int damage, final Ability ability) {
    if (this.isDead()) {
      return false;
    }

    if (!this.isIndestructible()) {
      this.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBSTRACT, damage));
    }

    if (this.isDead()) {
      this.fireDeathEvent();
      this.setCollision(false);
    }

    final CombatEntityHitEvent event = new CombatEntityHitEvent(this, damage, ability);
    for (final CombatEntityHitListener listener : this.hitListeners) {
      listener.onHit(event);
    }

    this.lastHit = Game.time().now();

    return this.isDead();
  }

  private void fireDeathEvent() {
    for (final CombatEntityDeathListener listener : this.deathListeners) {
      listener.onDeath(this);
    }
  }

  /**
   * Checks if is dead.
   *
   * @return true, if is dead
   */
  @Override
  public boolean isDead() {
    return !this.isIndestructible() && this.getHitPoints().getCurrentValue() <= 0;
  }

  /**
   * Checks if is friendly.
   *
   * @param entity
   *          the entity
   * @return true, if is friendly
   */
  @Override
  public boolean isFriendly(final ICombatEntity entity) {
    return this.getTeam() == entity.getTeam();
  }

  /**
   * Checks if is indestructible.
   *
   * @return true, if is indestructible
   */
  @Override
  public boolean isIndestructible() {
    return this.isIndestructible;
  }

  @Override
  public boolean isNeutral() {
    return this.getTeam() == 0;
  }

  /**
   * Resurrect.
   */
  @Override
  public void resurrect() {
    if (!this.isDead()) {
      return;
    }

    this.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SET, this.getHitPoints().getMaxValue()));

    for (final CombatEntityListener listener : this.listeners) {
      listener.onResurrection(this);
    }

    this.setCollision(true);
  }

  @Override
  public void setIndestructible(final boolean indestructible) {
    this.isIndestructible = indestructible;
  }

  @Override
  public void setTarget(final ICombatEntity target) {
    this.target = target;

  }

  /**
   * Sets the team.
   *
   * @param team
   *          the new team
   */
  @Override
  public void setTeam(final int team) {
    this.team = team;
  }

  @Override
  public boolean wasHit(int timeSpan) {
    return Game.time().since(this.lastHit) < timeSpan;
  }
}

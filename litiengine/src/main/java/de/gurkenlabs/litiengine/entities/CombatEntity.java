package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.tweening.TweenType;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@CombatInfo
@CollisionInfo(collision = true)
public class CombatEntity extends CollisionEntity implements ICombatEntity {
  public static final int DEFAULT_HITPOINTS = 100;

  private final Collection<CombatEntityListener> listeners;
  private final Collection<CombatEntityDeathListener> deathListeners;
  private final Collection<CombatEntityResurrectListener> resurrectListeners;
  private final Collection<CombatEntityHitListener> hitListeners;

  private final List<Effect> appliedEffects;
  private final RangeAttribute<Integer> hitPoints;

  @TmxProperty(name = MapObjectProperty.COMBAT_INDESTRUCTIBLE)
  private boolean isIndestructible;

  @TmxProperty(name = MapObjectProperty.COMBAT_TEAM)
  private int team;

  @TmxProperty(name = MapObjectProperty.COMBAT_HITPOINTS)
  private int initialHitpoints;

  private ICombatEntity target;
  private long lastHit;

  /** Instantiates a new {@code CombatEntity}. */
  public CombatEntity() {
    super();
    this.listeners = ConcurrentHashMap.newKeySet();
    this.deathListeners = ConcurrentHashMap.newKeySet();
    this.resurrectListeners = ConcurrentHashMap.newKeySet();
    this.hitListeners = ConcurrentHashMap.newKeySet();
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
  public void onHit(CombatEntityHitListener listener) {
    this.hitListeners.add(listener);
  }

  @Override
  public void onDeath(CombatEntityDeathListener listener) {
    this.deathListeners.add(listener);
  }

  @Override
  public void onResurrect(CombatEntityResurrectListener listener) {
    this.resurrectListeners.add(listener);
  }

  @Override
  public void removeListener(CombatEntityHitListener listener) {
    this.hitListeners.remove(listener);
  }

  @Override
  public void removeListener(CombatEntityDeathListener listener) {
    this.deathListeners.remove(listener);
  }

  @Override
  public void removeListener(CombatEntityResurrectListener listener) {
    this.resurrectListeners.remove(listener);
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
  public float[] getTweenValues(TweenType tweenType) {
    if (tweenType == TweenType.HITPOINTS) {
      return new float[] {(float) this.getHitPoints().get()};
    }
    return super.getTweenValues(tweenType);
  }

  @Override
  public void setTweenValues(TweenType tweenType, float[] newValues) {
    if (tweenType == TweenType.HITPOINTS) {
      this.getHitPoints().setBaseValue(Math.round(newValues[0]));
    } else {
      super.setTweenValues(tweenType, newValues);
    }
  }

  @Override
  public void hit(int damage) {
    this.hit(damage, null);
  }

  @Override
  public void hit(final int damage, final Ability ability) {
    if (this.isDead()) {
      return;
    }

    if (!this.isIndestructible()) {
      this.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, damage));
    }

    final EntityHitEvent event = new EntityHitEvent(this, ability, damage, this.isDead());

    for (final CombatEntityListener listener : this.listeners) {
      listener.hit(event);
    }

    for (final CombatEntityHitListener listener : this.hitListeners) {
      listener.hit(event);
    }

    if (this.isDead()) {
      this.fireDeathEvent();
      this.setCollision(false);
    }

    this.lastHit = Game.time().now();
  }

  private void fireDeathEvent() {
    for (final CombatEntityListener listener : this.listeners) {
      listener.death(this);
    }

    for (final CombatEntityDeathListener listener : this.deathListeners) {
      listener.death(this);
    }
  }

  /**
   * Checks if is dead.
   *
   * @return true, if is dead
   */
  @Override
  public boolean isDead() {
    return !this.isIndestructible() && this.getHitPoints().get() <= 0;
  }

  /**
   * Checks if is friendly.
   *
   * @param entity the entity
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

  /** Resurrect. */
  @Override
  public void resurrect() {
    if (!this.isDead()) {
      return;
    }

    this.getHitPoints()
        .modifyBaseValue(new AttributeModifier<>(Modification.SET, this.getHitPoints().getMax()));

    for (final CombatEntityListener listener : this.listeners) {
      listener.resurrect(this);
    }

    for (final CombatEntityResurrectListener listener : this.resurrectListeners) {
      listener.resurrect(this);
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
   * @param team the new team
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

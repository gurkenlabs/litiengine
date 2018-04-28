package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;

/**
 * The Class AttackableEntity.
 */
@CombatAttributesInfo
@CollisionInfo(collision = true)
public class CombatEntity extends CollisionEntity implements ICombatEntity {
  private final List<IEffect> appliedEffects;
  private final List<CombatEntityListener> listeners;
  private final List<CombatEntityDeathListener> deathListeners;
  private final List<CombatEntityHitListener> hitListeners;
  private final CombatAttributes attributes;

  private boolean isIndestructible;
  private ICombatEntity target;
  private int team;
  private long lastHit;

  public CombatEntity() {
    super();
    this.listeners = new CopyOnWriteArrayList<>();
    this.deathListeners = new CopyOnWriteArrayList<>();
    this.hitListeners = new CopyOnWriteArrayList<>();
    this.appliedEffects = new CopyOnWriteArrayList<>();

    final CombatAttributesInfo info = this.getClass().getAnnotation(CombatAttributesInfo.class);

    this.attributes = new CombatAttributes(info);
    this.setIndestructible(false);
    this.setupAttributes(this.getAttributes());
  }

  @Override
  public void addCombatEntityListener(CombatEntityListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeCombatEntityListener(CombatEntityListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addHitListener(CombatEntityHitListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeHitListener(CombatEntityHitListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addDeathListener(CombatEntityDeathListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeDeathListener(CombatEntityDeathListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void die() {
    if (this.isDead()) {
      return;
    }

    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<>(Modification.SET, 0));
    for (final CombatEntityDeathListener listener : this.deathListeners) {
      listener.onDeath(this);
    }

    this.setCollision(false);
  }

  @Override
  public List<IEffect> getAppliedEffects() {
    return this.appliedEffects;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  @Override
  public CombatAttributes getAttributes() {
    return this.attributes;
  }

  /**
   * Gets the hit box.
   *
   * @return the hit box
   */
  @Override
  public Ellipse2D getHitBox() {
    return new Ellipse2D.Double(this.getLocation().getX(), this.getLocation().getY(), this.getWidth(), this.getHeight());
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

    int actualDamage = damage;
    if (this.getAttributes().getShield().getCurrentValue() > 0) {
      int shieldDmg = damage;
      if (shieldDmg > this.getAttributes().getShield().getCurrentValue()) {
        shieldDmg = this.getAttributes().getShield().getCurrentValue();
      }

      this.getAttributes().getShield().modifyBaseValue(new AttributeModifier<Short>(Modification.SUBSTRACT, shieldDmg));
      actualDamage = damage - shieldDmg;
    }

    if (!this.isIndestructible()) {
      this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<>(Modification.SUBSTRACT, actualDamage));
    }

    if (this.isDead()) {
      this.die();
    }

    final CombatEntityHitEvent event = new CombatEntityHitEvent(this, actualDamage, ability);
    for (final CombatEntityHitListener listener : this.hitListeners) {
      listener.onHit(event);
    }

    this.lastHit = Game.getLoop().getTicks();

    return this.isDead();
  }

  /**
   * Checks if is dead.
   *
   * @return true, if is dead
   */
  @Override
  public boolean isDead() {
    return this.getAttributes().getHealth().getCurrentValue() <= 0;
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

    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<>(Modification.SET, this.getAttributes().getHealth().getMaxValue()));

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

  /**
   * Sets the up attributes.
   *
   * @param attributes
   *          the new up attributes
   */
  protected void setupAttributes(final CombatAttributes attributes) {
    // do nothing because this method is designed to provide the child classes
    // the possibility to implement additional functionality upon instantiation
  }

  @Override
  public boolean wasHit(int timeSpan) {
    return Game.getLoop().getDeltaTime(this.lastHit) < timeSpan;
  }
}

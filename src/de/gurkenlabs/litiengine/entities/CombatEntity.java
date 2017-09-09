package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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
  /** The attributes. */
  private final CombatAttributes attributes;
  private final List<Consumer<ICombatEntity>> entityDeathConsumer;

  private final List<Consumer<CombatEntityHitArgument>> entityHitConsumer;

  private final List<Consumer<ICombatEntity>> entityResurrectConsumer;

  /** The is indestructible. */
  private boolean isIndestructible;

  private ICombatEntity target;

  /** The team. */
  private int team;

  /**
   * Instantiates a new attackable entity.
   */
  public CombatEntity() {
    super();
    this.entityDeathConsumer = new CopyOnWriteArrayList<>();
    this.entityResurrectConsumer = new CopyOnWriteArrayList<>();
    this.entityHitConsumer = new CopyOnWriteArrayList<>();
    this.appliedEffects = new CopyOnWriteArrayList<>();

    final CombatAttributesInfo info = this.getClass().getAnnotation(CombatAttributesInfo.class);

    this.attributes = new CombatAttributes(info);
    this.setIndestructible(false);
    this.setupAttributes(this.getAttributes());
  }

  /**
   * Die.
   */
  @Override
  public void die() {
    if (this.isDead()) {
      return;
    }

    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.SET, 0));
    for (final Consumer<ICombatEntity> consumer : this.entityDeathConsumer) {
      consumer.accept(this);
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.entities.Entity#hasCollision()
   */
  @Override
  public boolean hasCollision() {
    // if the entity is dead, ignore collision
    return !this.isDead() && super.hasCollision();
  }

  @Override
  public boolean hit(int damage, final Ability ability) {
    if (this.isDead()) {
      return false;
    }

    if (this.getAttributes().getShield().getCurrentValue() > 0) {
      int shieldDmg = damage;
      if (shieldDmg > this.getAttributes().getShield().getCurrentValue()) {
        shieldDmg = this.getAttributes().getShield().getCurrentValue();
      }

      this.getAttributes().getShield().modifyBaseValue(new AttributeModifier<Short>(Modification.SUBSTRACT, shieldDmg));
      damage = damage - shieldDmg;
    }

    if (!this.isIndestructible()) {
      this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.SUBSTRACT, damage));
    }

    if (this.isDead()) {
      this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.SET, 0));
      for (final Consumer<ICombatEntity> consumer : this.entityDeathConsumer) {
        consumer.accept(this);
      }

      this.setCollision(false);
    }

    final CombatEntityHitArgument arg = new CombatEntityHitArgument(this, damage, ability);
    for (final Consumer<CombatEntityHitArgument> consumer : this.entityHitConsumer) {
      consumer.accept(arg);
    }

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

  @Override
  public void onDeath(final Consumer<ICombatEntity> consumer) {
    if (this.entityDeathConsumer.contains(consumer)) {
      return;
    }

    this.entityDeathConsumer.add(consumer);
  }

  @Override
  public void onHit(final Consumer<CombatEntityHitArgument> consumer) {
    if (this.entityHitConsumer.contains(consumer)) {
      return;
    }

    this.entityHitConsumer.add(consumer);
  }

  @Override
  public void onResurrect(final Consumer<ICombatEntity> consumer) {
    if (this.entityResurrectConsumer.contains(consumer)) {
      return;
    }

    this.entityResurrectConsumer.add(consumer);
  }

  /**
   * Resurrect.
   */
  @Override
  public void resurrect() {
    if (!this.isDead()) {
      return;
    }

    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.SET, this.getAttributes().getHealth().getMaxValue()));
    for (final Consumer<ICombatEntity> consumer : this.entityResurrectConsumer) {
      consumer.accept(this);
    }

    this.setCollision(true);
  }

  /**
   * Sets the indestructible.
   *
   * @param ind
   *          the new indestructible
   */
  public void setIndestructible(final boolean ind) {
    this.isIndestructible = ind;
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

  }
}

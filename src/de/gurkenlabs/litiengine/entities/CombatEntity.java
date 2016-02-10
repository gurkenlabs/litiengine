/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;

/**
 * The Class AttackableEntity.
 */
@CombatAttributesInfo
@CollisionInfo(collision = true)
public abstract class CombatEntity extends CollisionEntity implements ICombatEntity {
  private final List<Consumer<ICombatEntity>> entityDeathConsumer;
  private final List<Consumer<CombatEntityHitArgument>> entityHitConsumer;

  /** The attributes. */
  private final CombatAttributes attributes;

  /** The team. */
  private int team;

  /** The is indestructible. */
  private boolean isIndestructible;

  /**
   * Instantiates a new attackable entity.
   */
  public CombatEntity() {
    super();
    this.entityDeathConsumer = new CopyOnWriteArrayList<>();
    this.entityHitConsumer = new CopyOnWriteArrayList<>();

    final CombatAttributesInfo info = this.getClass().getAnnotation(CombatAttributesInfo.class);

    this.attributes = new CombatAttributes(info);
    this.setIndestructible(false);
    this.setupAttributes(this.getAttributes());
  }

  /**
   * Adds the xp.
   *
   * @param deltaXP
   *          the delta xp
   */
  public void addXP(final int deltaXP) {
    this.attributes.getExperience().modifyBaseValue(new AttributeModifier<Integer>(Modification.Add, deltaXP));
    if (this.attributes.getExperience().getCurrentValue() >= this.attributes.getExperience().getMaxValue()) {
      this.levelUp();
    }
  }

  /**
   * Die.
   */
  public void die() {
    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.Set, 0));
    for (final Consumer<ICombatEntity> consumer : this.entityDeathConsumer) {
      consumer.accept(this);
    }

    this.setCollision(false);
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
  public boolean hit(final int damage) {
    if (this.isIndestructible() || this.isDead()) {
      return false;
    }
    
    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.Substract, damage));

    if (this.isDead()) {
      this.die();
    }

    final CombatEntityHitArgument arg = new CombatEntityHitArgument(this, damage);
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

  /**
   * Resurrect.
   */
  public void resurrect() {
    this.getAttributes().getHealth().modifyBaseValue(new AttributeModifier<Short>(Modification.Set, this.getAttributes().getHealth().getMaxValue()));
    this.setCollision(true);
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
   * Level up.
   */
  private void levelUp() {
    if (this.attributes.getLevel().getCurrentValue() >= this.attributes.getLevel().getMaxValue()) {
      return;
    }
    this.attributes.getLevel().modifyBaseValue(new AttributeModifier<Byte>(Modification.Add, 1));
    this.updateAttributes();
  }

  /**
   * Sets the indestructible.
   *
   * @param ind
   *          the new indestructible
   */
  protected void setIndestructible(final boolean ind) {
    this.isIndestructible = ind;
  }

  /**
   * Sets the up attributes.
   *
   * @param attributes
   *          the new up attributes
   */
  protected void setupAttributes(final CombatAttributes attributes) {

  }

  /**
   * Update attributes.
   */
  protected void updateAttributes() {

    final float levelMultiplier = (float) (Math.log(Math.pow(this.getAttributes().getLevel().getCurrentValue(), 2)) + Math.sqrt(this.getAttributes().getLevel().getCurrentValue()));

    // float xpMultiplier = (float)
    // (this.getAttributes().getExperience().getInitialMaxValue()
    // + this.getAttributes().getExperience().getMaxValue()
    // + this.getAttributes().getExperience().getMaxValue() * 0.1);

    this.getAttributes().getHealth().modifyMaxBaseValue(new AttributeModifier<Short>(Modification.Multiply, levelMultiplier));

    this.getAttributes().getExperience().modifyMaxBaseValue(new AttributeModifier<Integer>(Modification.Multiply, levelMultiplier / 2 * this.getAttributes().getExperience().getMaxValue()));

    this.getAttributes().getShield().modifyMaxBaseValue(new AttributeModifier<Short>(Modification.Multiply, levelMultiplier));

    this.getAttributes().getHealthRegeneration().modifyBaseValue(new AttributeModifier<Byte>(Modification.Multiply, levelMultiplier));
    this.getAttributes().getDamageMultiplier().modifyBaseValue(new AttributeModifier<Float>(Modification.Multiply, levelMultiplier));
  }
}

/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.abilities.OffensiveAbility;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

/**
 * The Class HitEffect.
 */
public class HitEffect extends Effect {
  private final List<Consumer<EntityHitArgument>> entityHitConsumers;
  /** The offensive ability. */
  private final OffensiveAbility offensiveAbility;

  /**
   * Instantiates a new hurt effect.
   *
   * @param ability
   *          the ability
   */
  public HitEffect(final IEnvironment environment, final OffensiveAbility ability) {
    super(environment, ability, new EffectTarget[] { EffectTarget.ENEMY });
    this.entityHitConsumers = new CopyOnWriteArrayList<>();

    this.offensiveAbility = ability;
  }

  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    int damage = this.offensiveAbility.getAttackDamage();
    boolean killed = affectedEntity.hit(damage);
    EntityHitArgument arg = new EntityHitArgument(this.getAbility().getExecutor(), affectedEntity, damage, killed);
    for (final Consumer<EntityHitArgument> consumer : this.entityHitConsumers) {
      consumer.accept(arg);
    }
  }

  public void onEnemyHit(final Consumer<EntityHitArgument> consumer) {
    if (!this.entityHitConsumers.contains(consumer)) {
      this.entityHitConsumers.add(consumer);
    }
  }
}

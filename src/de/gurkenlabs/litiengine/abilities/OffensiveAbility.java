package de.gurkenlabs.litiengine.abilities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.abilities.effects.EntityHitArgument;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;

/**
 * This ability class provides some additional events and helper methods to
 * manager offensive abilities. It can be used to calculate the current attack
 * damage respecting the attributes of the entity and inform consumers about the
 * hit entity.
 */
public abstract class OffensiveAbility extends Ability {
  private final List<Consumer<EntityHitArgument>> entityHitConsumers;

  /**
   * Instantiates a new offensive ability.
   *
   * @param executingMob
   *          the executing mob
   */
  protected OffensiveAbility(final IMovableCombatEntity executingMob) {
    super(executingMob);
    this.entityHitConsumers = new CopyOnWriteArrayList<>();
  }

  public void entityHit(final EntityHitArgument arg) {
    for (final Consumer<EntityHitArgument> consumer : this.entityHitConsumers) {
      consumer.accept(arg);
    }
  }

  /**
   * Gets the attack damage.
   *
   * @return the attack damage
   */
  public int getAttackDamage() {
    return Math.round(this.getAttributes().getValue().getCurrentValue() * this.getExecutor().getAttributes().getDamageMultiplier().getCurrentValue());
  }

  public void onEntityHit(final Consumer<EntityHitArgument> consumer) {
    if (!this.entityHitConsumers.contains(consumer)) {
      this.entityHitConsumers.add(consumer);
    }
  }
}

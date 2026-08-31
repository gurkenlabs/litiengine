package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.entities.Creature;
import java.util.function.Consumer;

/// An [Ability] implementation configured programmatically with attributes and executed via callbacks.
public class DynamicAbility extends Ability {
  private Consumer<AbilityExecution> castConsumer;

  public DynamicAbility(Creature executor, String name) {
    super(executor);
    this.setName(name);
  }

  public void setCastConsumer(Consumer<AbilityExecution> castConsumer) {
    this.castConsumer = castConsumer;
  }

  public Consumer<AbilityExecution> getCastConsumer() {
    return this.castConsumer;
  }

  @Override
  public AbilityExecution cast() {
    AbilityExecution execution = super.cast();
    if (execution != null && this.castConsumer != null) {
      this.castConsumer.accept(execution);
    }
    return execution;
  }
}

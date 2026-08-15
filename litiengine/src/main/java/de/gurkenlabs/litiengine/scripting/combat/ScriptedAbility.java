package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.function.Consumer;

/** An ability implementation configured fluently and executed via script callbacks. */
public class ScriptedAbility extends Ability {
  private Consumer<AbilityExecution> castConsumer;

  public ScriptedAbility(Creature executor, String name) {
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

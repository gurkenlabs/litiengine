package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.abilities.CastType;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.Objects;
import java.util.function.Consumer;

/** Fluent builder for constructing and registering {@link ScriptedAbility} instances. */
public final class ScriptedAbilityBuilder {
  private final Creature executor;
  private final String name;
  private String description = "";
  private int cooldown = 0;
  private int range = 0;
  private int impact = 0;
  private int value = 0;
  private CastType castType = CastType.INSTANT;
  private boolean multiTarget = false;
  private Consumer<AbilityExecution> onCastConsumer;

  public ScriptedAbilityBuilder(Creature executor, String name) {
    this.executor = Objects.requireNonNull(executor, "Executor must not be null.");
    this.name = Objects.requireNonNull(name, "Ability name must not be null.");
  }

  public ScriptedAbilityBuilder description(String description) {
    this.description = description == null ? "" : description;
    return this;
  }

  public ScriptedAbilityBuilder cooldown(int cooldownMs) {
    this.cooldown = Math.max(0, cooldownMs);
    return this;
  }

  public ScriptedAbilityBuilder range(int range) {
    this.range = Math.max(0, range);
    return this;
  }

  public ScriptedAbilityBuilder impact(int impact) {
    this.impact = Math.max(0, impact);
    return this;
  }

  public ScriptedAbilityBuilder value(int value) {
    this.value = value;
    return this;
  }

  public ScriptedAbilityBuilder castType(CastType castType) {
    this.castType = Objects.requireNonNull(castType);
    return this;
  }

  public ScriptedAbilityBuilder multiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
    return this;
  }

  public ScriptedAbilityBuilder onCast(Consumer<AbilityExecution> onCast) {
    this.onCastConsumer = onCast;
    return this;
  }

  public ScriptedAbilityBuilder onCast(Runnable onCast) {
    if (onCast == null) {
      this.onCastConsumer = null;
    } else {
      this.onCastConsumer = execution -> onCast.run();
    }
    return this;
  }

  public ScriptedAbility build() {
    ScriptedAbility ability = new ScriptedAbility(this.executor, this.name);
    ability.setDescription(this.description);
    ability.setCastType(this.castType);
    ability.setMultiTarget(this.multiTarget);
    ability.getAttributes().cooldown().setValue(this.cooldown);
    ability.getAttributes().range().setValue(this.range);
    ability.getAttributes().impact().setValue(this.impact);
    ability.getAttributes().value().setValue(this.value);
    if (this.onCastConsumer != null) {
      ability.setCastConsumer(this.onCastConsumer);
    }
    return ability;
  }
}

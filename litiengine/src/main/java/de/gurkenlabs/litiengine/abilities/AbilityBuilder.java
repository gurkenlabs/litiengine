package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/// Fluent builder for constructing and registering [DynamicAbility] instances on a creature.
public class AbilityBuilder {
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
  private final List<Effect> effects = new ArrayList<>();

  public AbilityBuilder(Creature executor, String name) {
    this.executor = Objects.requireNonNull(executor, "Executor must not be null.");
    this.name = Objects.requireNonNull(name, "Ability name must not be null.");
  }

  public AbilityBuilder description(String description) {
    this.description = description == null ? "" : description;
    return this;
  }

  public AbilityBuilder cooldown(int cooldownMs) {
    this.cooldown = Math.max(0, cooldownMs);
    return this;
  }

  public AbilityBuilder range(int range) {
    this.range = Math.max(0, range);
    return this;
  }

  public AbilityBuilder impact(int impact) {
    this.impact = Math.max(0, impact);
    return this;
  }

  public AbilityBuilder value(int value) {
    this.value = value;
    return this;
  }

  public AbilityBuilder castType(CastType castType) {
    this.castType = Objects.requireNonNull(castType);
    return this;
  }

  public AbilityBuilder multiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
    return this;
  }

  public AbilityBuilder onCast(Consumer<AbilityExecution> onCast) {
    this.onCastConsumer = onCast;
    return this;
  }

  public AbilityBuilder onCast(Runnable onCast) {
    if (onCast == null) {
      this.onCastConsumer = null;
    } else {
      this.onCastConsumer = execution -> onCast.run();
    }
    return this;
  }

  /// Adds an [Effect] to this ability.
  ///
  /// @param effect The effect to attach.
  /// @return This builder instance for chaining.
  public AbilityBuilder effect(Effect effect) {
    if (effect != null) {
      this.effects.add(effect);
    }
    return this;
  }

  public DynamicAbility build() {
    DynamicAbility ability = new DynamicAbility(this.executor, this.name);
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
    for (Effect effect : this.effects) {
      ability.addEffect(effect);
    }
    return ability;
  }

  /// Constructs the ability and registers it directly on the executing creature.
  ///
  /// @return The registered [DynamicAbility]
  public DynamicAbility register() {
    DynamicAbility ability = this.build();
    if (this.executor != null) {
      this.executor.addAbility(ability);
    }
    return ability;
  }
}

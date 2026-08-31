package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/// Fluent builder for constructing [DynamicAbility] instances for a [Creature].
///
/// Use [#build()] when the caller will manage the ability itself, or [#register()] to also add it
/// to the executor. Numeric values default to zero and the cast type defaults to [CastType#INSTANT].
/// Negative cooldown, range, and impact values are normalized to zero.
///
/// @see Creature#createAbility(String)
/// @see AbilityExecution
/// @see Effect
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

  /// Creates a builder for an ability executed by the specified creature.
  ///
  /// @param executor The creature that will execute the ability.
  /// @param name The name used to identify the ability on the creature.
  /// @throws NullPointerException if `executor` or `name` is `null`.
  public AbilityBuilder(Creature executor, String name) {
    this.executor = Objects.requireNonNull(executor, "Executor must not be null.");
    this.name = Objects.requireNonNull(name, "Ability name must not be null.");
  }

  /// Sets the player-facing description.
  ///
  /// @param description The description, or `null` to use an empty description.
  /// @return This builder.
  public AbilityBuilder description(String description) {
    this.description = description == null ? "" : description;
    return this;
  }

  /// Sets the cooldown after a successful cast.
  ///
  /// @param cooldownMs The cooldown in milliseconds; negative values are treated as zero.
  /// @return This builder.
  public AbilityBuilder cooldown(int cooldownMs) {
    this.cooldown = Math.max(0, cooldownMs);
    return this;
  }

  /// Sets the maximum cast range.
  ///
  /// @param range The range in map units; negative values are treated as zero.
  /// @return This builder.
  public AbilityBuilder range(int range) {
    this.range = Math.max(0, range);
    return this;
  }

  /// Sets the base impact used by the ability's effects.
  ///
  /// @param impact The impact value; negative values are treated as zero.
  /// @return This builder.
  public AbilityBuilder impact(int impact) {
    this.impact = Math.max(0, impact);
    return this;
  }

  /// Sets the general-purpose value exposed through the ability attributes.
  ///
  /// @param value The value to expose; unlike the other numeric attributes this may be negative.
  /// @return This builder.
  public AbilityBuilder value(int value) {
    this.value = value;
    return this;
  }

  /// Sets how the ability is cast.
  ///
  /// @param castType The cast type.
  /// @return This builder.
  /// @throws NullPointerException if `castType` is `null`.
  public AbilityBuilder castType(CastType castType) {
    this.castType = Objects.requireNonNull(castType);
    return this;
  }

  /// Sets whether one execution may affect multiple targets.
  ///
  /// @param multiTarget `true` to permit multiple targets.
  /// @return This builder.
  public AbilityBuilder multiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
    return this;
  }

  /// Sets the callback invoked when the ability is cast.
  ///
  /// @param onCast The callback, or `null` to remove the current callback.
  /// @return This builder.
  public AbilityBuilder onCast(Consumer<AbilityExecution> onCast) {
    this.onCastConsumer = onCast;
    return this;
  }

  /// Sets a callback that does not need the [AbilityExecution] details.
  ///
  /// @param onCast The callback, or `null` to remove the current callback.
  /// @return This builder.
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

  /// Constructs a new ability without adding it to the executor.
  ///
  /// Each invocation returns a new ability configured from the builder's current values.
  ///
  /// @return A new, unregistered ability.
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
  /// @return The newly constructed and registered ability.
  public DynamicAbility register() {
    DynamicAbility ability = this.build();
    if (this.executor != null) {
      this.executor.addAbility(ability);
    }
    return ability;
  }
}

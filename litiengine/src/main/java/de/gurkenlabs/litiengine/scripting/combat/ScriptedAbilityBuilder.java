package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.abilities.AbilityBuilder;
import de.gurkenlabs.litiengine.abilities.CastType;
import de.gurkenlabs.litiengine.abilities.DynamicAbility;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.function.Consumer;

/** Fluent builder for constructing and registering {@link ScriptedAbility} instances. */
public final class ScriptedAbilityBuilder extends AbilityBuilder {

  public ScriptedAbilityBuilder(Creature executor, String name) {
    super(executor, name);
  }

  @Override
  public ScriptedAbilityBuilder description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder cooldown(int cooldownMs) {
    super.cooldown(cooldownMs);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder range(int range) {
    super.range(range);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder impact(int impact) {
    super.impact(impact);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder value(int value) {
    super.value(value);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder castType(CastType castType) {
    super.castType(castType);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder multiTarget(boolean multiTarget) {
    super.multiTarget(multiTarget);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder onCast(Consumer<de.gurkenlabs.litiengine.abilities.AbilityExecution> onCast) {
    super.onCast(onCast);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder onCast(Runnable onCast) {
    super.onCast(onCast);
    return this;
  }

  @Override
  public ScriptedAbilityBuilder effect(Effect effect) {
    super.effect(effect);
    return this;
  }

  @Override
  public ScriptedAbility build() {
    DynamicAbility dynamic = super.build();
    ScriptedAbility ability = new ScriptedAbility((Creature) dynamic.getExecutor(), dynamic.getName());
    ability.setDescription(dynamic.getDescription());
    ability.setCastType(dynamic.getCastType());
    ability.setMultiTarget(dynamic.isMultiTarget());
    ability.getAttributes().cooldown().setValue(dynamic.getAttributes().cooldown().getModifiedValue());
    ability.getAttributes().range().setValue(dynamic.getAttributes().range().getModifiedValue());
    ability.getAttributes().impact().setValue(dynamic.getAttributes().impact().getModifiedValue());
    ability.getAttributes().value().setValue(dynamic.getAttributes().value().getModifiedValue());
    ability.setCastConsumer(dynamic.getCastConsumer());
    for (Effect effect : dynamic.getEffects()) {
      ability.addEffect(effect);
    }
    return ability;
  }
}

package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;
import java.util.function.Consumer;

public interface IEffect {
  public void apply(Shape impactArea);

  public void cease();

  public int getDelay();

  public int getDuration();

  public EffectTarget[] getEffectTargets();

  public List<IEffect> getFollowUpEffects();

  public boolean isActive();

  public void onEffectApplied(Consumer<EffectArgument> consumer);

  public void onEffectCeased(Consumer<EffectArgument> consumer);
}

package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

public interface IEffect extends IUpdateable {
  public static final int NO_DURATION = -1;

  public void apply(IGameLoop loop, Shape impactArea);

  public void cease(final ICombatEntity affectedEntity);

  public int getDelay();

  public int getDuration();

  public EffectTarget[] getEffectTargets();

  public List<IEffect> getFollowUpEffects();

  public List<EffectAppliance> getActiveAppliances();

  public boolean isActive(ICombatEntity entity);

  public void onEffectApplied(Consumer<EffectArgument> consumer);

  public void onEffectCeased(Consumer<EffectArgument> consumer);
}

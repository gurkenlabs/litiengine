package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public interface IEffect extends IUpdateable {
  /**
   * Applies the effect in the specified impact area on the specified
   * environment.
   * 
   * @param loop
   * @param environment
   * @param impactArea
   */
  public void apply(IGameLoop loop, IEnvironment environment, Shape impactArea);

  public void cease(final ICombatEntity affectedEntity);

  public List<EffectApplication> getActiveAppliances();

  public int getDelay();

  public int getDuration();

  public EffectTarget[] getEffectTargets();

  public List<IEffect> getFollowUpEffects();

  public boolean isActive(ICombatEntity entity);

  public void onEffectApplied(Consumer<EffectArgument> consumer);

  public void onEffectCeased(Consumer<EffectArgument> consumer);
}

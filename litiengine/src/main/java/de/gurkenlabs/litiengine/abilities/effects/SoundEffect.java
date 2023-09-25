package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import java.util.Arrays;

public class SoundEffect extends Effect {

  private final Sound[] sounds;

  /**
   * Initializes a new instance of the {@code SoundEffect} class.
   *
   * @param ability The ability that performs the effect.
   * @param sounds  The sounds to chose from when applying the effect.
   */
  public SoundEffect(final Ability ability, final Sound... sounds) {
    super(ability, EffectTarget.EXECUTINGENTITY);
    this.sounds = sounds;
  }

  public SoundEffect(final Ability ability, final String... sounds) {
    super(ability, EffectTarget.EXECUTINGENTITY);
    this.sounds =
      Arrays.stream(sounds).map(x -> Resources.sounds().get(x)).toArray(Sound[]::new);
  }

  @Override
  protected void apply(final ICombatEntity entity) {
    super.apply(entity);
    Game.audio().playSound(Game.random().choose(this.sounds), entity);
  }
}

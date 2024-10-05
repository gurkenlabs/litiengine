package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import java.util.Arrays;

public class SoundEffect extends Effect {

  private final Sound[] sounds;

  /**
   * Initializes a new instance of the {@code SoundEffect} class.
   *
   * @param sounds The sounds to chose from when applying the effect.
   */
  public SoundEffect(ICombatEntity executingEntity, final Sound... sounds) {
    super(TargetingStrategy.executingEntity(), executingEntity);
    this.sounds = sounds;
  }

  /**
   * Initializes a new instance of the {@code SoundEffect} class with the specified sounds.
   *
   * @param sounds The sounds to choose from when applying the effect.
   */
  public SoundEffect(final Sound... sounds) {
    this(null, sounds);
  }

  /**
   * Initializes a new instance of the {@code SoundEffect} class with the specified executing entity and sounds.
   *
   * @param executingEntity The entity executing the effect.
   * @param sounds          The names of the sounds to choose from when applying the effect.
   */
  public SoundEffect(ICombatEntity executingEntity, final String... sounds) {
    this(executingEntity, Arrays.stream(sounds).map(x -> Resources.sounds().get(x)).toArray(Sound[]::new));
  }

  /**
   * Initializes a new instance of the {@code SoundEffect} class with the specified sounds.
   *
   * @param sounds The names of the sounds to choose from when applying the effect.
   */
  public SoundEffect(final String... sounds) {
    this(null, sounds);
  }

  /**
   * Applies the sound effect to the specified entity, playing a random sound from the list of sounds.
   *
   * @param entity The entity to which the effect is applied.
   */
  @Override
  protected void apply(final ICombatEntity entity) {
    super.apply(entity);
    Game.audio().playSound(Game.random().choose(this.sounds), entity);
  }
}

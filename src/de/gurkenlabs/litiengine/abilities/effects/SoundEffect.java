package de.gurkenlabs.litiengine.abilities.effects;

import java.util.Random;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.sound.Sound;

public class SoundEffect extends Effect {
  private final Sound[] sounds;

  public SoundEffect(final Ability ability, final Sound... sounds) {
    super(ability, EffectTarget.EXECUTINGENTITY);
    this.sounds = sounds;
  }

  @Override
  protected void apply(final ICombatEntity entity) {
    super.apply(entity);
    if (this.sounds.length == 0) {
      return;
    }

    Game.audio().playSound(entity, this.getRandomSound());
  }

  private Sound getRandomSound() {
    if (this.sounds.length == 0) {
      return null;
    }

    final int randomIndex = new Random().nextInt(this.sounds.length);
    return this.sounds[randomIndex];
  }
}

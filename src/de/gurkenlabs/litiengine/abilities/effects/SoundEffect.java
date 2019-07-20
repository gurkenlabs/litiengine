package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

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

    Game.audio().playSound(this.getRandomSound(), entity);
  }

  private Sound getRandomSound() {
    if (this.sounds.length == 0) {
      return null;
    }

    return ArrayUtilities.getRandom(this.sounds);
  }
}

package de.gurkenlabs.litiengine.sounds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.ISoundPlayback;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.sound.SoundEvent;
import de.gurkenlabs.litiengine.sound.SoundPlaybackAdapter;

public class SoundTests {
  private static boolean finished;

  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMADLINE_ARG_NOGUI);
    finished = false;
  }

  @Test
  public void testLoading() {
    Sound sound = Resources.sounds().get("tests/de/gurkenlabs/litiengine/sounds/test.ogg");

    assertNotNull(sound);

    ISoundPlayback playback = Game.getSoundEngine().playSound(sound);

    playback.addSoundPlaybackListener(new SoundPlaybackAdapter() {

      @Override
      public void finished(SoundEvent event) {
        finished = true;
      }
    });

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertTrue(finished);
  }
}

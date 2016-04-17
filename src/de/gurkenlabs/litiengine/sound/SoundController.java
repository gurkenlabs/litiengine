package de.gurkenlabs.litiengine.sound;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.sound.ISoundEngine;

public class SoundController {
  private static final int LOCK_TIME = 50;
  private static long lastPlay;

  public static boolean canPlay() {
    if (Game.getLoop().getDeltaTime(lastPlay) > LOCK_TIME) {
      return true;
    }

    return false;
  }

  public static void call(Consumer<ISoundEngine> engine) {
    if (canPlay()) {
      Thread t = new SoundPlayThread(engine);
      t.run();
      lastPlay = Game.getLoop().getTicks();
    }
  }

  public static void callIgnoreTimeout(Consumer<ISoundEngine> engine) {
    Thread t = new SoundPlayThread(engine);
    t.start();
  }

  public static class SoundPlayThread extends Thread {
    private Consumer<ISoundEngine> engine;

    public SoundPlayThread(Consumer<ISoundEngine> engine) {
      this.engine = engine;
    }

    @Override
    public void run() {
      this.engine.accept(Game.getSoundEngine());
    }
  }
}

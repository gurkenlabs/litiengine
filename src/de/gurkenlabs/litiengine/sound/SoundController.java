package de.gurkenlabs.litiengine.sound;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;

public class SoundController {
  private static final int LOCK_TIME = 50;
  private static long lastPlay;

  public static boolean canPlay() {
    if (Game.getLoop().getDeltaTime(lastPlay) > LOCK_TIME) {
      return true;
    }

    return false;
  }

  public static void call(final Consumer<ISoundEngine> engine) {
    if (canPlay()) {
      final Thread t = new SoundPlayThread(engine);
      t.run();
      lastPlay = Game.getLoop().getTicks();
    }
  }

  public static void callIgnoreTimeout(final Consumer<ISoundEngine> engine) {
    final Thread t = new SoundPlayThread(engine);
    t.start();
  }

  public static class SoundPlayThread extends Thread {
    private final Consumer<ISoundEngine> engine;

    public SoundPlayThread(final Consumer<ISoundEngine> engine) {
      this.engine = engine;
    }

    @Override
    public void run() {
      this.engine.accept(Game.getSoundEngine());
    }
  }
}

package de.gurkenlabs.litiengine.sound;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;

public class SoundController {
  private static final int LOCK_TIME = 50;
  private static long lastPlay;

  private static final SoundPlayThread soundPlayThread = new SoundPlayThread();

  public static boolean canPlay() {
    if (Game.getLoop().getDeltaTime(lastPlay) > LOCK_TIME) {
      return true;
    }

    return false;
  }

  public static void call(final Consumer<ISoundEngine> engine) {
    if (canPlay()) {
      soundPlayThread.enqueue(engine, false);
      lastPlay = Game.getLoop().getTicks();
    }
  }

  public static void callIgnoreTimeout(final Consumer<ISoundEngine> engine, boolean force) {
    soundPlayThread.enqueue(engine, force);
  }

  public static void start() {
    soundPlayThread.start();
  }

  public static void terminate() {
    soundPlayThread.setRunning(false);
  }

  public static class SoundPlayThread extends Thread {
    private boolean isRunning = true;
    private final Map<Consumer<ISoundEngine>, Long> reqTime = new ConcurrentHashMap<>();
    private final Queue<Consumer<ISoundEngine>> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
      while (this.isRunning) {
        if (this.queue.peek() != null) {
          Consumer<ISoundEngine> consumer = this.queue.poll();
          if (this.reqTime.containsKey(consumer)) {
            if (Game.getLoop().getDeltaTime(this.reqTime.get(consumer)) < 500) {
              consumer.accept(Game.getSoundEngine());
            }

            this.reqTime.remove(consumer);
          } else {
            consumer.accept(Game.getSoundEngine());
          }
        }
        try {
          Thread.sleep(20);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    public boolean isRunning() {
      return isRunning;
    }

    public void setRunning(boolean isRunning) {
      this.isRunning = isRunning;
    }

    public void enqueue(Consumer<ISoundEngine> consumer, boolean force) {
      this.queue.add(consumer);
      if (!force) {
        this.reqTime.put(consumer, Game.getLoop().getTicks());
      }
    }

  }
}

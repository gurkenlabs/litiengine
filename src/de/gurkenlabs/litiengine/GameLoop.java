package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameLoop extends Thread implements IGameLoop {
  private final List<IUpdateable> updatables;
  private final List<Consumer<Integer>> upsTrackedConsumer;

  private final int updateRate;
  private int totalTicks;

  private int updateCount;
  private long lastUpsTime;
  private long lastUpdateTime;

  private boolean gameIsRunning = true;

  /** The next game tick. */
  private long nextGameTick = System.currentTimeMillis();

  public GameLoop(final int updateRate) {
    this.updatables = new CopyOnWriteArrayList<>();
    this.upsTrackedConsumer = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
  }

  @Override
  public long convertToMs(final long ticks) {
    return (long) (ticks / (this.updateRate / 1000.0));
  }

  @Override
  public long getDeltaTime() {
    return System.currentTimeMillis() - this.lastUpdateTime;
  }

  @Override
  public long getDeltaTime(final long ticks) {
    return this.convertToMs(this.totalTicks - ticks);
  }

  @Override
  public long getTicks() {
    return this.totalTicks;
  }

  @Override
  public int getUpdateRate() {
    return this.updateRate;
  }

  @Override
  public void onUpsTracked(final Consumer<Integer> upsConsumer) {
    if (!this.upsTrackedConsumer.contains(upsConsumer)) {
      this.upsTrackedConsumer.add(upsConsumer);
    }
  }

  @Override
  public void registerForUpdate(final IUpdateable updatable) {
    if (!this.updatables.contains(updatable)) {
      this.updatables.add(updatable);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (this.gameIsRunning) {
      final int SKIP_TICKS = 1000 / this.updateRate;

      if (System.currentTimeMillis() > this.nextGameTick) {
        ++this.totalTicks;
        this.updatables.forEach(updatable -> updatable.update());

        ++this.updateCount;

        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - this.lastUpsTime >= 1000) {
          this.lastUpsTime = currentMillis;
          this.upsTrackedConsumer.forEach(consumer -> consumer.accept(this.updateCount));
          this.updateCount = 0;
        }

        this.lastUpdateTime = currentMillis;

        this.nextGameTick += SKIP_TICKS;
      }
    }
  }

  @Override
  public void terminate() {
    this.gameIsRunning = false;
  }

  @Override
  public void unregisterFromUpdate(final IUpdateable updatable) {
    if (this.updatables.contains(updatable)) {
      this.updatables.remove(updatable);
    }
  }
}

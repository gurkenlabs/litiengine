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
  
  public GameLoop(int updateRate) {
    this.updatables = new CopyOnWriteArrayList<>();
    this.upsTrackedConsumer = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
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
        updatables.forEach(updatable -> updatable.update());

        ++this.updateCount;

        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - lastUpsTime >= 1000) {
          lastUpsTime = currentMillis;
          upsTrackedConsumer.forEach(consumer -> consumer.accept(updateCount));
          updateCount = 0;
        }

        lastUpdateTime = currentMillis;

        this.nextGameTick += SKIP_TICKS;
      }
    }
  }

  @Override
  public void terminate() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpsTracked(Consumer<Integer> upsConsumer) {
    if (!this.upsTrackedConsumer.contains(upsConsumer)) {
      this.upsTrackedConsumer.add(upsConsumer);
    }
  }

  @Override
  public void registerForUpdate(IUpdateable updatable) {
    if (!this.updatables.contains(updatable)) {
      this.updatables.add(updatable);
    }
  }

  @Override
  public void unregisterFromUpdate(IUpdateable updatable) {
    if (this.updatables.contains(updatable)) {
      this.updatables.remove(updatable);
    }
  }

  @Override
  public long getTicks() {
    return this.totalTicks;
  }

  @Override
  public long convertToMs(long ticks) {
    return (long) (ticks / (this.updateRate / 1000.0));
  }

  @Override
  public long getDeltaTime() {
    return System.currentTimeMillis() - lastUpdateTime;
  }

  @Override
  public long getDeltaTime(long ticks) {
    return convertToMs(this.totalTicks - ticks);
  }
}

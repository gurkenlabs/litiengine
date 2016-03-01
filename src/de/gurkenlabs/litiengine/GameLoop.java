package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameLoop extends Thread implements IGameLoop {
  private final List<IUpdateable> updatables;
  private final List<Consumer<Integer>> upsTrackedConsumer;

  private final int updateRate;
  private final GameTime gameTime;
  private float timeScale;
  private int totalTicks;

  private int updateCount;
  private long lastUpsTime;
  private long lastUpdateTime;

  private boolean gameIsRunning = true;

  public GameLoop(final int updateRate) {
    this.updatables = new CopyOnWriteArrayList<>();
    this.upsTrackedConsumer = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
    this.gameTime = new GameTime(this);
    this.setTimeScale(1.0F);
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
      final int SKIP_TICKS = (int) (1000 / (this.getUpdateRate() * this.getTimeScale()));

      ++this.totalTicks;
      this.updatables.parallelStream().forEach(updatable -> updatable.update(this));

      ++this.updateCount;

      final long currentMillis = System.currentTimeMillis();
      if (currentMillis - this.lastUpsTime >= 1000) {
        this.lastUpsTime = currentMillis;
        this.upsTrackedConsumer.forEach(consumer -> consumer.accept(this.updateCount));
        this.updateCount = 0;
      }

      this.lastUpdateTime = currentMillis;

      try {
        Thread.sleep(SKIP_TICKS);
      } catch (InterruptedException e) {
        e.printStackTrace();
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

  @Override
  public float getTimeScale() {
    return this.timeScale;
  }

  @Override
  public void setTimeScale(float timeScale) {
    this.timeScale = timeScale;
  }

  @Override
  public GameTime getTime() {
    return this.gameTime;
  }
}

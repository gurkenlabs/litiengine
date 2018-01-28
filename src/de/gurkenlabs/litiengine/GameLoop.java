package de.gurkenlabs.litiengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLoop extends UpdateLoop implements IGameLoop, AutoCloseable {
  private static final Logger log = Logger.getLogger(GameLoop.class.getName());
  private static int executionIndex = -1;

  private final List<TimedAction> actions;
  private final int updateRate;
  private final List<Consumer<Integer>> upsTrackedConsumer;

  private long deltaTime;
  private boolean gameIsRunning = true;

  private long lastUpsTime;

  private float timeScale;
  private long totalTicks;

  private int updateCount;

  public GameLoop(final int updateRate) {
    super();
    this.upsTrackedConsumer = new CopyOnWriteArrayList<>();
    this.actions = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
    this.setTimeScale(1.0F);
  }

  @Override
  public void close() {
    this.gameIsRunning = false;
  }

  @Override
  public long convertToMs(final long ticks) {
    return (long) (ticks / (this.updateRate / 1000.0));
  }

  @Override
  public long convertToTicks(final int ms) {
    return (long) (this.updateRate / 1000.0 * ms);
  }

  @Override
  public int execute(final int delay, final Consumer<Integer> action) {
    final long d = this.convertToTicks(delay);

    TimedAction a = new TimedAction(this.getTicks() + d, action);
    this.actions.add(a);

    return a.getIndex();
  }

  @Override
  public int execute(int delay, Runnable action) {
    final long d = this.convertToTicks(delay);

    TimedAction a = new TimedAction(this.getTicks() + d, action);
    this.actions.add(a);

    return a.getIndex();
  }

  @Override
  public long getDeltaTime() {
    return this.deltaTime;
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
  public float getTimeScale() {
    return this.timeScale;
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
  public void run() {
    while (this.gameIsRunning) {
      final float scale = this.getTimeScale() > 0 ? this.getTimeScale() : 1;
      final long tickWait = (long) (1.0 / (this.getUpdateRate() * scale) * 1000);
      final long updateStart = System.nanoTime();

      if (this.getTimeScale() > 0) {
        ++this.totalTicks;
        this.update();
        this.executeTimedActions();
      }

      ++this.updateCount;

      final long currentMillis = System.currentTimeMillis();
      this.trackUpdateRate(currentMillis);

      final long lastUpdateTime = currentMillis;
      final long updateTime = (System.nanoTime() - updateStart) / 1000000;
      try {
        Thread.sleep(Math.max(0, tickWait - updateTime));
      } catch (final InterruptedException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        Thread.currentThread().interrupt();
        break;
      }

      this.deltaTime = System.currentTimeMillis() - lastUpdateTime + updateTime;
    }
  }

  @Override
  public void setTimeScale(final float timeScale) {
    this.timeScale = timeScale;
  }

  @Override
  public void terminate() {
    this.gameIsRunning = false;
  }

  @Override
  public void updateExecutionTime(int index, long ticks) {
    for (TimedAction action : this.actions) {
      if (action.getIndex() == index) {
        action.setExecutionTicks(ticks);
      }
    }
  }

  private void executeTimedActions() {
    final List<TimedAction> executed = new ArrayList<>();
    for (final TimedAction action : this.actions) {
      if (action.getExecutionTick() <= this.totalTicks) {

        if (action.getConsumerAction() != null) {
          action.getConsumerAction().accept(action.getIndex());
        } else {
          action.getAction().run();
        }

        executed.add(action);
      }
    }

    this.actions.removeAll(executed);
  }

  private void trackUpdateRate(long currentMillis) {
    if (currentMillis - this.lastUpsTime >= 1000) {
      this.lastUpsTime = currentMillis;
      this.upsTrackedConsumer.forEach(consumer -> consumer.accept(this.updateCount));
      this.updateCount = 0;
    }
  }

  private class TimedAction {
    private final Consumer<Integer> consumerAction;
    private final Runnable action;
    private long execution;
    private final int index;

    private TimedAction(final long execution, final Runnable action) {
      this.execution = execution;
      this.consumerAction = null;
      this.action = action;
      this.index = ++executionIndex;
    }

    private TimedAction(final long execution, final Consumer<Integer> action) {
      this.execution = execution;
      this.consumerAction = action;
      this.action = null;
      this.index = ++executionIndex;
    }

    public Consumer<Integer> getConsumerAction() {
      return this.consumerAction;
    }

    public Runnable getAction() {
      return this.action;
    }

    public long getExecutionTick() {
      return this.execution;
    }

    public void setExecutionTicks(long ticks) {
      this.execution = ticks;
    }

    public int getIndex() {
      return index;
    }
  }
}

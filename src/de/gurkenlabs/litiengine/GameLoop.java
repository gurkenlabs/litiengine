package de.gurkenlabs.litiengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.util.TimeUtilities;

public class GameLoop extends UpdateLoop implements AutoCloseable {
  /**
   * The tick {@link #getDeltaTime()} at which we consider the game not to run fluently anymore.
   * <ul>
   * <li>16.6 ms: 60 FPS</li>
   * <li>33.3 ms: 30 FPS</li>
   * <li>66.6 ms: 15 FPS</li>
   * </ul>
   */
  public static final int TICK_DELTATIME_LAG = 67;

  private static int executionIndex = -1;

  private final List<TimedAction> actions;
  private final int updateRate;

  private long deltaTime;

  private long lastUpsTime;

  private float timeScale;
  private long totalTicks;

  private int updateCount;

  public GameLoop(final int updateRate) {
    super();
    this.actions = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
    this.setTimeScale(1.0F);
  }

  @Override
  public void close() {
    this.terminate();
  }

  public long convertToMs(final long ticks) {
    return (long) (ticks / (this.updateRate / 1000.0));
  }

  public long convertToTicks(final int ms) {
    return (long) (this.updateRate / 1000.0 * ms);
  }

  public int execute(int delay, Runnable action) {
    final long d = this.convertToTicks(delay);

    TimedAction a = new TimedAction(this.getTicks() + d, action);
    this.actions.add(a);

    return a.getIndex();
  }

  /**
   * Gets the time that passed since the last tick in ms.
   *
   * @return The delta time in ms.
   */
  public long getDeltaTime() {
    return this.deltaTime;
  }

  /**
   * Calculates the deltatime between the current game time and the specified
   * ticks in ms.
   *
   * @param ticks
   *          The ticks for which to calculate the delta time.
   * @return The delta time in ms.
   */
  public long getDeltaTime(final long ticks) {
    return this.convertToMs(this.totalTicks - ticks);
  }

  public long getTicks() {
    return this.totalTicks;
  }

  public float getTimeScale() {
    return this.timeScale;
  }

  public int getUpdateRate() {
    return this.updateRate;
  }

  @Override
  public void run() {
    while (!interrupted()) {
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
      final long updateTime = (long) TimeUtilities.nanoToMs(System.nanoTime() - updateStart);
      try {
        Thread.sleep(Math.max(0, tickWait - updateTime));
      } catch (final InterruptedException e) {
        break;
      }

      this.deltaTime = System.currentTimeMillis() - lastUpdateTime + updateTime;
    }
  }

  public void setTimeScale(final float timeScale) {
    this.timeScale = timeScale;
  }

  @Override
  public void terminate() {
    this.interrupt();
  }

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

        action.getAction().run();
        executed.add(action);
      }
    }

    this.actions.removeAll(executed);
  }

  private void trackUpdateRate(long currentMillis) {
    if (currentMillis - this.lastUpsTime >= 1000) {
      this.lastUpsTime = currentMillis;
      Game.getMetrics().setUpdatesPerSecond(this.updateCount);
      this.updateCount = 0;
    }
  }

  private class TimedAction {
    private final Runnable action;
    private long execution;
    private final int index;

    private TimedAction(final long execution, final Runnable action) {
      this.execution = execution;
      this.action = action;
      this.index = ++executionIndex;
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

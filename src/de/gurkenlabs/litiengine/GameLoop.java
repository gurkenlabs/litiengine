package de.gurkenlabs.litiengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The main update loop that executes the game logic by calling the update functions on all registered <code>IUpdatable</code> instances.
 * Subsequently, it performs the rendering of the current frame and tracks some performance metrics on the process.
 *
 * @see IUpdateable#update()
 * @see Game#loop()
 * @see RenderComponent#render()
 */
public final class GameLoop extends UpdateLoop implements IGameLoop {
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

  private float timeScale;

  GameLoop(String name, final int updateRate) {
    super(name, updateRate);
    this.actions = new CopyOnWriteArrayList<>();
    this.setTimeScale(1.0F);
  }

  @Override
  public int perform(int delay, Runnable action) {
    final long d = Game.time().toTicks(delay);

    TimedAction a = new TimedAction(this.getTicks() + d, action);
    this.actions.add(a);

    return a.getId();
  }

  @Override
  public float getTimeScale() {
    return this.timeScale;
  }

  @Override
  public void setTimeScale(final float timeScale) {
    this.timeScale = timeScale;
  }

  @Override
  public void alterExecutionTime(int index, long ticks) {
    for (TimedAction action : this.actions) {
      if (action.getId() == index) {
        action.setExecutionTicks(ticks);
      }
    }
  }

  @Override
  public void removeAction(int id) {
    this.actions.removeIf(x -> x.getId() == id);
  }

  /**
   * In addition to the normal base implementation, the <code>GameLoop</code> performs registered action at the required
   * time and tracks some detailed metrics.
   */
  @Override
  protected void process() {
    Game.world().camera().updateFocus();
    if (this.getTimeScale() > 0) {
      super.process();
      this.executeTimedActions();
    }

    if (!Game.isInNoGUIMode()) {
      Game.window().getRenderComponent().render();
    }

    this.trackRenderMetric();
  }

  @Override
  protected long getExpectedDelta() {
    final float scale = this.getTimeScale() > 0 ? this.getTimeScale() : 1;
    return (long) (1000 / (this.getTickRate() * scale));
  }

  private void executeTimedActions() {
    final List<TimedAction> executed = new ArrayList<>();
    for (final TimedAction action : this.actions) {
      if (action.getExecutionTick() <= this.getTicks()) {

        action.getAction().run();
        executed.add(action);
      }
    }

    this.actions.removeAll(executed);
  }

  private void trackRenderMetric() {
    Game.metrics().setEstimatedMaxFramesPerSecond((int) (1000.0 / this.getProcessTime()));
    if (Game.config().debug().trackRenderTimes()) {
      Game.metrics().trackRenderTime("total", this.getProcessTime());
    }
  }

  private class TimedAction {
    private final Runnable action;
    private long execution;
    private final int id;

    private TimedAction(final long execution, final Runnable action) {
      this.execution = execution;
      this.action = action;
      this.id = ++executionIndex;
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

    public int getId() {
      return id;
    }
  }
}

package de.gurkenlabs.litiengine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GameLoop extends Thread implements IGameLoop, AutoCloseable {
  private class TimedAction {
    private final Consumer<Integer> action;
    private long execution;
    private final int index;

    private TimedAction(final long execution, final Consumer<Integer> action) {
      this.execution = execution;
      this.action = action;
      this.index = ++executionIndex;
    }

    public Consumer<Integer> getAction() {
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

  private static final Logger log = Logger.getLogger(GameLoop.class.getName());
  private final List<TimedAction> actions;
  private static int executionIndex = -1;

  private long deltaTime;
  private boolean gameIsRunning = true;
  private final GameTime gameTime;
  private long lastUpdateTime;
  private long lastUpsTime;

  private float timeScale;
  private long totalTicks;
  private final List<IUpdateable> updatables;
  private int updateCount;
  private final int updateRate;

  private final List<Consumer<Integer>> upsTrackedConsumer;

  public GameLoop(final int updateRate) {
    this.updatables = new CopyOnWriteArrayList<>();
    this.upsTrackedConsumer = new CopyOnWriteArrayList<>();
    this.actions = new CopyOnWriteArrayList<>();
    this.updateRate = updateRate;
    this.gameTime = new GameTime(this);
    this.setTimeScale(1.0F);
  }

  @Override
  public void attach(final IUpdateable updatable) {
    if (updatable == null) {
      return;
    }

    if (this.updatables.contains(updatable)) {
      System.out.println("Updatable " + updatable + " already registered for update!");
      return;
    }

    this.updatables.add(updatable);
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
  public void detach(final IUpdateable updatable) {
    this.updatables.remove(updatable);
  }

  @Override
  public int execute(final int delay, final Consumer<Integer> action) {
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
  public GameTime getTime() {
    return this.gameTime;
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

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (this.gameIsRunning) {
      final float scale = this.getTimeScale() > 0 ? this.getTimeScale() : 1;
      final long tickWait = (long) (1.0 / (this.getUpdateRate() * scale) * 1000);
      final long updateStart = System.nanoTime();

      if (this.getTimeScale() > 0) {
        ++this.totalTicks;
        this.updatables.forEach(updatable -> {
          try {
            if (updatable != null) {
              updatable.update(this);
            }
          } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String stacktrace = sw.toString();
            log.severe(stacktrace);
          }
        });

        final List<TimedAction> executed = new ArrayList<>();
        for (final TimedAction action : this.actions) {
          if (action.getExecutionTick() <= this.totalTicks) {
            action.getAction().accept(action.getIndex());
            executed.add(action);
          }
        }

        this.actions.removeAll(executed);
      }

      ++this.updateCount;

      final long currentMillis = System.currentTimeMillis();
      if (currentMillis - this.lastUpsTime >= 1000) {
        this.lastUpsTime = currentMillis;
        this.upsTrackedConsumer.forEach(consumer -> consumer.accept(this.updateCount));
        this.updateCount = 0;
      }

      this.lastUpdateTime = currentMillis;

      final long updateTime = (System.nanoTime() - updateStart) / 1000000;
      try {
        Thread.sleep(Math.max(0, tickWait - updateTime));
      } catch (final InterruptedException e) {
        Thread.interrupted();
        break;
      }

      this.deltaTime = System.currentTimeMillis() - this.lastUpdateTime + updateTime;
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
}

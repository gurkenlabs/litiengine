package de.gurkenlabs.litiengine;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.util.TimeUtilities;

public class UpdateLoop extends Thread implements AutoCloseable, ILoop {
  private static final Logger log = Logger.getLogger(UpdateLoop.class.getName());
  private final Set<IUpdateable> updatables;

  private int tickRate;

  private long totalTicks;

  private long deltaTime;
  private double processTime;
  private double delayError;

  protected UpdateLoop(String name, int tickRate) {
    super(name);
    this.updatables = Collections.newSetFromMap(new ConcurrentHashMap<IUpdateable, Boolean>());
    this.tickRate = tickRate;
  }

  /**
   * The loop implementation, executing the <code>process()</code> method which does the actual work.
   * It also tracks the processing time and the total number of performed ticks while making sure that the expected
   * tick rate is met by delaying the loop accordingly.
   * 
   * @see #process()
   * @see #delay()
   * @see #getDeltaTime()
   * @see #getProcessTime()
   */
  @Override
  public void run() {
    while (!interrupted()) {
      ++this.totalTicks;

      final long start = System.nanoTime();

      this.process();

      // delay tick to meet the expected rate
      this.processTime = TimeUtilities.nanoToMs(System.nanoTime() - start);
      double delay = this.delay();
      this.deltaTime = (long) (delay + this.processTime);
    }
  }

  @Override
  public void terminate() {
    this.interrupt();
  }

  @Override
  public void close() {
    this.terminate();
  }

  @Override
  public void attach(final IUpdateable updatable) {
    if (updatable == null) {
      return;
    }

    if (!this.updatables.add(updatable)) {
      log.log(Level.FINE, "Updatable {0} already registered for update!", new Object[] { updatable });
      return;
    }
  }

  @Override
  public void detach(final IUpdateable updatable) {
    this.updatables.remove(updatable);
  }

  @Override
  public long getTicks() {
    return this.totalTicks;
  }

  @Override
  public int getTickRate() {
    return this.tickRate;
  }

  @Override
  public long getDeltaTime() {
    return this.deltaTime;
  }

  @Override
  public double getProcessTime() {
    return this.processTime;
  }

  protected Set<IUpdateable> getUpdatables() {
    return this.updatables;
  }

  /**
   * Performs the actual workload of a tick. This base implementation just calls the update method on all registered instances.
   * For derived loop implementations this is more sophisticated.
   */
  protected void process() {
    this.update();
  }

  protected long getExpectedDelta() {
    return (long) (1000.0 / this.tickRate);
  }

  /**
   * Calls the <code>update()</code> procedure on all registered instances.
   * 
   * @see IUpdateable#update()
   */
  protected void update() {
    for (IUpdateable updatable : this.getUpdatables()) {
      try {
        if (updatable != null) {
          updatable.update();
        }
      } catch (final Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  /**
   * This method determines how long the current tick should be delayed to match the expected delta time for the specified tick rate.
   * It then delays the execution of this loop by pausing the thread for the necessary delay.
   * 
   * @return The delay for which this tick was paused after the actual processing.
   */
  protected double delay() {
    double delay = Math.max(0, this.getExpectedDelta() - this.getProcessTime());
    long sleepDelay = Math.round(delay);

    // since thread sleep only supports long values and no double values, there will always be some error that we need to account for
    // we'll aggregate the error until its absolute value is greater than 1 and then add it to the current delay and subtract it from the current delay error 
    this.delayError += delay - sleepDelay;
    if (Math.abs(this.delayError) > 1) {
      long errorAdjustment = (long) this.delayError;
      sleepDelay += errorAdjustment;
      this.delayError -= errorAdjustment;
    }

    if (delay > 0) {
      try {
        sleep(sleepDelay);
      } catch (final InterruptedException e) {
        this.interrupt();
      }
    }

    return delay;
  }

  protected void setTickRate(int tickRate) {
    this.tickRate = tickRate;
  }
}

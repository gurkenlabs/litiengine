package de.gurkenlabs.litiengine;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UpdateLoop extends Thread implements ILaunchable {
  private static final Logger log = Logger.getLogger(UpdateLoop.class.getName());
  private final Set<IUpdateable> updatables;

  protected UpdateLoop() {
    this.updatables = Collections.newSetFromMap(new ConcurrentHashMap<IUpdateable, Boolean>());
  }

  @Override
  public void run() {
    // nothing to do for now in the base implementation
  }

  /**
   * Attaches the update method of the specified IUpdatable instance to be called
   * every tick. The tick rate can be configured in the client configuration and
   * is independent from rendering.
   * 
   * @param updatable
   *          The instance that will be registered for the update event.
   */
  public void attach(final IUpdateable updatable) {
    if (updatable == null) {
      return;
    }

    if (!this.updatables.add(updatable)) {
      log.log(Level.FINE, "Updatable {0} already registered for update!", new Object[] { updatable });
      return;
    }
  }

  /**
   * Detaches the specified instance from the game loop.
   * 
   * @param updatable The instance that will be unregistered for the update event.
   */
  public void detach(final IUpdateable updatable) {
    this.updatables.remove(updatable);
  }

  protected Set<IUpdateable> getUpdatables() {
    return this.updatables;
  }

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
}

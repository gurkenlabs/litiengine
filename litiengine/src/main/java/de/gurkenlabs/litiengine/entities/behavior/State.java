package de.gurkenlabs.litiengine.entities.behavior;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an abstract state in a state machine.
 */
public abstract class State {

  private final String name;
  private final List<Transition> transitions;
  private final Collection<StateListener> listeners = ConcurrentHashMap.newKeySet();

  /**
   * Initializes a new instance of the State class with the specified name.
   *
   * @param name the name of the state
   */
  protected State(final String name) {
    this.transitions = new CopyOnWriteArrayList<>();
    this.name = name;
  }

  /**
   * Called when the state is entered. Notifies all registered listeners about the state entry.
   */
  public void enter() {
    StateEvent event = new StateEvent(this);
    listeners.forEach(listener -> listener.onEnter(event));
  }

  /**
   * Called when the state is exited. Notifies all registered listeners about the state exit.
   */
  public void exit() {
    StateEvent event = new StateEvent(this);
    listeners.forEach(listener -> listener.onExit(event));
  }

  /**
   * Gets the name of the state.
   *
   * @return the name of the state
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the list of transitions associated with this state.
   *
   * @return the list of transitions
   */
  public List<Transition> getTransitions() {
    return this.transitions;
  }

  /**
   * Adds a listener to be notified of state events.
   *
   * @param listener the listener to be added
   */
  public void addStateListener(final StateListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener from being notified of state events.
   *
   * @param listener the listener to be removed
   */
  public void removeStateListener(final StateListener listener) {
    listeners.remove(listener);
  }

  /**
   * Performs the actions associated with this state.
   */
  protected abstract void perform();
}

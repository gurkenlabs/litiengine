package de.gurkenlabs.litiengine.entities.behavior;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class State {

  private final String name;

  private final List<Transition> transitions;
  private final Collection<StateListener> listeners = ConcurrentHashMap.newKeySet();


  protected State(final String name) {
    this.transitions = new CopyOnWriteArrayList<>();
    this.name = name;
  }

  public void enter() {
    StateEvent event = new StateEvent(this);
    listeners.forEach(listener -> listener.onEnter(event));
  }

  public void exit() {
    StateEvent event = new StateEvent(this);
    listeners.forEach(listener -> listener.onExit(event));
  }

  public String getName() {
    return this.name;
  }

  public List<Transition> getTransitions() {
    return this.transitions;
  }

  public void addStateListener(final StateListener listener) {
    listeners.add(listener);
  }

  public void removeStateListener(final StateListener listener) {
    listeners.remove(listener);
  }

  protected abstract void perform();
}

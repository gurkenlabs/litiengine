package de.gurkenlabs.util.states;


public interface ITransition extends Comparable<ITransition> {
  public int getPriority();

  public boolean conditionsFullfilled();

  public IState getState();

  public IState getNextState();
}

package de.gurkenlabs.util.states;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IStateMachine extends IUpdateable {

  public void setState(final IState newState);

  public IState getCurrentState();
  
}

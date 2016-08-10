package de.gurkenlabs.util.states;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IStateMachine extends IUpdateable {

  public IState getCurrentState();

  public void setState(final IState newState);

}

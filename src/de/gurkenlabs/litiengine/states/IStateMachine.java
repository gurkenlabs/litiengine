package de.gurkenlabs.litiengine.states;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IStateMachine extends IUpdateable {

  public IState getCurrentState();

  public void setState(final IState newState);

}

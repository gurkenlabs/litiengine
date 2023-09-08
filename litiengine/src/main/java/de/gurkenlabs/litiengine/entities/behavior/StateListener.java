package de.gurkenlabs.litiengine.entities.behavior;

import java.util.EventListener;

public interface StateListener extends EventListener {

  void onEnter(StateEvent state);

  void onExit(StateEvent state);

}

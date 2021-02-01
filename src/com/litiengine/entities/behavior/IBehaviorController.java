package com.litiengine.entities.behavior;

import com.litiengine.Game;
import com.litiengine.entities.IEntityController;

public interface IBehaviorController extends IEntityController {

  public default void detach() {
    Game.loop().detach(this);
  }

  public default void attach() {
    Game.loop().attach(this);
  }
}

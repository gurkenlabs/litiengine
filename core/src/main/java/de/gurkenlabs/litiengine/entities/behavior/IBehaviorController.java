package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntityController;

public interface IBehaviorController extends IEntityController {

  default void detach() {
    Game.loop().detach(this);
  }

  default void attach() {
    Game.loop().attach(this);
  }
}

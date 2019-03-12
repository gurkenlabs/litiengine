package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntityController;

public interface IBehaviorController extends IEntityController {

  public default void detach() {
    Game.loop().detach(this);
  }

  public default void attach() {
    Game.loop().attach(this);
  }
}

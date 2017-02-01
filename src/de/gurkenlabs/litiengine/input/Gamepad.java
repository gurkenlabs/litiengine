package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;

public class Gamepad implements IGamepad, IUpdateable {
  private final int index;
  private final Controller controller;

  protected Gamepad(final int index, Controller controller) {
    this.index = index;
    this.controller = controller;
    Game.getLoop().registerForUpdate(this);
  }

  public int getIndex() {
    return this.index;
  }

  public String getName() {
    return this.controller.getName();
  }

  @Override
  public void dispose() {
    Game.getLoop().unregisterFromUpdate(this);
    Input.GAMEPADS.remove(this);
  }

  @Override
  public void update(IGameLoop loop) {
    boolean couldPoll = this.controller.poll();
    if (!couldPoll) {
      this.dispose();
    }
  }

  @Override
  public float getPollData(Identifier identifier) {
    Component comp = this.controller.getComponent(identifier);
    if (comp == null) {
      return 0;
    }

    return comp.getPollData();
  }
}

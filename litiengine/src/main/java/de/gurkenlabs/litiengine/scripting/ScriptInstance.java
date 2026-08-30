package de.gurkenlabs.litiengine.scripting;

import java.awt.Graphics2D;

/// One configured runtime instance of a script.
public interface ScriptInstance {
  void attach(ScriptContext<?> context) throws Exception;

  default void update() throws Exception {}

  default void render(Graphics2D g) throws Exception {}

  void detach() throws Exception;
}


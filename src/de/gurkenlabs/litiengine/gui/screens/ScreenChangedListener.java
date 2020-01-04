package de.gurkenlabs.litiengine.gui.screens;

import java.util.EventListener;

public interface ScreenChangedListener extends EventListener {
  public void changed(ScreenChangedEvent event);
}

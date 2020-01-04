package de.gurkenlabs.litiengine.graphics;

import java.util.EventListener;

public interface MapRenderedListener extends EventListener {
  public void rendered(MapRenderedEvent event);
}

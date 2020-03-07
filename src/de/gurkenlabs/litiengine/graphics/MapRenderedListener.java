package de.gurkenlabs.litiengine.graphics;

import java.util.EventListener;

@FunctionalInterface
public interface MapRenderedListener extends EventListener {
  public void rendered(MapRenderedEvent event);
}

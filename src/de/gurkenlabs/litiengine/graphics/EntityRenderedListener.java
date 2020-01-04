package de.gurkenlabs.litiengine.graphics;

import java.util.EventListener;

public interface EntityRenderedListener extends EventListener {
  public void rendered(EntityRenderEvent event);
}

package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

public interface TriggerDeactivatedListener extends EventListener {
  public void deactivated(TriggerEvent event);
}

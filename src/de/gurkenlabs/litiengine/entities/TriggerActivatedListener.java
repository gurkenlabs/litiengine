package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

public interface TriggerActivatedListener extends EventListener {
  public void activated(TriggerEvent event);
}

package de.gurkenlabs.litiengine.gui.screens;

import java.util.EventListener;

/**
 * Listener interface for receiving screen changed events.
 * The class that is interested in processing a screen changed event
 * implements this interface, and the object created with that class
 * is registered with a component using the component's
 * <code>addScreenChangedListener</code> method. When the screen changed
 * event occurs, that object's <code>changed</code> method is invoked.
 */
public interface ScreenChangedListener extends EventListener {
  /**
   * Invoked when the screen has changed.
   *
   * @param event the event that contains information about the screen change
   */
  void changed(ScreenChangedEvent event);
}

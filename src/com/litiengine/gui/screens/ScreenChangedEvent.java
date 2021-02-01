package com.litiengine.gui.screens;

import java.util.EventObject;

public class ScreenChangedEvent extends EventObject {
  private static final long serialVersionUID = 6145911214616836674L;
  private final transient Screen previous;
  private final transient Screen changed;

  public ScreenChangedEvent(Screen changed, Screen previous) {
    super(changed);
    this.previous = previous;
    this.changed = changed;
  }

  public Screen getPrevious() {
    return this.previous;
  }

  public Screen getChanged() {
    return this.changed;
  }
}

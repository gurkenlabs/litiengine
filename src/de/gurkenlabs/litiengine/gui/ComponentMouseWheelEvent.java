package de.gurkenlabs.litiengine.gui;

import java.awt.event.MouseWheelEvent;

public class ComponentMouseWheelEvent {

  private final MouseWheelEvent event;

  /** The sender. */
  private final GuiComponent sender;

  /**
   * Instantiates a new component mouse event.
   *
   * @param event the event
   * @param sender the sender
   */
  public ComponentMouseWheelEvent(final MouseWheelEvent event, final GuiComponent sender) {
    this.event = event;
    this.sender = sender;
  }

  /**
   * Gets the event.
   *
   * @return the event
   */
  public MouseWheelEvent getEvent() {
    return this.event;
  }

  /**
   * Gets the sender.
   *
   * @return the sender
   */
  public GuiComponent getSender() {
    return this.sender;
  }
}

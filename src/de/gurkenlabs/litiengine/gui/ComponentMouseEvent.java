/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.event.MouseEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class ComponentMouseEvent.
 */
public class ComponentMouseEvent {

  /** The event. */
  private final MouseEvent event;

  /** The sender. */
  private final GuiComponent sender;

  /**
   * Instantiates a new component mouse event.
   *
   * @param event
   *          the event
   * @param sender
   *          the sender
   */
  public ComponentMouseEvent(final MouseEvent event, final GuiComponent sender) {
    this.event = event;
    this.sender = sender;
  }

  /**
   * Gets the event.
   *
   * @return the event
   */
  public MouseEvent getEvent() {
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

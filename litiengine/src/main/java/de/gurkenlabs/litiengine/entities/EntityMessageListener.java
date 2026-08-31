package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/// This listener provides callbacks for when an `Entity` received a message.
@FunctionalInterface
public interface EntityMessageListener extends EventListener {

  /// This method is called whenever a message is received by [String)][IEntity#sendMessage(Object,].
  ///
  /// @param event
  /// The event data that contains information about the received message and sender.
  void messageReceived(EntityMessageEvent event);
}

package de.gurkenlabs.litiengine.net.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MessageHandlerProvider implements IMessageHandlerProvider {

  private final HashMap<MessageType, List<IMessageHandler>> messageHandlers;

  public MessageHandlerProvider() {
    this.messageHandlers = new HashMap<>();
    this.initializeHandlers();
  }

  @Override
  public List<IMessageHandler> getMessageHanders(final MessageType type) {
    if (this.getMessageHandlers().containsKey(type)) {
      return this.getMessageHandlers().get(type);
    }

    return new ArrayList<>();
  }

  @Override
  public void register(final MessageType messageType, final IMessageHandler messageHandler) {
    if (!this.getMessageHandlers().containsKey(messageType)) {
      this.getMessageHandlers().put(messageType, new ArrayList<IMessageHandler>());
    }

    if (this.getMessageHandlers().get(messageType).contains(messageHandler)) {
      return;
    }

    this.getMessageHandlers().get(messageType).add(messageHandler);
  }

  /**
   * Gets the message handlers.
   *
   * @return the message handlers
   */
  protected HashMap<MessageType, List<IMessageHandler>> getMessageHandlers() {
    return this.messageHandlers;
  }

  protected abstract void initializeHandlers();
}
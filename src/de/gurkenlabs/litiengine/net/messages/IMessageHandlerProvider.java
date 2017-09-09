package de.gurkenlabs.litiengine.net.messages;

import java.util.List;

/**
 * The Interface IMessageHandlerProvider.
 */
public interface IMessageHandlerProvider {

  /**
   * Gets the message hander.
   *
   * @param type
   *          the type
   * @return the message hander
   */
  public List<IMessageHandler> getMessageHanders(MessageType type);

  public void register(MessageType messageType, IMessageHandler messageHandler);
}

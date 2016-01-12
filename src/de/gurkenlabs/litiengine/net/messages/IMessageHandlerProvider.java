/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.util.List;

// TODO: Auto-generated Javadoc
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

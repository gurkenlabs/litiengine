/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.io.Serializable;
import java.net.InetAddress;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageHandler.
 *
 * @param <T>
 *          the generic type
 */
public abstract class MessageHandler<T extends Serializable> implements IMessageHandler {

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.messages.IMessageHandler#handle(byte[],
   * java.net.InetAddress, int)
   */
  @Override
  public void handle(final byte[] data, final InetAddress address, final int port) {
    final T message = new MessagePackage<T>(data).getObject();
    if (message == null) {
      return;
    }

    this.handle(message, address, port);
  }

  /**
   * Handle.
   *
   * @param message
   *          the message
   * @param address
   *          the address
   * @param port
   *          the port
   */
  protected abstract void handle(T message, InetAddress address, int port);
}

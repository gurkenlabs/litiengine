/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages.handlers;

import java.io.Serializable;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.net.messages.IMessageHandler;
import de.gurkenlabs.litiengine.net.messages.MessagePackage;

/**
 * The Class MessageHandler is an abstract implementation for all existing
 * message handlers that handle a certain type of messages.
 *
 * @param <T>
 *          the type of the message that will be validated and handled.
 */
public abstract class MessageHandler<T extends Serializable> implements IMessageHandler {
  private static final Logger log = Logger.getLogger(MessageHandler.class.getName());

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

    if (!this.validate(message)) {
      log.info(MessageFormat.format("Data from {0}:{1} is not valid for the messagehandler {2}.", address.getHostAddress(), port, this.getClass().getSimpleName()));
      return;
    }

    this.handle(message, address, port);
  }

  /**
   * This method can be overwritten by concrete implementations to provide a
   * validation mechanism for the messages sent. E.g. one could test if a
   * certain field of the message is not null.
   *
   * @param message
   * @return True if the message is valid for the message handler; otherwise
   *         false.
   */
  protected boolean validate(final T message) {
    return true;
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

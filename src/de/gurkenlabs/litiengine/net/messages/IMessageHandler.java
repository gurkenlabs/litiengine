/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.net.InetAddress;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMessageHandler.
 */
public interface IMessageHandler {

  /**
   * Handle.
   *
   * @param data
   *          the data
   * @param address
   *          the address
   * @param port
   *          the port
   */
  public void handle(byte[] data, InetAddress address, int port);
}

package de.gurkenlabs.litiengine.net.messages;

import java.net.InetAddress;

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

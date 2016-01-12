/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface IClientConnectionManager.
 */
public interface IClientConnectionManager extends List<ClientConnection> {

  /**
   * Gets the.
   *
   * @param clientId
   *          the client id
   * @return the client connection
   */
  @Override
  public ClientConnection get(int clientId);

  /**
   * Checks if is connected.
   *
   * @param connectionId
   *          the connection id
   * @param address
   *          the address
   * @param port
   *          the port
   * @return true, if is connected
   */
  public boolean isConnected(int connectionId, InetAddress address, int port);

  public void setSignOfLife(final int clientId);
}

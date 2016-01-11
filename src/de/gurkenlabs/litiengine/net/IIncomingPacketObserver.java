/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net;

import java.net.InetAddress;

/**
 * An asynchronous update interface for receiving notifications about
 * IIncomingPacket information as the IIncomingPacket is constructed.
 */
public interface IIncomingPacketObserver {

  /**
   * This method is called when information about an IIncomingPacket which was
   * previously requested using an asynchronous interface becomes available.
   *
   * @param data
   *          the data
   * @param address
   *          the address
   * @param port
   *          the port
   */
  public void packetReceived(byte[] data, InetAddress address, int port);
}

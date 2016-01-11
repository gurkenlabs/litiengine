/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net;

import java.net.InetAddress;

// TODO: Auto-generated Javadoc
/**
 * The Interface IPacketSender.
 */
public interface IPacketSender {

  /**
   * Send data.
   *
   * @param packet
   *          the packet
   * @param ipAddress
   *          the ip address
   * @param port
   *          the port
   */
  public void sendData(Package packet, InetAddress ipAddress, int port);

  /**
   * Send data.
   *
   * @param packet
   *          the packet
   * @param ipAddress
   *          the ip address
   * @param port
   *          the port
   */
  public void sendData(Package packet, String ipAddress, int port);
}

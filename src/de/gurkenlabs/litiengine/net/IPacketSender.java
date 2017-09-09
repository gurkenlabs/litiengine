package de.gurkenlabs.litiengine.net;

import java.net.InetAddress;

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

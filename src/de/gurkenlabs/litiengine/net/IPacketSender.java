package de.gurkenlabs.litiengine.net;

import java.net.InetAddress;

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
  public void sendData(Packet packet, InetAddress ipAddress, int port);

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
  public void sendData(Packet packet, String ipAddress, int port);
}

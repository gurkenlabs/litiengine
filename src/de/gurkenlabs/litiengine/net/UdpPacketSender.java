package de.gurkenlabs.litiengine.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class UdpPacketSender.
 */
public class UdpPacketSender implements IPacketSender {
  private static final Logger log = Logger.getLogger(UdpPacketSender.class.getName());

  /** The socket. */
  private DatagramSocket socket;

  /**
   * Instantiates a new udp packet sender.
   */
  public UdpPacketSender() {
    try {
      this.socket = new DatagramSocket();
    } catch (final SocketException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Gets the sender socket.
   *
   * @return the sender socket
   */
  public DatagramSocket getSenderSocket() {
    return this.socket;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.IPacketSender#sendData(de.gurkenlabs.liti.net.
   * Package, java.net.InetAddress, int)
   */
  @Override
  public void sendData(final Package packet, final InetAddress ipAddress, final int port) {
    final DatagramPacket datagramPacket = new DatagramPacket(packet.getData(), packet.getData().length, ipAddress, port);
    try {
      this.socket.send(datagramPacket);
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.IPacketSender#sendData(de.gurkenlabs.liti.net.
   * Package, java.lang.String, int)
   */
  @Override
  public void sendData(final Package packet, final String ipAddress, final int port) {
    try {
      this.sendData(packet, InetAddress.getByName(ipAddress), port);
    } catch (final UnknownHostException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}

package de.gurkenlabs.litiengine.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class UdpPacketReceiver.
 */
public class UdpPacketReceiver extends Thread implements IPacketReceiver {
  private static final Logger log = Logger.getLogger(UdpPacketReceiver.class.getName());

  /** The incoming packet observers. */
  private final ArrayList<IIncomingPacketObserver> incomingPacketObservers;

  /** The is terminated. */
  private boolean isTerminated;

  /** The socket. */
  private DatagramSocket socket;

  /**
   * Instantiates a new udp packet receiver.
   *
   * @param socket
   *          the socket
   */
  public UdpPacketReceiver(final DatagramSocket socket) {
    this.incomingPacketObservers = new ArrayList<>();
    this.socket = socket;
  }

  /**
   * Instantiates a new udp packet receiver.
   *
   * @param port
   *          the port
   */
  public UdpPacketReceiver(final int port) {
    this.incomingPacketObservers = new ArrayList<>();
    try {
      this.socket = new DatagramSocket(port);
    } catch (final SocketException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void registerForIncomingPackets(final IIncomingPacketObserver observer) {
    this.incomingPacketObservers.add(observer);
  }

  @Override
  public void run() {
    while (!this.isTerminated) {
      final byte[] data = new byte[10000];
      final DatagramPacket packet = new DatagramPacket(data, data.length);
      try {
        this.socket.receive(packet);
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }

      for (final IIncomingPacketObserver packetObserver : this.incomingPacketObservers) {
        packetObserver.packetReceived(packet.getData(), packet.getAddress(), packet.getPort());
      }
    }

    this.socket.close();
  }

  @Override
  public void terminate() {
    this.isTerminated = true;
  }
}

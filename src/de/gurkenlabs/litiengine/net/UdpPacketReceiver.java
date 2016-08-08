/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class UdpPacketReceiver.
 */
public class UdpPacketReceiver extends Thread implements IPacketReceiver {

  /** The is terminated. */
  private boolean isTerminated;

  /** The socket. */
  private DatagramSocket socket;

  /** The incoming packet observers. */
  ArrayList<IIncomingPacketObserver> incomingPacketObservers;

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
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.IPacketReceiver#registerForIncomingPackets(de.
   * gurkenlabs.liti.net.IIncomingPacketObserver)
   */
  @Override
  public void registerForIncomingPackets(final IIncomingPacketObserver observer) {
    this.incomingPacketObservers.add(observer);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    while (!this.isTerminated) {
      final byte[] data = new byte[10000];
      final DatagramPacket packet = new DatagramPacket(data, data.length);
      try {
        this.socket.receive(packet);
      } catch (final IOException e) {
        e.printStackTrace();
      }

      for (final IIncomingPacketObserver packetObserver : this.incomingPacketObservers) {
        packetObserver.packetReceived(packet.getData(), packet.getAddress(), packet.getPort());
      }
    }

    this.socket.close();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.IPacketReceiver#terminate()
   */
  @Override
  public void terminate() {
    this.isTerminated = true;
  }
}

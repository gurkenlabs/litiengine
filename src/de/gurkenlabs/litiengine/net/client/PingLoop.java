/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.net.IPacketSender;
import de.gurkenlabs.litiengine.net.messages.ClientMessage;
import de.gurkenlabs.litiengine.net.messages.IMessageHandlerProvider;
import de.gurkenlabs.litiengine.net.messages.MessagePackage;
import de.gurkenlabs.litiengine.net.messages.MessageType;
import de.gurkenlabs.litiengine.net.messages.PingResponseMessage;
import de.gurkenlabs.litiengine.net.messages.handlers.ClientMessageHandler;

/**
 * The Class PingThread.
 */
public class PingLoop extends ClientMessageHandler<PingResponseMessage> implements IPingLoop {
  private final List<Consumer<Long>> pingRecordConsumer;
  private final int clientId;
  private final IPacketSender sender;

  private final int port;

  private final String serverIpAdress;

  private PingThread pingLoop;

  public PingLoop(final int clientId, final IMessageHandlerProvider provider, final IPacketSender sender, final String serverIpAdress, final int port) {
    this.pingRecordConsumer = new ArrayList<>();
    this.clientId = clientId;
    this.sender = sender;
    this.serverIpAdress = serverIpAdress;
    this.port = port;
    provider.register(MessageType.PING, this);
  }

  @Override
  public void onPingRecorded(final Consumer<Long> consumer) {
    if (this.pingRecordConsumer.contains(consumer)) {
      return;
    }

    this.pingRecordConsumer.add(consumer);
  }

  @Override
  public void start() {
    this.pingLoop = new PingThread(this.sender, this.serverIpAdress, this.port);
    this.pingLoop.start();
  }

  @Override
  public void terminate() {
    this.pingLoop.terminate();
  }

  @Override
  protected void handle(final PingResponseMessage message, final InetAddress address, final int port) {
    if (this.pingLoop == null) {
      return;
    }

    try {
      if (address.getHostAddress().equals(InetAddress.getByName(this.serverIpAdress).getHostAddress())) {
        this.pingLoop.pingAnswerReceived();
      }
    } catch (final UnknownHostException e) {
      e.printStackTrace();
    }
  }

  private class PingThread extends Thread implements ILaunchable {
    /** The Constant TimeBetweenPings. */
    private final static int TimeBetweenPings = 1000;

    private boolean pingRecorded;

    /** The is terminated. */
    private boolean isTerminated;

    /** The ping. */
    private long ping;

    private final IPacketSender sender;

    private long lastPing;

    private PingThread(final IPacketSender sender, final String serverIpAdress, final int port) {
      this.sender = sender;
      this.pingRecorded = true;
    }

    public void pingAnswerReceived() {
      this.pingRecorded = true;

      final long after = System.currentTimeMillis();
      this.ping = after - this.lastPing;

      for (final Consumer<Long> consumer : PingLoop.this.pingRecordConsumer) {
        consumer.accept(this.ping);
      }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      while (!this.isTerminated) {
        this.pingRecorded = false;
        this.lastPing = System.currentTimeMillis();
        final MessagePackage<ClientMessage> packet = new MessagePackage<ClientMessage>(MessageType.PING, new ClientMessage(PingLoop.this.clientId));
        this.sender.sendData(packet, PingLoop.this.serverIpAdress, PingLoop.this.port);

        try {
          Thread.sleep(TimeBetweenPings);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * Terminate.
     */
    @Override
    public void terminate() {
      this.isTerminated = true;
    }
  }
}
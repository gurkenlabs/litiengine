package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.net.IPacketReceiver;
import de.gurkenlabs.litiengine.net.IPacketSender;
import de.gurkenlabs.litiengine.net.UdpPacketReceiver;
import de.gurkenlabs.litiengine.net.UdpPacketSender;
import de.gurkenlabs.litiengine.net.messages.IMessageHandler;
import de.gurkenlabs.litiengine.net.messages.IMessageHandlerProvider;
import de.gurkenlabs.litiengine.net.messages.MessageType;
import de.gurkenlabs.util.CommandManager;
import de.gurkenlabs.util.ICommandManager;
import de.gurkenlabs.util.io.CompressionUtilities;

public class UdpServer implements IServer {
  private static final String SHUTDOWN = "shutdown";

  private static final Logger log = Logger.getLogger(UdpServer.class.getName());

  /** The client connection manager. */
  private final IClientConnectionManager clientConnectionManager;

  private final ICommandManager commandManager;

  /** The message handler provider. */
  private final IMessageHandlerProvider messageHandlerProvider;

  /** The receiver. */
  private final IPacketReceiver receiver;

  /** The sender. */
  private final IPacketSender sender;

  public UdpServer(final int listenPort, final IMessageHandlerProvider provider) {
    this.receiver = new UdpPacketReceiver(listenPort);
    this.receiver.registerForIncomingPackets(this);
    this.sender = new UdpPacketSender();
    this.messageHandlerProvider = provider;
    this.commandManager = new CommandManager();
    this.commandManager.bind(SHUTDOWN, this::handleShutdownCommand);

    this.clientConnectionManager = new ClientConnectionManager();
    provider.register(MessageType.PING, new ClientConnectionPingMessageHandler(this.clientConnectionManager));
  }

  @Override
  public ICommandManager getCommandManager() {
    return this.commandManager;
  }

  @Override
  public IClientConnectionManager getConnectionManager() {
    return this.clientConnectionManager;
  }

  @Override
  public IPacketSender getSender() {
    return this.sender;
  }

  @Override
  public void packetReceived(final byte[] data, final InetAddress address, final int port) {
    if (data.length == 0) {
      return;
    }

    final byte[] decompressedData = CompressionUtilities.decompress(data);
    final MessageType type = MessageType.get(decompressedData[0]);
    final List<IMessageHandler> messageHandlers = this.messageHandlerProvider.getMessageHanders(type);
    if (messageHandlers == null || messageHandlers.isEmpty()) {
      return;
    }

    for (final IMessageHandler messageHandler : messageHandlers) {
      messageHandler.handle(decompressedData, address, port);
    }
  }

  @Override
  public void start() {
    this.receiver.start();
    this.getCommandManager().start();
  }

  @Override
  public void terminate() {
    this.receiver.terminate();
    this.getCommandManager().terminate();
    System.exit(-1);
  }

  protected boolean handleShutdownCommand(final String[] command) {
    log.log(Level.INFO, "Shutting down server...");
    this.terminate();
    return true;
  }
}

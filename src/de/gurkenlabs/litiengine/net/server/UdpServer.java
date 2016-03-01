package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;
import java.util.List;

import de.gurkenlabs.litiengine.net.IPacketReceiver;
import de.gurkenlabs.litiengine.net.IPacketSender;
import de.gurkenlabs.litiengine.net.UdpPacketReceiver;
import de.gurkenlabs.litiengine.net.UdpPacketSender;
import de.gurkenlabs.litiengine.net.messages.IMessageHandler;
import de.gurkenlabs.litiengine.net.messages.IMessageHandlerProvider;
import de.gurkenlabs.litiengine.net.messages.MessageType;
import de.gurkenlabs.util.console.CommandManager;
import de.gurkenlabs.util.console.ICommandManager;
import de.gurkenlabs.util.zip.CompressionUtilities;

public class UdpServer implements IServer {
  private static final String SHUTDOWN = "shutdown";
  /** The client connection manager. */
  private final IClientConnectionManager clientConnectionManager;

  /** The message handler provider. */
  private final IMessageHandlerProvider messageHandlerProvider;

  /** The receiver. */
  private final IPacketReceiver receiver;

  /** The sender. */
  private final IPacketSender sender;

  private final ICommandManager commandManager;

  public UdpServer(final int listenPort, final IMessageHandlerProvider provider, int updateRate) {
    this.receiver = new UdpPacketReceiver(listenPort, updateRate);
    this.receiver.registerForIncomingPackets(this);
    this.sender = new UdpPacketSender();
    this.messageHandlerProvider = provider;
    this.commandManager = new CommandManager();
    this.commandManager.bind(SHUTDOWN, (args) -> this.handleShutdownCommand(args));

    this.clientConnectionManager = new ClientConnectionManager();
    provider.register(MessageType.PING, new ClientConnectionPingMessageHandler(this.clientConnectionManager));
  }

  protected boolean handleShutdownCommand(final String[] command) {
    System.out.println("Shutting down server...");
    this.terminate();
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.net.server.IGameServer#getConnectionManager()
   */
  @Override
  public IClientConnectionManager getConnectionManager() {
    return this.clientConnectionManager;
  }

  @Override
  public IPacketSender getSender() {
    return this.sender;
  }

  @Override
  public ICommandManager getCommandManager() {
    return this.commandManager;
  }

  @Override
  public void packetReceived(final byte[] data, final InetAddress address, final int port) {
    if (data.length == 0) {
      return;
    }

    final byte[] decompressedData = CompressionUtilities.decompress(data);
    final MessageType type = MessageType.get(decompressedData[0]);
    final List<IMessageHandler> messageHandlers = this.messageHandlerProvider.getMessageHanders(type);
    if (messageHandlers == null || messageHandlers.size() == 0) {
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
}

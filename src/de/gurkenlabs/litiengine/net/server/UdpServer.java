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
import de.gurkenlabs.util.console.ConsoleCommandListener;
import de.gurkenlabs.util.zip.CompressionUtilities;

public abstract class UdpServer implements IServer {

  /** The client connection manager. */
  private final IClientConnectionManager clientConnectionManager;

  private final ConsoleCommandListener commandLineListener;

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
    this.commandLineListener = new ConsoleCommandListener(this);
    this.clientConnectionManager = new ClientConnectionManager();
    provider.register(MessageType.PING, new ClientConnectionPingMessageHandler(this.clientConnectionManager));
  }

  @Override
  public boolean executeCommand(final String command) {
    final String SHUTDOWN = "shutdown";
    if (command == null || command.isEmpty()) {
      return false;
    }

    if (command.equalsIgnoreCase(SHUTDOWN)) {
      System.out.println("Shutting down server...");
      this.terminate();
    }

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
    this.commandLineListener.start();
  }

  @Override
  public void terminate() {
    this.receiver.terminate();
    this.commandLineListener.terminate();
    System.exit(-1);
  }

}

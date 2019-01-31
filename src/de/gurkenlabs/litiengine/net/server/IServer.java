package de.gurkenlabs.litiengine.net.server;

import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.net.IIncomingPacketObserver;
import de.gurkenlabs.litiengine.net.IPacketSender;
import de.gurkenlabs.litiengine.util.CommandManager;

public interface IServer extends IIncomingPacketObserver, ILaunchable {

  public CommandManager getCommandManager();

  /**
   * Gets the connection manager.
   *
   * @return the connection manager
   */
  public IClientConnectionManager getConnectionManager();

  /**
   * Gets the sender.
   *
   * @return the sender
   */
  public IPacketSender getSender();
}

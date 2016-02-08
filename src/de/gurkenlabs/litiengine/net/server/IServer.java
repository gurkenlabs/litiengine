package de.gurkenlabs.litiengine.net.server;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.net.IIncomingPacketObserver;
import de.gurkenlabs.litiengine.net.IPacketSender;
import de.gurkenlabs.util.console.ICommandManager;

public interface IServer extends IIncomingPacketObserver, ILaunchable {

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
  
  public ICommandManager getCommandManager();
}

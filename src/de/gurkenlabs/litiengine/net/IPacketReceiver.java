package de.gurkenlabs.litiengine.net;

import de.gurkenlabs.litiengine.ILaunchable;

/**
 * The Interface IPacketReceiver.
 */
public interface IPacketReceiver extends ILaunchable {

  /**
   * Register for incoming packets.
   *
   * @param observer
   *          the observer
   */
  public void registerForIncomingPackets(IIncomingPacketObserver observer);
}

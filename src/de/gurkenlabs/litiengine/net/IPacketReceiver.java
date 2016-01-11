/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net;

import de.gurkenlabs.core.ILaunchable;

// TODO: Auto-generated Javadoc
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

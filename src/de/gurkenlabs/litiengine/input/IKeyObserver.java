/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications about IKey
 * information as the IKey is constructed.
 */
public interface IKeyObserver {

  /**
   * This method is called when information about an IKey which was previously
   * requested using an asynchronous interface becomes available.
   *
   * @param keyCode
   *          the key code
   */
  public abstract void handlePressedKey(KeyEvent keyCode);

  /**
   * This method is called when information about an IKey which was previously
   * requested using an asynchronous interface becomes available.
   *
   * @param keyCode
   *          the key code
   */
  public abstract void handleReleasedKey(KeyEvent keyCode);

  /**
   * This method is called when information about an IKey which was previously
   * requested using an asynchronous interface becomes available.
   *
   * @param keyCode
   *          the key code
   */
  public abstract void handleTypedKey(KeyEvent keyCode);
}

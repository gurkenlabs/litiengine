/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

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
  public abstract void handlePressedKey(int keyCode);

  /**
   * This method is called when information about an IKey which was previously
   * requested using an asynchronous interface becomes available.
   *
   * @param keyCode
   *          the key code
   */
  public abstract void handleReleasedKey(int keyCode);

  /**
   * This method is called when information about an IKey which was previously
   * requested using an asynchronous interface becomes available.
   *
   * @param keyCode
   *          the key code
   */
  public abstract void handleTypedKey(int keyCode);
}

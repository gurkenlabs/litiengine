/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.core.IUpdateable;

/**
 * The Interface IKeyboard.
 */
public interface IKeyboard extends IUpdateable {

  /**
   * Register for key down events.
   *
   * @param observer
   *          the observer
   */
  public void registerForKeyDownEvents(IKeyObserver observer);

  /**
   * Unregister from key down events.
   *
   * @param observer
   *          the observer
   */
  public void unregisterFromKeyDownEvents(IKeyObserver observer);
}

/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IUpdateable;

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

  public void onKeyTyped(int keyCode, Consumer<Integer> consumer);

  public void onKeyReleased(int keyCode, Consumer<Integer> consumer);

  public void onKeyPressed(int keyCode, Consumer<Integer> consumer);

  /**
   * Unregister from key down events.
   *
   * @param observer
   *          the observer
   */
  public void unregisterFromKeyDownEvents(IKeyObserver observer);
}

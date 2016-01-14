/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui.screens;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications about
 * IResolution information as the IResolution is constructed.
 */
public interface IResolutionObserver {

  /**
   * This method is called when information about an IResolution which was
   * previously requested using an asynchronous interface becomes available.
   */
  public void resolutionChanged();
}

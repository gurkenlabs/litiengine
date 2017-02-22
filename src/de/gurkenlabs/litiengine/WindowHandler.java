package de.gurkenlabs.litiengine;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * The Class WindowHandler.
 */
public class WindowHandler implements WindowListener {

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
   */
  @Override
  public void windowActivated(final WindowEvent event) {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosed(final WindowEvent event) {
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosing(final WindowEvent event) {
    Game.terminate();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.
   * WindowEvent)
   */
  @Override
  public void windowDeactivated(final WindowEvent event) {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.
   * WindowEvent)
   */
  @Override
  public void windowDeiconified(final WindowEvent event) {
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
   */
  @Override
  public void windowIconified(final WindowEvent event) {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
   */
  @Override
  public void windowOpened(final WindowEvent event) {
  }
}
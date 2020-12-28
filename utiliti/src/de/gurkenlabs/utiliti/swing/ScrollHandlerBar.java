package de.gurkenlabs.utiliti.swing;

import de.gurkenlabs.utiliti.handlers.Scroll;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScrollHandlerBar extends JScrollBar implements Scroll.ScrollHandler {
  private final List<Scroll.ScrollHandlerEventListener> listeners;

  public ScrollHandlerBar(int orientation) {
    super(orientation);

    this.listeners = new CopyOnWriteArrayList<>();

    this.setDoubleBuffered(true);
    this.addAdjustmentListener(e -> {
      for (Scroll.ScrollHandlerEventListener listener : listeners) {
        listener.scrolled(this);
      }
    });
  }

  @Override
  public void onScrolled(Scroll.ScrollHandlerEventListener listener) {
    this.listeners.add(listener);
  }
}

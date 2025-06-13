package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.Scroll;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JScrollBar;

public class ScrollHandlerBar extends JScrollBar implements Scroll.ScrollHandler {
  private final transient List<Scroll.ScrollHandlerEventListener> listeners;

  public ScrollHandlerBar(int orientation) {
    super(orientation);

    this.listeners = new CopyOnWriteArrayList<>();

    this.setDoubleBuffered(true);
    this.addAdjustmentListener(
        e -> {
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

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.Scroll;
import de.gurkenlabs.utiliti.controller.Editor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JScrollBar;
import java.awt.Dimension;

public class ScrollHandlerBar extends JScrollBar implements Scroll.ScrollHandler {
  private final transient List<Scroll.ScrollHandlerEventListener> listeners;

  public ScrollHandlerBar(int orientation) {
    super(orientation);

    this.listeners = new CopyOnWriteArrayList<>();
    int thickness = Math.max(9, Math.round(9 * Editor.preferences().getUiScale()));
    if (orientation == JScrollBar.HORIZONTAL) {
      setPreferredSize(new Dimension(0, thickness));
    } else {
      setPreferredSize(new Dimension(thickness, 0));
    }
    setUnitIncrement(20_000);
    setBlockIncrement(100_000);
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

package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class AssetPanelItemTest {

  @Test
  void leftMouseDragStartsOneCopyTransfer() {
    AssetPanelItem item = new AssetPanelItem(new Object());
    AtomicInteger exports = new AtomicInteger();
    AtomicInteger action = new AtomicInteger();
    item.setTransferHandler(new TransferHandler() {
      @Override public void exportAsDrag(JComponent component, InputEvent event, int requestedAction) {
        exports.incrementAndGet();
        action.set(requestedAction);
      }
    });

    MouseEvent press = new MouseEvent(item, MouseEvent.MOUSE_PRESSED, 0,
      InputEvent.BUTTON1_DOWN_MASK, 4, 4, 1, false, MouseEvent.BUTTON1);
    for (var listener : item.getMouseListeners()) {
      listener.mousePressed(press);
    }
    MouseEvent drag = new MouseEvent(item, MouseEvent.MOUSE_DRAGGED, 1,
      InputEvent.BUTTON1_DOWN_MASK, 8, 8, 0, false, MouseEvent.NOBUTTON);
    for (var listener : item.getMouseMotionListeners()) {
      listener.mouseDragged(drag);
      listener.mouseDragged(drag);
    }

    assertEquals(1, exports.get());
    assertEquals(TransferHandler.COPY, action.get());
  }
}

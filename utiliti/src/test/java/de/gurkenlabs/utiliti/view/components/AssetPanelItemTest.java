package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
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

  @Test
  void clipboardExportKeepsExternalFilePayloadAvailable() throws Exception {
    SpritesheetResource resource = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "clipboard-item", 1, 1);
    AssetPanelItem item = new AssetPanelItem(resource);
    Clipboard clipboard = new Clipboard("asset-test");

    item.getTransferHandler().exportToClipboard(item, clipboard, TransferHandler.COPY);
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) clipboard.getData(DataFlavor.javaFileListFlavor);

    assertEquals(1, files.size());
    org.junit.jupiter.api.Assertions.assertTrue(files.getFirst().isFile());
  }

  @Test
  void overlappingTransferCompletionClosesTheCompletedData() {
    AssetTransferable first = mock(AssetTransferable.class);
    AssetTransferable second = mock(AssetTransferable.class);

    AssetPanelItem.closeTransfer(first);

    verify(first).close();
    verify(second, never()).close();
  }
}

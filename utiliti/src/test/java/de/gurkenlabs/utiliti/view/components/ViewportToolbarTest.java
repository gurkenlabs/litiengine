package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ViewportToolbarTest {

  @Test
  void largeHistoryPopupIsScrollableAndRetainsEveryEntry() {
    List<UndoManager.HistoryEntry> history = history(44);
    AtomicInteger appliedOperations = new AtomicInteger();

    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(history, true, appliedOperations::set);

    JScrollPane scroll = assertInstanceOf(JScrollPane.class, popup.getComponent(0));
    JList<?> list = assertInstanceOf(JList.class, scroll.getViewport().getView());
    assertEquals(44, list.getModel().getSize());
    assertEquals(ViewportToolbar.MAX_HISTORY_VISIBLE_ROWS, list.getVisibleRowCount());
    assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, scroll.getVerticalScrollBarPolicy());

    list.setSelectedIndex(24);
    list.getActionMap()
        .get("applyHistory")
        .actionPerformed(new ActionEvent(list, ActionEvent.ACTION_PERFORMED, "applyHistory"));
    assertEquals(25, appliedOperations.get());
  }

  @Test
  void shortHistoryPopupUsesOnlyTheRowsItNeeds() {
    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(history(3), false, ignored -> {});

    JScrollPane scroll = assertInstanceOf(JScrollPane.class, popup.getComponent(0));
    JList<?> list = assertInstanceOf(JList.class, scroll.getViewport().getView());
    assertEquals(3, list.getVisibleRowCount());
  }

  @Test
  void emptyHistoryPopupKeepsDisabledMessage() {
    JPopupMenu popup =
        ViewportToolbar.createHistoryPopup(List.of(), true, ignored -> {});

    JMenuItem message = assertInstanceOf(JMenuItem.class, popup.getComponent(0));
    assertFalse(message.isEnabled());
  }

  private static List<UndoManager.HistoryEntry> history(int size) {
    List<UndoManager.HistoryEntry> history = new ArrayList<>(size);
    for (int index = 1; index <= size; index++) {
      history.add(new UndoManager.HistoryEntry("Change object " + index, 1));
    }
    return history;
  }
}

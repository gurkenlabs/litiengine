package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.util.List;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class MapListTest {

  @Test
  void bindEmptyMapsClearsListSelection() {
    assertTrue(SwingUtilities.isEventDispatchThread());

    MapList mapList = new MapList();

    mapList.bind(List.of(), true);

    JList<?> list = (JList<?>) mapList.getViewport().getView();
    assertEquals(0, list.getModel().getSize());
    assertTrue(list.isSelectionEmpty());
  }
}

package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class MapListCellRendererTest {

  @Test
  void rendererHandlesNullMapValue() {
    MapListCellRenderer renderer = new MapListCellRenderer();

    JLabel label = (JLabel) renderer.getListCellRendererComponent(new JList<IMap>(), null, -1, false, false);

    assertEquals("", label.getText());
  }
}

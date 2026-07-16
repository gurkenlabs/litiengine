package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class AssetPanelTest {
  @Test
  void controlClickCreatesOrderedPersistentSelection() {
    AssetPanel panel = panelWithTilesets("a", "b", "c");

    panel.selectItemForTest(0, false, false);
    panel.selectItemForTest(2, true, false);

    assertEquals(List.of("a", "c"), panel.getSelectedItems().stream()
        .map(AssetPanelItem::getName).toList());
    assertTrue(panel.getItemsForTest().get(0).isSelected());
    assertTrue(panel.getItemsForTest().get(2).isSelected());
    assertTrue(panel.getItemsForTest().get(2).isFocused());
  }

  @Test
  void shiftClickSelectsVisibleRange() {
    AssetPanel panel = panelWithTilesets("a", "b", "c");

    panel.selectItemForTest(0, false, false);
    panel.selectItemForTest(2, false, true);

    assertEquals(List.of("a", "b", "c"), panel.getSelectedItems().stream()
        .map(AssetPanelItem::getName).toList());
  }

  private static AssetPanel panelWithTilesets(String... names) {
    List<Tileset> tilesets = new ArrayList<>();
    for (String name : names) {
      Tileset tileset = new Tileset();
      tileset.setName(name);
      tilesets.add(tileset);
    }
    AssetPanel panel = new AssetPanel();
    panel.loadTilesets(tilesets);
    return panel;
  }
}

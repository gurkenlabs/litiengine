package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

@ExtendWith(SwingTestSuite.class)
class AssetPanelTest {
  @Test
  void menuShortcutClickCreatesOrderedPersistentSelection() {
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

  @Test
  void multiselectDisablesFocusedOnlyActions() {
    AssetPanel panel = panelWithTilesets("a", "b");

    panel.selectItemForTest(0, false, false);
    panel.selectItemForTest(1, true, false);

    assertFalse(panel.getItemsForTest().get(0).isIndividualActionsEnabled());
    assertFalse(panel.getItemsForTest().get(1).isIndividualActionsEnabled());
  }

  @Test
  void deselectingFocusedItemTransfersRemainingSelection() throws Exception {
    AssetPanel panel = panelWithTilesets("a", "b");
    panel.selectItemForTest(0, false, false);
    panel.selectItemForTest(1, true, false);
    AssetPanelItem deselected = panel.getItemsForTest().get(1);

    panel.selectItemForTest(1, true, false);
    Transferable transfer = deselected.createTransferableForTest();

    assertEquals(List.of(panel.getItemsForTest().get(0).getOrigin()),
        transfer.getTransferData(AssetTransferable.ASSET_FLAVOR));
  }

  @Test
  void filteringIsNullSafe() {
    Animation animation = mock(Animation.class);
    when(animation.getName()).thenReturn(null);
    when(animation.getSpritesheet()).thenReturn(null);
    AssetPanel panel = new AssetPanel();
    panel.loadAnimations(new ArrayList<>(List.of(animation)));

    assertDoesNotThrow(() -> panel.setFilterText("walk"));
    assertEquals(0, panel.getVisibleItemCount());
  }

  @Test
  @ResourceLock("default-locale")
  void filteringIsLocaleIndependent() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      AssetPanel panel = panelWithTilesets("Image Tiles");

      panel.setFilterText("image");

      assertEquals(1, panel.getVisibleItemCount());
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  void clearAssetsRemovesStaleItems() {
    AssetPanel panel = panelWithTilesets("stale");

    panel.clearAssets();

    assertEquals(0, panel.getTotalItemCount());
    assertEquals(0, panel.getVisibleItemCount());
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

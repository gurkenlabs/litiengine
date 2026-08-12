package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.image.BufferedImage;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class AssetListTest {
  @Test
  void resourceCategoriesScrollWhenBottomPanelIsShort() {
    AssetList assets = new AssetList();

    JScrollPane scroll = assertInstanceOf(
        JScrollPane.class,
        SwingUtilities.getAncestorOfClass(JScrollPane.class, assets.getAssetTree()));
    assertEquals(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        scroll.getVerticalScrollBarPolicy());
    assertEquals(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
        scroll.getHorizontalScrollBarPolicy());
  }

  @Test
  void focusNullOrNonInspectableAssetHasNoInspectorTarget() {
    assertFalse(AssetList.hasAssetInspectorTarget(null));
    assertFalse(AssetList.hasAssetInspectorTarget(new Object()));
    assertTrue(AssetList.hasAssetInspectorTarget(new Tileset()));
    assertTrue(AssetList.hasAssetInspectorTarget(
      new SpritesheetResource(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "sprite", 1, 1)));
  }
}

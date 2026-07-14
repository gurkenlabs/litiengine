package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import org.junit.jupiter.api.Test;

class AssetTreeTest {

  @Test
  void navigationSeparatesSpritesheetsAndOtherResources() {
    AssetTree tree = new AssetTree(new AssetPanel());
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

    assertEquals(2, root.getChildCount());
    assertEquals("Spritesheets", root.getChildAt(0).toString());
    assertEquals(3, root.getChildAt(0).getChildCount());
    assertEquals("Resources", root.getChildAt(1).toString());
    assertEquals(5, root.getChildAt(1).getChildCount());
  }

  @Test
  void tilesetSpriteMatchingAcceptsPathExtensionAndBaseNameVariants() {
    Set<String> tilesetKeys = Set.of("tiles/world.png", "world.png", "world");

    assertTrue(AssetTree.isTilesetSpriteName("world", tilesetKeys));
    assertTrue(AssetTree.isTilesetSpriteName("world.png", tilesetKeys));
    assertTrue(AssetTree.isTilesetSpriteName("tiles\\world.png", tilesetKeys));
  }

  @Test
  void tilesetSpriteMatchingIgnoresUnrelatedMiscSprites() {
    assertFalse(AssetTree.isTilesetSpriteName("sparkle", Set.of("world")));
  }
}

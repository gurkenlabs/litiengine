package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AssetTreeTest {

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

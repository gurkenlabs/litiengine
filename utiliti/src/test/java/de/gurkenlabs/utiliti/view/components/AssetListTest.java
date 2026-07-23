package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class AssetListTest {
  @Test
  void focusNullOrNonInspectableAssetHasNoInspectorTarget() {
    assertFalse(AssetList.hasAssetInspectorTarget(null));
    assertFalse(AssetList.hasAssetInspectorTarget(new Object()));
    assertTrue(AssetList.hasAssetInspectorTarget(new Tileset()));
    assertTrue(AssetList.hasAssetInspectorTarget(
      new SpritesheetResource(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "sprite", 1, 1)));
  }
}

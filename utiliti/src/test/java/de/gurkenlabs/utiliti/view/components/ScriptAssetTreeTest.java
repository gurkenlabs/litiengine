package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.Editor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ScriptAssetTreeTest {

  @BeforeEach
  void setUp() {
    if (Editor.instance().getGameFile() == null) {
      Editor.instance().load(null, false);
    }
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getScripts().clear();
    }
  }

  @Test
  void testScriptAssetTreeInitializesAndSelectsDefault() {
    AssetPanel assetPanel = new AssetPanel();
    ScriptAssetTree tree = new ScriptAssetTree(assetPanel);

    assertNotNull(tree);
    assertDoesNotThrow(tree::selectDefault);
    assertEquals(AssetPanel.AssetType.SCRIPT, assetPanel.getCurrentType());
  }

  @Test
  void testScriptAssetTreeLoadsAndCountsCategories() {
    AssetPanel assetPanel = new AssetPanel();
    ScriptAssetTree tree = new ScriptAssetTree(assetPanel);

    ScriptDefinition entityScript = new ScriptDefinition("HeroScript", "java", "HeroScript.java", "HeroScript", ScriptHostType.ENTITY);
    ScriptDefinition envScript = new ScriptDefinition("DungeonScript", "java", "DungeonScript.java", "DungeonScript", ScriptHostType.ENVIRONMENT);
    ScriptDefinition gameScript = new ScriptDefinition("CoreGameScript", "java", "CoreGameScript.java", "CoreGameScript", ScriptHostType.GAME);

    Editor.instance().getGameFile().getScripts().addAll(List.of(entityScript, envScript, gameScript));

    tree.forceUpdate();
    tree.selectDefault();

    assertEquals(AssetPanel.AssetType.SCRIPT, assetPanel.getCurrentType());
    assertEquals(3, assetPanel.getTotalItemCount());
  }
}

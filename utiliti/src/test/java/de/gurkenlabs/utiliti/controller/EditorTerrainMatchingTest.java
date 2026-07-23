package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class EditorTerrainMatchingTest {
  @Test
  public void unresolvedExternalTilesetMatchesSourceByFileName() throws Exception {
    URL tsx = EditorTerrainMatchingTest.class.getResource("/de/gurkenlabs/utiliti/controller/naughtytiles.tsx");
    assertNotNull(tsx);
    Tileset source = XmlUtilities.read(Tileset.class, tsx);
    assertFalse(source.getTerrainSets() == null || source.getTerrainSets().isEmpty(),
      "the .tsx must define wang terrain sets");

    // Mimics what ResourceBundle.load produces for a map whose external tileset is not
    // registered as a game-file tileset: the wrapper keeps its source reference but the
    // sourceTileset is never resolved (so name / tile dimensions are empty).
    Tileset unresolvedWrapper = new Tileset();
    unresolvedWrapper.setSource("naughtytiles.tsx");

    assertTrue(Editor.tilesetsReferToSameFile(unresolvedWrapper, source),
      "unresolved external wrapper should match the .tsx by file name");

    assertTrue(unresolvedWrapper.getTerrainSets() == null || unresolvedWrapper.getTerrainSets().isEmpty(),
      "before recovery the wrapper must have no terrains");

    unresolvedWrapper.copyTerrainSetsFrom(source);
    assertEquals(1, unresolvedWrapper.getTerrainSets().size());
    assertEquals(4, unresolvedWrapper.getTerrainSets().get(0).getTerrains().size());
  }
}

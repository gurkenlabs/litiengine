package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EditorGameFileTerrainTest {
  @Test
  public void recoversTerrainsForResolvedReferenceTilesets(@TempDir Path projectDir) throws Exception {
    // The .tsx carries the wang terrains and lives in the project.
    URL tsx = EditorTerrainMatchingTest.class.getResource("/de/gurkenlabs/utiliti/controller/naughtytiles.tsx");
    Files.copy(new File(tsx.toURI()).toPath(), projectDir.resolve("naughtytiles.tsx"));

    // A game-file tileset entry that is an external reference (like game.litidata's
    // top-level <tileset source="naughtytiles.tsx"/>).
    Tileset reference = new Tileset();
    reference.setSource("naughtytiles.tsx");

    // A map whose tileset wrapper only knows the source file. ResourceBundle.load
    // resolves it to the reference by source file name (the fix in Tileset.load).
    TmxMap map = new TmxMap();
    Tileset wrapper = new Tileset();
    set(wrapper, "firstgid", 513);
    wrapper.setSource("naughtytiles.tsx");
    map.getTilesets().add(wrapper);

    ResourceBundle gameFile = new ResourceBundle();
    gameFile.getTilesets().add(reference);
    gameFile.getMaps().add(map);

    wrapper.load(gameFile.getTilesets());
    assertTrue(wrapper.getSourceTileset() == reference, "map wrapper must resolve to the game-file reference");

    Editor.loadProjectTilesetTerrains(gameFile, projectDir);

    // The recovery delegates into the resolved reference; the map wrapper exposes terrains.
    assertNotNull(reference.getTerrainSets());
    assertNotNull(wrapper.getTerrainSets());
    assertEquals(4, wrapper.getTerrainSets().get(0).getTerrains().size());
    assertEquals(4, reference.getTerrainSets().get(0).getTerrains().size());
  }

  @Test
  public void recoversTerrainsFromUppercaseTilesetExtension(@TempDir Path projectDir) throws Exception {
    URL tsx = EditorTerrainMatchingTest.class.getResource("/de/gurkenlabs/utiliti/controller/naughtytiles.tsx");
    Files.copy(new File(tsx.toURI()).toPath(), projectDir.resolve("NAUGHTYTILES.TSX"));
    Tileset reference = new Tileset();
    reference.setSource("NAUGHTYTILES.TSX");
    ResourceBundle gameFile = new ResourceBundle();
    gameFile.getTilesets().add(reference);

    Editor.loadProjectTilesetTerrains(gameFile, projectDir);

    assertNotNull(reference.getTerrainSets());
    assertEquals(4, reference.getTerrainSets().getFirst().getTerrains().size());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

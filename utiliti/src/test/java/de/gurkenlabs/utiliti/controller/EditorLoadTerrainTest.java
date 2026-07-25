package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

public class EditorLoadTerrainTest {
  @Test
  public void recoversTerrainsForUnresolvedExternalTilesets(@TempDir Path projectDir) throws Exception {
    // The .tsx carries the wang terrains and lives in the project.
    URL tsx = EditorTerrainMatchingTest.class.getResource("/de/gurkenlabs/utiliti/controller/naughtytiles.tsx");
    Files.copy(new File(tsx.toURI()).toPath(), projectDir.resolve("naughtytiles.tsx"));

    // Simulate the game file as the editor produces it: the map references the .tsx
    // externally, and the registered resource tileset has NO terrains (they were never
    // loaded when the game file was saved).
    TmxMap map = new TmxMap();
    Tileset wrapper = new Tileset();
    set(wrapper, "firstgid", 513);
    wrapper.setSource("naughtytiles.tsx");
    map.getTilesets().add(wrapper);

    Tileset resourceWithoutTerrains = new Tileset();
    resourceWithoutTerrains.setSource("naughtytiles.tsx");

    ResourceBundle gameFile = new ResourceBundle();
    gameFile.getMaps().add(map);
    gameFile.getTilesets().add(resourceWithoutTerrains);

    Editor.loadProjectTilesetTerrains(gameFile, projectDir);

    // The map's external reference should now expose the wang terrains.
    assertNotNull(wrapper.getTerrainSets());
    assertEquals(1, wrapper.getTerrainSets().size());
    assertEquals(4, wrapper.getTerrainSets().get(0).getTerrains().size());

    // And the resource tileset shown under "Resources -> Tilesets" too.
    assertFalse(resourceWithoutTerrains.getTerrainSets() == null || resourceWithoutTerrains.getTerrainSets().isEmpty());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

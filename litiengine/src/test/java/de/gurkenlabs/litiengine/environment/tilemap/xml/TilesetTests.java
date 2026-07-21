package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TilesetTests {
  @Test
  void testTransformations() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");

    assertFalse(map.getTilesets().isEmpty());

    var tileset = (Tileset) map.getTilesets().getFirst();

    assertTrue(tileset.getTransformations().isHflip());
    assertTrue(tileset.getTransformations().isVflip());
    assertTrue(tileset.getTransformations().isRotate());
    assertTrue(tileset.getTransformations().isPreferuntransformed());
  }

  @Test
  void testTileCollision() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");

    assertFalse(map.getTilesets().isEmpty());

    var tileset = map.getTilesets().getFirst();

    var tile = tileset.getTile(4);
    var collisionInfo = tile.getCollisionInfo();
    assertFalse(collisionInfo.getMapObjects().isEmpty());

    var collision = collisionInfo.getMapObjects().getFirst();
    assertEquals(7.23684, collision.getX(), 0.00001);
    assertEquals(10.9211, collision.getY(), 0.00001);
    assertEquals(10f, collision.getWidth());
    assertEquals(10f, collision.getHeight());
  }

  @Test
  void copiedTilesetEntryIsolatesCollisionData() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");
    Tileset original = (Tileset) map.getTilesets().getFirst();
    Tileset copy = new Tileset(original);

    var originalCollision = original.getTile(4).getCollisionInfo();
    var copiedCollision = copy.getTile(4).getCollisionInfo();
    assertNotSame(originalCollision, copiedCollision);
    assertNotSame(originalCollision.getMapObjects().getFirst(), copiedCollision.getMapObjects().getFirst());
  }

  @Test
  void testWangSets() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");

    assertFalse(map.getTilesets().isEmpty());

    var terrainSet = map.getTilesets().getFirst().getTerrainSets().getFirst();
    assertEquals("wang1", terrainSet.getName());
    assertEquals(TerrainType.MIXED, terrainSet.getType());

    var terrain1 = terrainSet.getTerrains().getFirst();
    var terrain2 = terrainSet.getTerrains().get(1);

    assertEquals("name me", terrain1.getName());
    assertEquals(Color.RED, terrain1.getColor());
    assertEquals(1.0, terrain1.getProbability(), 0.00001);

    assertEquals("second", terrain2.getName());
    assertEquals(Color.GREEN, terrain2.getColor());
    assertEquals(1.0, terrain1.getProbability(), 0.00001);

    assertArrayEquals(new ITerrain[]{null, null, null, null, null, null, null, null}, terrainSet.getTerrains(0));
    assertArrayEquals(new ITerrain[]{null, null, terrain2, null, terrain2, terrain2, null, null}, terrainSet.getTerrains(47));

    assertEquals("because wang!", terrainSet.getStringValue("tell me whyyyy"));
  }

  @Test
  void testLegacyTerrainsAreConvertedToCornerWangSet() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-legacy-terrain.tmx");

    var terrainSet = map.getTilesets().getFirst().getTerrainSets().getFirst();
    assertEquals("Terrains", terrainSet.getName());
    assertEquals(TerrainType.CORNER, terrainSet.getType());
    assertEquals("grass", terrainSet.getTerrains().getFirst().getName());
    assertEquals("dirt", terrainSet.getTerrains().get(1).getName());
    assertEquals(0, ((WangColor) terrainSet.getTerrains().getFirst()).getTileId());
    assertEquals("soft", ((WangColor) terrainSet.getTerrains().getFirst()).getStringValue("footstep", null));
    assertEquals(1, ((WangColor) terrainSet.getTerrains().get(1)).getTileId());
    assertEquals("grass", terrainSet.getTerrains(0)[7].getName());
    assertEquals("dirt", terrainSet.getTerrains(0)[1].getName());
    assertEquals("grass", terrainSet.getTerrains(0)[5].getName());
    assertEquals("dirt", terrainSet.getTerrains(0)[3].getName());
  }

  @Test
  void legacyTerrainFieldsSurviveSaveAfterDerivedWangAccess() throws Exception {
    TmxMap map = (TmxMap) Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-legacy-terrain.tmx");
    map.getTilesets().getFirst().getTerrainSets();
    Path target = Files.createTempFile("legacy-terrain", ".tmx");

    try {
      XmlUtilities.save(map, target);
      String xml = Files.readString(target);
      assertTrue(xml.contains("<terraintypes>"));
      assertTrue(xml.contains("terrain=\"0,1,0,1\""));
      assertTrue(xml.contains("<wangsets>"));

      TmxMap restored = (TmxMap) Resources.maps().get(target.toUri().toURL());
      assertEquals("grass", restored.getTilesets().getFirst().getTerrainSets().getFirst().getTerrains().getFirst().getName());
    } finally {
      Files.deleteIfExists(target);
    }
  }

  @Test
  void convertedLegacyTerrainSetCanBeRemoved() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-legacy-terrain.tmx");
    Tileset tileset = new Tileset((Tileset) map.getTilesets().getFirst());

    tileset.getTerrainSets().clear();

    assertEquals(0, tileset.getTerrainSets().size());
  }

  @Test
  void removedDerivedTerrainSetDoesNotRegenerateAfterSaveAndReload() throws Exception {
    Path target = Files.createTempFile("removed-legacy-terrain", ".tmx");
    Path reloadTarget = Files.createTempFile("reloaded-legacy-terrain", ".tmx");

    try {
      try (var source = TilesetTests.class.getResourceAsStream("test-legacy-terrain.tmx")) {
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
      TmxMap map = (TmxMap) Resources.maps().get(target.toUri().toURL());
      map.getTilesets().getFirst().getTerrainSets().clear();
      XmlUtilities.save(map, target);
      String xml = Files.readString(target);
      assertFalse(xml.contains("<terraintypes>"));
      assertFalse(xml.contains("terrain=\"0,1,0,1\""));

      Files.copy(target, reloadTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      TmxMap restored = (TmxMap) Resources.maps().get(reloadTarget.toUri().toURL());
      assertTrue(restored.getTilesets().getFirst().getTerrainSets().isEmpty());
    } finally {
      Files.deleteIfExists(target);
      Files.deleteIfExists(reloadTarget);
    }
  }

  @Test
  void testExternalLegacyTerrainsAreConvertedToCornerWangSet() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-legacy-terrain-external.tmx");

    var terrainSet = map.getTilesets().getFirst().getTerrainSets().getFirst();
    assertEquals("Terrains", terrainSet.getName());
    assertEquals(TerrainType.CORNER, terrainSet.getType());
    assertEquals("grass", terrainSet.getTerrains(0)[7].getName());
    assertEquals("dirt", terrainSet.getTerrains(0)[1].getName());
    assertEquals("grass", terrainSet.getTerrains(0)[5].getName());
    assertEquals("dirt", terrainSet.getTerrains(0)[3].getName());
  }

  @Test
  void copiedExternalTilesetSavesAsWrapperAndReloads() throws Exception {
    TmxMap original = (TmxMap) Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-legacy-terrain-external.tmx");
    Tileset originalTileset = (Tileset) original.getTilesets().getFirst();
    Tileset copiedTileset = new Tileset(originalTileset);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setTileWidth(16);
    map.setTileHeight(16);
    map.getTilesets().add(copiedTileset);
    Path directory = Files.createTempDirectory("copied-external-tileset");
    Path mapFile = directory.resolve("copied.tmx");
    Path tilesetFile = directory.resolve("test-legacy-terrain.tsx");

    try {
      assertTrue(copiedTileset.isExternal());
      assertNotSame(originalTileset.sourceTileset, copiedTileset.sourceTileset);
      try (var source = TilesetTests.class.getResourceAsStream("test-legacy-terrain.tsx")) {
        Files.copy(source, tilesetFile, StandardCopyOption.REPLACE_EXISTING);
      }
      XmlUtilities.save(map, mapFile);

      String xml = Files.readString(mapFile);
      assertTrue(xml.contains("source=\"test-legacy-terrain.tsx\""));
      assertFalse(xml.contains("<terraintypes>"));
      assertFalse(xml.contains("<tile id="));

      TmxMap restored = (TmxMap) Resources.maps().get(mapFile.toUri().toURL());
      assertTrue(((Tileset) restored.getTilesets().getFirst()).isExternal());
      assertEquals("grass", restored.getTilesets().getFirst().getTerrainSets().getFirst().getTerrains().getFirst().getName());
    } finally {
      Files.deleteIfExists(mapFile);
      Files.deleteIfExists(tilesetFile);
      Files.deleteIfExists(directory);
    }
  }

  @Test
  void missingTerrainSetsAreLoadedFromSource() throws Exception {
    Tileset source = new Tileset();
    set(source, "tilecount", 64);
    set(source, "allTiles", new java.util.ArrayList<>());
    WangSet terrainSet = new WangSet("Terrains", TerrainType.CORNER);
    terrainSet.getTerrains().add(new WangColor("darksnow", Color.RED));
    terrainSet.getTerrains().add(new WangColor("water", Color.BLUE));
    terrainSet.getWangTiles().add(new WangTile(50, new int[] {0, 1, 0, 1, 0, 1, 0, 1}));
    source.getOrCreateTerrainSets().add(terrainSet);

    Tileset unresolvedReference = new Tileset();
    set(unresolvedReference, "firstgid", 1);
    set(unresolvedReference, "tilecount", 64);
    set(unresolvedReference, "allTiles", new java.util.ArrayList<>());
    set(unresolvedReference, "source", "naughtytiles.tsx");
    assertNull(unresolvedReference.getTerrainSets());

    unresolvedReference.copyTerrainSetsFrom(source);

    var loaded = unresolvedReference.getTerrainSets();
    assertNotNull(loaded);
    assertEquals(1, loaded.size());
    assertEquals("Terrains", loaded.getFirst().getName());
    assertEquals(2, loaded.getFirst().getTerrains().size());
    assertEquals("darksnow", loaded.getFirst().getTerrains().getFirst().getName());
    assertEquals("water", loaded.getFirst().getTerrains().get(1).getName());
    assertEquals(1, ((WangSet) loaded.getFirst()).getWangTiles().size());
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void externalWrapperResolvesToResourceTilesetWithTerrains() throws Exception {
    URL tsx = TilesetTests.class.getResource("/de/gurkenlabs/litiengine/environment/tilemap/xml/naughtytiles.tsx");
    Tileset resource = XmlUtilities.read(Tileset.class, tsx);
    assertFalse("the loaded .tsx resource must carry wang terrains",
      resource.getTerrainSets() == null || resource.getTerrainSets().isEmpty());

    Tileset wrapper = new Tileset();
    set(wrapper, "firstgid", 513);
    set(wrapper, "source", "naughtytiles.tsx");

    // This is what ResourceBundle.load does to resolve external map tilesets.
    wrapper.load(List.of(resource));

    var terrains = wrapper.getTerrainSets();
    assertNotNull(terrains);
    assertEquals(1, terrains.size());
    assertEquals(4, terrains.get(0).getTerrains().size());
  }

  @Test
  void externalWrapperResolvesToReferenceBySourceFileName() throws Exception {
    URL tsx = TilesetTests.class.getResource("/de/gurkenlabs/litiengine/environment/tilemap/xml/naughtytiles.tsx");
    Tileset full = XmlUtilities.read(Tileset.class, tsx);

    // A game-file tileset entry that is itself an external reference (name != file name).
    Tileset reference = new Tileset();
    set(reference, "source", "naughtytiles.tsx");
    reference.copyTerrainSetsFrom(full);

    // The map's tileset wrapper only knows the source file, not the tileset name.
    Tileset wrapper = new Tileset();
    set(wrapper, "firstgid", 513);
    set(wrapper, "source", "naughtytiles.tsx");

    wrapper.load(List.of(reference));

    var terrains = wrapper.getTerrainSets();
    assertNotNull(terrains);
    assertEquals(1, terrains.size());
    assertEquals(4, terrains.get(0).getTerrains().size());
  }

}

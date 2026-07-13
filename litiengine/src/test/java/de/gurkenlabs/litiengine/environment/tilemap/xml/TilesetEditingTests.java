package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.awt.Color;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import org.junit.jupiter.api.Test;

class TilesetEditingTests {

  @Test
  void frameAndAnimationCopyFramesIndependently() {
    Frame frame = new Frame(2, 150);
    TileAnimation animation = new TileAnimation(List.of(frame));

    frame.setDuration(300);

    assertEquals(150, animation.getFrames().getFirst().getDuration());
    assertEquals(150, animation.getTotalDuration());
  }

  @Test
  void tileOffsetIsMutable() {
    TileOffset offset = new TileOffset(2, -3);
    offset.setX(5);
    offset.setY(7);

    assertEquals(5, offset.getX());
    assertEquals(7, offset.getY());
  }

  @Test
  void tilesetCopiesAndRestoresEditableMetadata() throws Exception {
    Tileset tileset = tilesetWithEntry();
    tileset.setName("before");
    tileset.setObjectalignment("center");
    tileset.setTilerendersize("grid");
    tileset.setFillmode("preserve-aspect-fit");
    tileset.setTileOffset(2, -1);
    tileset.setValue("theme", "hospital");
    TilesetEntry entry = (TilesetEntry) tileset.getTile(0);
    entry.setType("wall");
    entry.setProbability(0.25);
    entry.setValue("material", "stone");
    entry.setAnimation(new TileAnimation(List.of(new Frame(0, 100), new Frame(1, 150))));
    Tileset snapshot = new Tileset(tileset);

    tileset.setName("after");
    tileset.setTileOffset(0, 0);
    entry.setType("floor");
    entry.setProbability(1.0);
    entry.removeProperty("material");
    entry.setAnimation(null);
    tileset.copyFrom(snapshot);

    TilesetEntry restored = (TilesetEntry) tileset.getTile(0);
    assertEquals("before", tileset.getName());
    assertEquals("center", tileset.getObjectalignment());
    assertEquals("grid", tileset.getTilerendersize());
    assertEquals("preserve-aspect-fit", tileset.getFillmode());
    assertEquals(2, tileset.getTileOffset().getX());
    assertEquals(-1, tileset.getTileOffset().getY());
    assertEquals("hospital", tileset.getStringValue("theme", null));
    assertEquals("wall", restored.getType());
    assertEquals(0.25, restored.getProbability(), 0.0001);
    assertEquals("stone", restored.getStringValue("material", null));
    assertEquals(250, restored.getAnimation().getTotalDuration());
    assertNotSame(entry, restored);
  }

  @Test
  void defaultTileProbabilityIsOneAndCanBeCleared() {
    TilesetEntry entry = new TilesetEntry();

    assertEquals(1.0, entry.getProbability());
    entry.setProbability(0.5);
    assertEquals(0.5, entry.getProbability());
    entry.setProbability(1.0);
    assertEquals(1.0, entry.getProbability());
    assertThrows(IllegalArgumentException.class, () -> entry.setProbability(-1));
    assertThrows(IllegalArgumentException.class, () -> entry.setProbability(Double.NaN));
  }

  @Test
  void restoringExternalTilesetPreservesItsMapReference() throws Exception {
    Tileset external = tilesetWithEntry();
    set(external, "firstgid", 99);
    set(external, "source", "shared.tsx");
    external.setName("external-before");
    Tileset mapReference = tilesetWithEntry();
    set(mapReference, "firstgid", 7);
    set(mapReference, "sourceTileset", external);
    Tileset snapshot = new Tileset(mapReference);

    external.setName("external-after");
    mapReference.copyFrom(snapshot);

    assertEquals("external-before", external.getName());
    assertEquals(99, external.getFirstGridId());
    assertEquals("shared.tsx", field(external, "source"));
    assertEquals(7, mapReference.getFirstGridId());
    assertNotNull(mapReference.getTile(0));
    assertEquals(2, ((List<?>) field(external, "allTiles")).size());
  }

  @Test
  void restoredTilesetPropertiesDoNotMutateTheSnapshot() throws Exception {
    Tileset tileset = tilesetWithEntry();
    tileset.setValue("theme", "before");
    Tileset snapshot = new Tileset(tileset);

    tileset.copyFrom(snapshot);
    tileset.setValue("theme", "after");

    assertEquals("before", snapshot.getStringValue("theme", null));
  }

  @Test
  void tilesetCanRestoreNamedTerrainDefinitions() throws Exception {
    Tileset bundled = tilesetWithEntry();
    WangSet generated = new WangSet("Terrains", TerrainType.CORNER);
    generated.getTerrains().add(new WangColor("Terrain 1", Color.RED));
    bundled.getOrCreateTerrainSets().add(generated);
    Tileset source = tilesetWithEntry();
    WangSet named = new WangSet("Terrains", TerrainType.CORNER);
    named.getTerrains().add(new WangColor("gravel", Color.GRAY));
    source.getOrCreateTerrainSets().add(named);

    bundled.copyTerrainSetsFrom(source);

    assertEquals("gravel", bundled.getTerrainSets().getFirst().getTerrains().getFirst().getName());
  }

  @Test
  void tilesetEnrichmentOnlyRestoresGeneratedTerrainMetadata() throws Exception {
    Tileset bundled = tilesetWithEntry();
    WangSet generated = new WangSet("Terrains", TerrainType.CORNER);
    generated.getTerrains().add(new WangColor("Terrain 1", Color.RED));
    generated.getWangTiles().add(new WangTile(0, new int[] {0, 1, 0, 0, 0, 0, 0, 0}));
    bundled.getOrCreateTerrainSets().add(generated);
    Tileset source = tilesetWithEntry();
    WangSet named = new WangSet("Terrains", TerrainType.CORNER);
    WangColor gravel = new WangColor("gravel", Color.GRAY);
    gravel.setTileId(1);
    named.getTerrains().add(gravel);
    named.getWangTiles().add(new WangTile(0, new int[] {0, 0, 0, 1, 0, 0, 0, 0}));
    source.getOrCreateTerrainSets().add(named);

    bundled.enrichTerrainMetadataFrom(source);

    WangColor enriched = (WangColor) bundled.getTerrainSets().getFirst().getTerrains().getFirst();
    assertEquals("gravel", enriched.getName());
    assertEquals(1, enriched.getTileId());
    assertEquals(1, ((WangSet) bundled.getTerrainSets().getFirst()).getWangId(0)[1]);
    assertEquals(0, ((WangSet) bundled.getTerrainSets().getFirst()).getWangId(0)[3]);
  }

  @Test
  void wangTileNormalizesMalformedIds() {
    WangTile tile = new WangTile(0, new int[] {-1, 2, 3});

    assertEquals(List.of(0, 2, 3, 0, 0, 0, 0, 0), java.util.Arrays.stream(tile.getWangId()).boxed().toList());
    assertThrows(IllegalArgumentException.class, () -> tile.setTerrain(0, -1));
  }

  @Test
  void spritesheetResourceCopiesAllPersistedFields() {
    SpritesheetResource original = new SpritesheetResource(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB), "before", 2, 2);
    original.setImageFormat(ImageFormat.PNG);
    original.setKeyframes(new int[] {100, 150});
    SpritesheetResource copy = new SpritesheetResource(original);

    original.setName("after");
    original.setWidth(4);
    copy.copyFrom(original);

    assertEquals("after", copy.getName());
    assertEquals(4, copy.getWidth());
    assertEquals(2, copy.getHeight());
    assertEquals(ImageFormat.PNG, copy.getImageFormat());
    assertEquals(100, copy.getKeyframes()[0]);
    assertEquals(150, copy.getKeyframes()[1]);
  }

  private static Tileset tilesetWithEntry() throws Exception {
    Tileset tileset = new Tileset();
    set(tileset, "firstgid", 1);
    set(tileset, "tilewidth", 16);
    set(tileset, "tileheight", 16);
    set(tileset, "tilecount", 2);
    set(tileset, "columns", 2);
    set(tileset, "allTiles", List.of(new TilesetEntry(tileset, 0), new TilesetEntry(tileset, 1)));
    return tileset;
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Object field(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }
}

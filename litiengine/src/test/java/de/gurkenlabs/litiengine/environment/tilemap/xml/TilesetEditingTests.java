package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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
  void animationDurationTracksMutableFramesAndHandlesNoDuration() {
    TileAnimation animation = new TileAnimation(List.of(new Frame(2, 100)));
    ((Frame) animation.getFrames().getFirst()).setDuration(250);

    assertEquals(250, animation.getTotalDuration());
    animation.getFrames().clear();
    assertEquals(0, animation.getTotalDuration());
    assertNull(animation.getCurrentFrame());

    animation.getFrames().add(new Frame(3, 0));
    assertEquals(3, animation.getCurrentFrame().getTileId());
  }

  @Test
  void animationSwitchesFramesAtExactDurationBoundary() {
    TileAnimation animation = new TileAnimation(List.of(new Frame(2, 100), new Frame(3, 150)));

    assertEquals(2, animation.getFrameAt(99).getTileId());
    assertEquals(3, animation.getFrameAt(100).getTileId());
    assertEquals(3, animation.getFrameAt(249).getTileId());
    assertEquals(2, animation.getFrameAt(250).getTileId());
  }

  @Test
  void programmaticTileCountCreatesEntriesAndMarshals() throws Exception {
    Tileset tileset = new Tileset();
    tileset.setName("generated");
    tileset.setTileWidth(16);
    tileset.setTileHeight(16);
    tileset.setColumns(2);
    tileset.setTileCount(2);
    ((TilesetEntry) tileset.getTile(1)).setType("wall");
    Path target = Files.createTempFile("generated-tileset", ".tsx");

    try {
      XmlUtilities.save(tileset, target);
      String xml = Files.readString(target);
      assertTrue(xml.contains("tilecount=\"2\""));
      assertTrue(xml.contains("<tile id=\"1\" type=\"wall\""));
    } finally {
      Files.deleteIfExists(target);
    }
  }

  @Test
  void externalTilesetMetadataAndSpritesheetDelegateToSource() throws Exception {
    Tileset source = tilesetWithEntry();
    source.setObjectalignment("center");
    source.setTilerendersize("grid");
    source.setFillmode("stretch");
    Spritesheet original = new Spritesheet(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), "external-source-a.png", 16, 16);
    set(source, "spriteSheet", original);
    Tileset wrapper = new Tileset();
    set(wrapper, "source", "external.tsx");
    set(wrapper, "sourceTileset", source);

    assertEquals("center", wrapper.getObjectalignment());
    assertEquals("grid", wrapper.getTilerendersize());
    assertEquals("stretch", wrapper.getFillmode());
    assertSame(original, wrapper.getSpritesheet());

    source.setTileWidth(8);
    assertNull(field(source, "spriteSheet"));
    Spritesheet updated = new Spritesheet(new BufferedImage(8, 16, BufferedImage.TYPE_INT_ARGB), "external-source-b.png", 8, 16);
    set(source, "spriteSheet", updated);
    assertSame(updated, wrapper.getSpritesheet());
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
  void externalWrapperRestoresNestedSourceFromSnapshot() throws Exception {
    Tileset source = tilesetWithEntry();
    source.setName("before");
    source.setObjectalignment("center");
    ((TilesetEntry) source.getTile(1)).setType("wall");
    WangSet terrains = new WangSet("Ground", TerrainType.CORNER);
    terrains.getTerrains().add(new WangColor("grass", Color.GREEN));
    source.getOrCreateTerrainSets().add(terrains);
    Tileset wrapper = new Tileset();
    set(wrapper, "firstgid", 7);
    set(wrapper, "source", "external.tsx");
    set(wrapper, "sourceTileset", source);
    Tileset snapshot = new Tileset(wrapper);

    source.setName("after");
    source.setObjectalignment("topleft");
    ((TilesetEntry) source.getTile(1)).setType("floor");
    source.getTerrainSets().clear();
    wrapper.copyFrom(snapshot);

    assertSame(source, field(wrapper, "sourceTileset"));
    assertEquals(7, wrapper.getFirstGridId());
    assertEquals("external.tsx", field(wrapper, "source"));
    assertEquals("before", wrapper.getName());
    assertEquals("center", wrapper.getObjectalignment());
    assertEquals(2, wrapper.getTileCount());
    assertEquals("wall", wrapper.getTile(1).getType());
    assertEquals("Ground", wrapper.getTerrainSets().getFirst().getName());
    assertEquals("grass", wrapper.getTerrainSets().getFirst().getTerrains().getFirst().getName());
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

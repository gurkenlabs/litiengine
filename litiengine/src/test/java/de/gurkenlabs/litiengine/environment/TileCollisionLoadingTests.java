package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.PolyShape;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TileCollisionLoadingTests {

  @BeforeAll
  static void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  static void terminateGame() {
    GameTest.terminateGame();
  }

  @Test
  void tileCollisionRectangleProducesCollisionBox() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionRect(map, 2, 2, 8);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(1, colliders.size());

    CollisionBox box = colliders.getFirst();
    assertNotNull(box.getCollisionBox());
    assertEquals(8.0, box.getCollisionBoxWidth(), 0.0001);
    assertEquals(8.0, box.getCollisionBoxHeight(), 0.0001);
  }

  @Test
  void emptyTilesDoNotGenerateColliders() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    Environment env = new Environment(map);
    env.init();

    assertEquals(0, env.getEntities(CollisionBox.class).size());
  }

  @Test
  void repeatedTilesGenerateIndependentColliders() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gid = map.getTilesets().getFirst().getFirstGridId();
    layer.setTile(0, 0, gid);
    layer.setTile(2, 3, gid);
    layer.setTile(5, 5, gid);

    TilesetEntry entry = (TilesetEntry) map.getTilesets().getFirst().getTile(0);
    MapObject obj = new MapObject();
    obj.setId(1);
    obj.setLocation(0, 0);
    obj.setWidth(8);
    obj.setHeight(8);
    entry.getOrCreateCollisionInfo().addMapObject(obj);

    Environment env = new Environment(map);
    env.init();

    assertEquals(3, env.getEntities(CollisionBox.class).size());
  }

  @Test
  void polygonCollisionObjectsAreSkipped() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionPolygon(map, 0, 0);
    Environment env = new Environment(map);
    env.init();

    assertEquals(0, env.getEntities(CollisionBox.class).size());
  }

  @Test
  void tilesetOffsetIsApplied() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    ((Tileset) map.getTilesets().getFirst()).setTileOffset(4, -2);
    addCollisionRect(map, 0, 0, 8);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(1, colliders.size());
    assertNotNull(colliders.getFirst().getCollisionBox());
  }

  @Test
  void tileCollisionWithHorizontalFlipTransformsCollisionBox() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionRect(map, 0, 0, 8);
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gidWithFlip = layer.getTile(0, 0).getGridId() | 0x80000000;
    layer.setTile(0, 0, gidWithFlip);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(1, colliders.size());
    assertNotNull(colliders.getFirst().getCollisionBox());
  }

  @Test
  void tileCollisionWithVerticalFlipTransformsCollisionBox() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionRect(map, 0, 0, 8);
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gidWithFlip = layer.getTile(0, 0).getGridId() | 0x40000000;
    layer.setTile(0, 0, gidWithFlip);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(1, colliders.size());
    assertNotNull(colliders.getFirst().getCollisionBox());
  }

  @Test
  void tileCollisionWithDiagonalFlipTransformsCollisionBox() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionRect(map, 0, 0, 8);
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gidWithFlip = layer.getTile(0, 0).getGridId() | 0x20000000;
    layer.setTile(0, 0, gidWithFlip);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(1, colliders.size());
    assertNotNull(colliders.getFirst().getCollisionBox());
  }

  @Test
  void multipleCollisionRectsOnSameTileGenerateMultipleColliders() {
    TmxMap map = createMapWithCollision(10, 10, 16, 16);
    addCollisionRect(map, 0, 0, 8);
    addCollisionRect(map, 0, 0, 4);
    Environment env = new Environment(map);
    env.init();

    List<CollisionBox> colliders = env.getEntities(CollisionBox.class).stream().toList();
    assertEquals(2, colliders.size());
  }

  // --- helpers ---

  private static void setFirstGridId(Tileset tileset, int gid) {
    try {
      Field field = Tileset.class.getDeclaredField("firstgid");
      field.setAccessible(true);
      field.setInt(tileset, gid);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setSpritesheet(Tileset tileset, int tileWidth, int tileHeight) {
    BufferedImage img = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
    img.setRGB(0, 0, 1, 1, new int[]{0xFF000000}, 0, 1);
    Spritesheet ss = new Spritesheet(img, tileset.getName() + ".png", tileWidth, tileHeight);
    try {
      Field field = Tileset.class.getDeclaredField("spriteSheet");
      field.setAccessible(true);
      field.set(tileset, ss);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static TmxMap createMapWithCollision(int width, int height, int tileWidth, int tileHeight) {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("tile-collision-test");
    map.setWidth(width);
    map.setHeight(height);
    map.setTileWidth(tileWidth);
    map.setTileHeight(tileHeight);

    Tileset tileset = new Tileset();
    tileset.setName("collision-test");
    tileset.setTileWidth(tileWidth);
    tileset.setTileHeight(tileHeight);
    tileset.setColumns(4);
    setFirstGridId(tileset, 1);
    tileset.setTileCount(width * height);
    setSpritesheet(tileset, tileWidth, tileHeight);
    map.getTilesets().add(tileset);

    TileLayer layer = new TileLayer(width, height);
    layer.setName("collision");
    map.addLayer(layer);
    return map;
  }

  private static void addCollisionRect(TmxMap map, int tileX, int tileY, int size) {
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gid = map.getTilesets().getFirst().getFirstGridId();
    layer.setTile(tileX, tileY, gid);

    TilesetEntry entry = (TilesetEntry) map.getTilesets().getFirst().getTile(0);
    MapObject obj = new MapObject();
    obj.setId(1);
    obj.setLocation(0, 0);
    obj.setWidth(size);
    obj.setHeight(size);
    entry.getOrCreateCollisionInfo().addMapObject(obj);
  }

  private static void addCollisionPolygon(TmxMap map, int tileX, int tileY) {
    TileLayer layer = (TileLayer) map.getTileLayers().getFirst();
    int gid = map.getTilesets().getFirst().getFirstGridId();
    layer.setTile(tileX, tileY, gid);

    TilesetEntry entry = (TilesetEntry) map.getTilesets().getFirst().getTile(0);
    MapObject obj = new MapObject();
    obj.setId(1);
    obj.setLocation(0, 0);
    obj.setWidth(8);
    obj.setHeight(8);
    PolyShape polygon = new PolyShape();
    polygon.getPoints().add(new java.awt.geom.Point2D.Float(0, 0));
    polygon.getPoints().add(new java.awt.geom.Point2D.Float(8, 0));
    polygon.getPoints().add(new java.awt.geom.Point2D.Float(4, 8));
    obj.setPolygon(polygon);
    entry.getOrCreateCollisionInfo().addMapObject(obj);
  }
}

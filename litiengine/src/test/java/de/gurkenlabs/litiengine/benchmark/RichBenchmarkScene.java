package de.gurkenlabs.litiengine.benchmark;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tile;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileData;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class RichBenchmarkScene {

  private RichBenchmarkScene() { throw new UnsupportedOperationException(); }

  private static final int WARMUP = 100;
  private static final int SAMPLES = 1000;

  private static boolean texturesRegistered;

  private static synchronized void ensureTextures() {
    if (texturesRegistered) return;

    // entity sprite: 16x16 red square with a dark inset
    BufferedImage entityTex = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D eg = entityTex.createGraphics();
    eg.setColor(new Color(220, 50, 50));
    eg.fillRect(0, 0, 16, 16);
    eg.setColor(new Color(180, 30, 30));
    eg.fillRect(2, 2, 12, 12);
    eg.setColor(new Color(240, 100, 100));
    eg.fillRect(4, 4, 8, 8);
    eg.dispose();

    // register under all naming conventions the animation controller looks up
    for (CreatureAnimationState state : CreatureAnimationState.values()) {
      for (Direction dir : Direction.values()) {
        String name = "bench-creature-" + state.spriteString() + "-" + dir.name().toLowerCase();
        Resources.spritesheets().load(entityTex, name + ".png", 16, 16);
      }
    }

    // tile sheet: 2x2 grid of colored 16x16 tiles
    BufferedImage tileSheet = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D tg = tileSheet.createGraphics();
    tg.setColor(new Color(100, 180, 100));
    tg.fillRect(0, 0, 16, 16);
    tg.setColor(new Color(180, 180, 80));
    tg.fillRect(16, 0, 16, 16);
    tg.setColor(new Color(80, 80, 180));
    tg.fillRect(0, 16, 16, 16);
    tg.setColor(new Color(180, 80, 180));
    tg.fillRect(16, 16, 16, 16);
    tg.dispose();

    Resources.spritesheets().load(tileSheet, "bench-tiles.png", 16, 16);

    texturesRegistered = true;
  }

  private static TmxMap createTileMap(int widthTiles, int heightTiles) {
    ensureTextures();
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setWidth(widthTiles);
    map.setHeight(heightTiles);
    map.setTileWidth(16);
    map.setTileHeight(16);

    // Set renderLayers to include tile-layer and map-object-layer lists
    map.getRenderLayers().clear();

    // tileset -- uses the bench-tiles spritesheet we registered
    Tileset tileset = new Tileset();
    tileset.setName("bench-tiles");
    setTilesetFields(tileset, 1, 16, 16, 4, 2, "bench-tiles.png");

    map.getTilesets().add(tileset);

    // tile data: fill with alternating GIDs 1..4
    List<Tile> tileList = new ArrayList<>();
    for (int i = 0; i < widthTiles * heightTiles; i++) {
      int x = i % widthTiles;
      int y = i / widthTiles;
      int gid = 1 + ((x + y) % 4);
      tileList.add(new Tile(gid));
    }

    TileData data;
    try {
      data = new TileData(tileList, widthTiles, heightTiles, "csv", null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create tile data", e);
    }

    TileLayer layer = new TileLayer(data);
    layer.setName("ground");
    layer.setWidth(widthTiles);
    layer.setHeight(heightTiles);
    map.addLayer(layer);

    // finish the map to resolve tileset entries
    try {
      map.finish(new URL("file:///benchmap.tmx"));
    } catch (Exception e) {
      throw new RuntimeException("Failed to finish map", e);
    }

    return map;
  }

  private static void setTilesetFields(Tileset tileset, int firstGid, int tileWidth, int tileHeight,
      int tileCount, int columns, String imageSource) {
    try {
      java.lang.reflect.Field fFirstGid = Tileset.class.getDeclaredField("firstgid");
      fFirstGid.setAccessible(true);
      fFirstGid.set(tileset, firstGid);

      java.lang.reflect.Field fTileWidth = Tileset.class.getDeclaredField("tilewidth");
      fTileWidth.setAccessible(true);
      fTileWidth.set(tileset, tileWidth);

      java.lang.reflect.Field fTileHeight = Tileset.class.getDeclaredField("tileheight");
      fTileHeight.setAccessible(true);
      fTileHeight.set(tileset, tileHeight);

      java.lang.reflect.Field fTileCount = Tileset.class.getDeclaredField("tilecount");
      fTileCount.setAccessible(true);
      fTileCount.set(tileset, tileCount);

      java.lang.reflect.Field fColumns = Tileset.class.getDeclaredField("columns");
      fColumns.setAccessible(true);
      fColumns.set(tileset, columns);

      de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage img =
        new de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage();
      img.setSource(imageSource);
      img.setWidth(tileWidth * columns);
      img.setHeight(tileHeight * (tileCount / columns));

      java.lang.reflect.Field fImage = Tileset.class.getDeclaredField("image");
      fImage.setAccessible(true);
      fImage.set(tileset, img);

      // invoke the JAXB afterUnmarshal callback to initialize allTiles
      java.lang.reflect.Method afterUnmarshal = Tileset.class.getDeclaredMethod(
        "afterUnmarshal", jakarta.xml.bind.Unmarshaller.class, Object.class);
      afterUnmarshal.setAccessible(true);
      afterUnmarshal.invoke(tileset, (Object) null, (Object) null);

    } catch (Exception e) {
      throw new RuntimeException("Failed to set tileset fields", e);
    }
  }

  public static BenchmarkResult measureRealTextured(String name, Consumer<Graphics2D> renderFn) {
    ensureTextures();
    var g = BenchmarkScene.getOffscreenGraphics();

    for (int i = 0; i < WARMUP; i++) {
      renderFn.accept(g);
    }

    long gcBefore = gcCount();
    double[] samples = new double[SAMPLES];
    for (int i = 0; i < SAMPLES; i++) {
      long start = System.nanoTime();
      renderFn.accept(g);
      samples[i] = (System.nanoTime() - start) / 1_000_000.0;
    }
    long gcAfter = gcCount();

    return new BenchmarkResult(name, samples, gcBefore, gcAfter);
  }

  public static BenchmarkResult measureTexturedEntities(int entityCount) {
    ensureTextures();
    Environment env = BenchmarkScene.createBaseEnvironment();

    for (int i = 0; i < entityCount; i++) {
      Creature c = new Creature("bench-creature");
      c.setName("rich" + i);
      c.setX(i * 30 % 1800);
      c.setY(i * 20 % 900);
      c.setWidth(16);
      c.setHeight(16);
      c.setRenderType(RenderType.NORMAL);
      env.add(c);
    }

    return measureRealTextured(entityCount + " textured entities", g -> env.render(g));
  }

  public static BenchmarkResult measureTexturedTileMap(int widthTiles, int heightTiles) {
    TmxMap map = createTileMap(widthTiles, heightTiles);
    Environment env = new Environment(map);
    env.load();

    return measureRealTextured(widthTiles + "x" + heightTiles + " tilemap", g -> env.render(g));
  }

  public static BenchmarkResult measureFullScene(int entityCount, int tileMapSize,
      int lightCount, int shadowCount) {
    ensureTextures();

    TmxMap map = createTileMap(tileMapSize, tileMapSize);
    Environment env = new Environment(map);
    env.load();

    for (int i = 0; i < entityCount; i++) {
      Creature c = new Creature("bench-creature");
      c.setName("full" + i);
      c.setX(i * 30 % 1800);
      c.setY(i * 20 % 900);
      c.setWidth(16);
      c.setHeight(16);
      c.setRenderType(RenderType.NORMAL);
      env.add(c);
    }

    for (int i = 0; i < shadowCount; i++) {
      StaticShadow s = new StaticShadow(i * 40, i * 20, 30, 30, StaticShadowType.NONE);
      env.add(s);
    }

    for (int i = 0; i < lightCount; i++) {
      LightSource light = new LightSource(
        100 + i * 100, new Color(255, 255, 200, 80), LightSource.Type.ELLIPSE, true);
      light.setX(i * 200);
      light.setY(i * 150);
      env.add(light);
    }

    env.updateLighting();

    return measureRealTextured(
      entityCount + " entities + " + tileMapSize + "x" + tileMapSize + " tiles + "
        + lightCount + " lights + " + shadowCount + " shadows",
      g -> env.render(g));
  }

  private static long gcCount() {
    return ManagementFactory.getGarbageCollectorMXBeans().stream()
      .mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
  }
}

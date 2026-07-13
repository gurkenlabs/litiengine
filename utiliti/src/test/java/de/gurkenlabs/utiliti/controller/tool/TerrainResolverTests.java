package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangTile;
import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class TerrainResolverTests {
  @Test
  void fullCornerTilePaintResolvesCenterAndTransitions() throws Exception {
    Tileset tileset = tileset(16);
    WangSet terrainSet = completeCornerSet();
    tileset.getOrCreateTerrainSets().add(terrainSet);
    TileLayer layer = new TileLayer(5, 5);

    TerrainResolver.Result result = TerrainResolver.resolve(layer, tileset, terrainSet, 1, new Point(2, 2));

    assertEquals(0, result.invalidCells());
    assertEquals(16, result.changes().get(new Point(2, 2)));
    assertEquals(7, result.changes().get(new Point(2, 1)));
    assertEquals(13, result.changes().get(new Point(3, 2)));
    assertEquals(3, result.changes().get(new Point(1, 1)));
    assertEquals(9, result.changes().get(new Point(3, 3)));
  }

  @Test
  void paintingIntoAnotherTerrainPreservesTheSurroundingTerrain() throws Exception {
    Tileset tileset = tileset(81);
    WangSet terrainSet = completeTwoTerrainCornerSet();
    tileset.getOrCreateTerrainSets().add(terrainSet);
    TileLayer layer = new TileLayer(5, 5);
    for (int y = 0; y < 5; y++) {
      for (int x = 0; x < 5; x++) {
        layer.setTile(x, y, 41);
      }
    }

    TerrainResolver.Result result = TerrainResolver.resolve(layer, tileset, terrainSet, 2, new Point(2, 2));

    assertEquals(0, result.invalidCells());
    assertEquals(81, result.changes().get(new Point(2, 2)));
    assertEquals(53, result.changes().get(new Point(2, 1)));
    assertEquals(41, layer.getTile(0, 0).getGridId());
  }

  @Test
  void fullEdgeTilePaintResolvesCardinalTransitions() throws Exception {
    Tileset tileset = tileset(16);
    WangSet terrainSet = completeEdgeSet();
    tileset.getOrCreateTerrainSets().add(terrainSet);
    TileLayer layer = new TileLayer(5, 5);

    TerrainResolver.Result result = TerrainResolver.resolve(layer, tileset, terrainSet, 1, new Point(2, 2));

    assertEquals(0, result.invalidCells());
    assertEquals(16, result.changes().get(new Point(2, 2)));
    assertEquals(5, result.changes().get(new Point(2, 1)));
    assertEquals(9, result.changes().get(new Point(3, 2)));
    assertEquals(null, result.changes().get(new Point(1, 1)));
  }

  private static WangSet completeCornerSet() {
    WangSet terrainSet = new WangSet("Ground", TerrainType.CORNER);
    terrainSet.getTerrains().add(new WangColor("Grass", Color.GREEN));
    int[] corners = {1, 3, 5, 7};
    for (int pattern = 0; pattern < 16; pattern++) {
      int[] wangId = new int[8];
      for (int corner = 0; corner < corners.length; corner++) {
        wangId[corners[corner]] = (pattern >> corner) & 1;
      }
      terrainSet.getWangTiles().add(new WangTile(pattern, wangId));
    }
    return terrainSet;
  }

  private static WangSet completeTwoTerrainCornerSet() {
    WangSet terrainSet = new WangSet("Ground", TerrainType.CORNER);
    terrainSet.getTerrains().add(new WangColor("Grass", Color.GREEN));
    terrainSet.getTerrains().add(new WangColor("Dirt", Color.ORANGE));
    int[] corners = {1, 3, 5, 7};
    for (int pattern = 0; pattern < 81; pattern++) {
      int value = pattern;
      int[] wangId = new int[8];
      for (int corner : corners) {
        wangId[corner] = value % 3;
        value /= 3;
      }
      terrainSet.getWangTiles().add(new WangTile(pattern, wangId));
    }
    return terrainSet;
  }

  private static WangSet completeEdgeSet() {
    WangSet terrainSet = new WangSet("Road", TerrainType.EDGE);
    terrainSet.getTerrains().add(new WangColor("Road", Color.GRAY));
    int[] edges = {0, 2, 4, 6};
    for (int pattern = 0; pattern < 16; pattern++) {
      int[] wangId = new int[8];
      for (int edge = 0; edge < edges.length; edge++) {
        wangId[edges[edge]] = (pattern >> edge) & 1;
      }
      terrainSet.getWangTiles().add(new WangTile(pattern, wangId));
    }
    return terrainSet;
  }

  private static Tileset tileset(int tileCount) throws Exception {
    Tileset tileset = new Tileset();
    set(tileset, "firstgid", 1);
    set(tileset, "tilecount", tileCount);
    set(tileset, "allTiles", new ArrayList<>());
    return tileset;
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

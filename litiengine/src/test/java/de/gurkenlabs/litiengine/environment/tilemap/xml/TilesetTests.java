package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TilesetTests {
  @Test
  public void testTransformations() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");

    assertFalse(map.getTilesets().isEmpty());

    var tileset = (Tileset) map.getTilesets().getFirst();

    assertTrue(tileset.getTransformations().isHflip());
    assertTrue(tileset.getTransformations().isVflip());
    assertTrue(tileset.getTransformations().isRotate());
    assertTrue(tileset.getTransformations().isPreferuntransformed());
  }

  @Test
  public void testTileCollision() {
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
  public void testWangSets() {
    IMap map = Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-tileset.tmx");

    assertFalse(map.getTilesets().isEmpty());

    var terrainSet = map.getTilesets().getFirst().getTerrainSets().getFirst();
    assertEquals("wang1", terrainSet.getName());
    assertEquals(TerrainType.Mixed, terrainSet.getType());

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
}

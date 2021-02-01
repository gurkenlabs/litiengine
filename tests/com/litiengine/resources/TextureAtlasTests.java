package com.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

public class TextureAtlasTests {

  @Test
  public void testReadTextureAtlasFromFile() {
    TextureAtlas atlas = TextureAtlas.read("tests/com/litiengine/resources/gurk-nukem-atlas.xml");

    assertNotNull(atlas);
    assertEquals(123, atlas.getWidth());
    assertEquals(122, atlas.getHeight());
    assertEquals(12, atlas.getSprites().size());

    TextureAtlas.Sprite deanIdle = atlas.getSprite("Dean-idle-left.png");

    assertNotNull(deanIdle);
    assertEquals(105, deanIdle.getX());
    assertEquals(0, deanIdle.getY());
    assertEquals(18, deanIdle.getWidth());
    assertEquals(32, deanIdle.getHeight());
    assertTrue(deanIdle.isRotated());

    // now load the image 
    BufferedImage image = Resources.images().get(atlas.getAbsoluteImagePath());

    assertNotNull(image);
    assertEquals(123, image.getWidth());
    assertEquals(122, image.getHeight());
  }

  @Test
  public void testTextureAtlasLoad() {
    TextureAtlas atlas = TextureAtlas.read("tests/com/litiengine/resources/gurk-nukem-atlas.xml");
    Resources.images().load(atlas);

    BufferedImage deanIdle = Resources.images().get("Dean-idle-left.png");
    BufferedImage deanWalk = Resources.images().get("Dean-walk-left.png");
    BufferedImage gurknukemIdle = Resources.images().get("gurknukem-idle-left.png");
    BufferedImage gurknukemWalk = Resources.images().get("gurknukem-walk-left.png");
    BufferedImage icon = Resources.images().get("icon.png");
    BufferedImage jorgeIdle = Resources.images().get("Jorge-idle-left.png");
    BufferedImage jorgeWalk = Resources.images().get("Jorge-walk-left.png");
    BufferedImage propBarrel1 = Resources.images().get("prop-barrel-damaged.png");
    BufferedImage propBarrel2 = Resources.images().get("prop-barrel-destroyed.png");
    BufferedImage propBarrel3 = Resources.images().get("prop-barrel-intact.png");
    BufferedImage propBunker = Resources.images().get("prop-bunker.png");
    BufferedImage propFlag = Resources.images().get("prop-flag.png");

    assertNotNull(deanIdle);
    assertNotNull(deanWalk);
    assertNotNull(gurknukemIdle);
    assertNotNull(gurknukemWalk);
    assertNotNull(icon);
    assertNotNull(jorgeIdle);
    assertNotNull(jorgeWalk);
    assertNotNull(propBarrel1);
    assertNotNull(propBarrel2);
    assertNotNull(propBarrel3);
    assertNotNull(propBunker);
    assertNotNull(propFlag);

    // now ensure that dimensions are correct (especially for rotated sprites)
    assertEquals(32, deanIdle.getWidth());
    assertEquals(18, deanIdle.getHeight());

    assertEquals(64, deanWalk.getWidth());
    assertEquals(18, deanWalk.getHeight());

    assertEquals(36, gurknukemIdle.getWidth());
    assertEquals(18, gurknukemIdle.getHeight());

    assertEquals(72, gurknukemWalk.getWidth());
    assertEquals(18, gurknukemWalk.getHeight());

    assertEquals(64, icon.getWidth());
    assertEquals(64, icon.getHeight());
    
    assertEquals(10, propBarrel1.getWidth());
    assertEquals(12, propBarrel1.getHeight());
    
    assertEquals(104, propBunker.getWidth());
    assertEquals(41, propBunker.getHeight());
  }
}

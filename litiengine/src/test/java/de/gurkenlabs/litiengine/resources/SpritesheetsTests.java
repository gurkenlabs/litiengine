package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpritesheetsTests {

  @Test
  void testAddAndGet() {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);

    spritesheets.add("test", spritesheet);

    assertTrue(spritesheets.contains("test"));
    assertEquals(spritesheet, spritesheets.get("test"));
  }

  @Test
  void testAddClearedListener() {
    Spritesheets spritesheets = new Spritesheets();
    int[] timesCleared = {0};

    spritesheets.addClearedListener(() -> timesCleared[0]++);
    assertEquals(0, timesCleared[0]);
    spritesheets.clear();
    assertEquals(1, timesCleared[0]);
    spritesheets.clear();
    assertEquals(2, timesCleared[0]);
  }

  @Test
  void testRemoveClearedListener() {
    Spritesheets spritesheets = new Spritesheets();
    ResourcesContainerClearedListener listener = mock(ResourcesContainerClearedListener.class);

    spritesheets.addClearedListener(listener);
    spritesheets.removeClearedListener(listener);
    spritesheets.clear();

    verify(listener, never()).cleared();
  }

  @Test
  void testClear() {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);

    spritesheets.add("test", spritesheet);
    spritesheets.clear();

    assertFalse(spritesheets.contains("test"));
    assertNull(spritesheets.get("test"));
  }

  @Test
  void testContains() {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);

    spritesheets.add("test", spritesheet);

    assertTrue(spritesheets.contains("test"));
    assertFalse(spritesheets.contains("nonexistent"));
  }

  @Test
  void testGetCustomKeyFrameDurations() {
    int[] keyFrames = {1, 2, 3};
    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    Spritesheets spritesheets = new Spritesheets();
    SpritesheetResource info = new SpritesheetResource(image, "test", 16, 16);
    info.setKeyframes(keyFrames);

    Spritesheet spritesheet = spritesheets.load(info);

    assertNotNull(spritesheet);
    assertArrayEquals(keyFrames, spritesheets.getCustomKeyFrameDurations(spritesheet));
    assertArrayEquals(new int[0], spritesheets.getCustomKeyFrameDurations("nonexistent"));
  }

  @Test
  void testLoadFrom() throws IOException {
    Spritesheets spritesheets = new Spritesheets();
    String spriteInfoContent = "test.png,32,32;1,2,3\n";
    Path tempSpriteInfoFile = Files.createTempFile("spritesinfo", ".txt");
    Files.write(tempSpriteInfoFile, spriteInfoContent.getBytes());

    List<Spritesheet> loadedSprites = spritesheets.loadFrom(tempSpriteInfoFile.toString());

    assertEquals(1, loadedSprites.size());
    assertEquals("test", loadedSprites.get(0).getName());
    assertEquals(32, loadedSprites.get(0).getSpriteWidth());
    assertEquals(32, loadedSprites.get(0).getSpriteHeight());
    assertArrayEquals(new int[] {1, 2, 3}, spritesheets.getCustomKeyFrameDurations("test.png"));
  }

  @Test
  void testSaveTo() throws IOException {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);
    BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    when(spritesheet.getImage()).thenReturn(image);
    when(spritesheet.getName()).thenReturn("test");
    when(spritesheet.getSpriteWidth()).thenReturn(32);
    when(spritesheet.getSpriteHeight()).thenReturn(32);
    when(spritesheet.getImageFormat()).thenReturn(ImageFormat.PNG);

    spritesheets.add("test.png", spritesheet);

    Path tempSpriteInfoFile = Files.createTempFile("spritesinfo", ".txt");
    assertTrue(spritesheets.saveTo(tempSpriteInfoFile.toString(), false));

    String savedContent = Files.readString(tempSpriteInfoFile);
    assertEquals("test.png,32,32", savedContent.trim());
  }

  @Test
  void testLoadFromInvalidFile() throws IOException {
    Spritesheets spritesheets = new Spritesheets();
    Path tempSpriteInfoFile = Files.createTempFile("spritesinfo", ".txt");

    // Delete the file to make it invalid
    Files.delete(tempSpriteInfoFile);

    List<Spritesheet> loadedSprites = spritesheets.loadFrom(tempSpriteInfoFile.toString());

    assertTrue(loadedSprites.isEmpty());
  }

  @Test
  void testSaveToInvalidFile() {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);
    BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    when(spritesheet.getImage()).thenReturn(image);
    when(spritesheet.getName()).thenReturn("test.png");
    when(spritesheet.getSpriteWidth()).thenReturn(32);
    when(spritesheet.getSpriteHeight()).thenReturn(32);
    when(spritesheet.getImageFormat()).thenReturn(ImageFormat.PNG);

    spritesheets.add("test.png", spritesheet);

    assertFalse(spritesheets.saveTo("/invalid/file/path", false));
  }

  @Test
  void testUpdate() {
    Spritesheets spritesheets = new Spritesheets();
    Spritesheet spritesheet = mock(Spritesheet.class);
    BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    when(spritesheet.getImage()).thenReturn(image);
    when(spritesheet.getName()).thenReturn("test");
    when(spritesheet.getSpriteWidth()).thenReturn(32);
    when(spritesheet.getSpriteHeight()).thenReturn(32);
    when(spritesheet.getImageFormat()).thenReturn(ImageFormat.PNG);

    assertNull(spritesheets.get("test"));
    spritesheets.add(spritesheet.getName(), spritesheet);
    SpritesheetResource info = new SpritesheetResource(image, "test", 32, 32);
    Spritesheet updated = spritesheets.update(info);
    spritesheets.add(updated.getName(), updated);

    assertTrue(spritesheets.contains("test")); // it was removed and re-added
    assertEquals(32, spritesheets.get("test").getSpriteWidth());
    assertEquals(32, spritesheets.get("test").getSpriteHeight());
  }

}

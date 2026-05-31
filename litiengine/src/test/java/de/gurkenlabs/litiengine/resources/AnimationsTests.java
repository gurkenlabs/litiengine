package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AnimationsTests {

  private static final String JSON_RESOURCE = "de/gurkenlabs/litiengine/resources/gurke-idle-right.json";

  @BeforeEach
  void setup() {
    Resources.spritesheets().clear();
    Resources.images().clear();
    Resources.animations().clear();
  }

  @AfterEach
  void tearDown() {
    Resources.spritesheets().clear();
    Resources.images().clear();
    Resources.animations().clear();
  }

  @Test
  void importAseprite_parsesFramesAndKeyframeDurations(@TempDir Path tempDir) throws IOException {
    Path jsonPath = copyJsonAndImage(tempDir);

    Animation animation = Resources.animations().importAseprite(jsonPath);

    assertNotNull(animation);
    assertEquals("gurke-idle-right", animation.getName());
    assertArrayEquals(new int[] {120, 150, 200}, animation.getKeyFrameDurations());

    Spritesheet sheet = animation.getSpritesheet();
    assertNotNull(sheet);
    assertEquals(40, sheet.getSpriteWidth());
    assertEquals(40, sheet.getSpriteHeight());
    assertEquals(3, sheet.getColumns());
    assertEquals(1, sheet.getRows());

    assertTrue(Resources.animations().contains("gurke-idle-right"));
    assertEquals(animation, Resources.animations().get("gurke-idle-right"));
  }

  @Test
  void exportAseprite_writesRoundTrippableJson(@TempDir Path tempDir) throws IOException {
    Path jsonPath = copyJsonAndImage(tempDir);
    Animation original = Resources.animations().importAseprite(jsonPath);

    // export to a new location
    Path exportDir = Files.createDirectory(tempDir.resolve("export"));
    Path exportedJson = exportDir.resolve("gurke-idle-right.json");
    assertTrue(Resources.animations().exportAseprite(original, exportedJson));

    assertTrue(Files.exists(exportedJson));
    Path exportedImage = exportDir.resolve(original.getSpritesheet().getName() + ".png");
    assertTrue(Files.exists(exportedImage));

    // re-parse the exported JSON and verify the frame data matches
    AsepriteFormat parsed = AsepriteFormat.read(exportedJson);
    assertEquals(3, parsed.getFrames().size());
    assertEquals(120, parsed.getFrames().get(0).getDuration());
    assertEquals(150, parsed.getFrames().get(1).getDuration());
    assertEquals(200, parsed.getFrames().get(2).getDuration());
    assertEquals(40, parsed.getFrames().get(0).getWidth());
    assertEquals(40, parsed.getFrames().get(0).getHeight());
    assertEquals(0, parsed.getFrames().get(0).getX());
    assertEquals(40, parsed.getFrames().get(1).getX());
    assertEquals(80, parsed.getFrames().get(2).getX());
  }

  @Test
  void asepriteFormat_writesValidJson() {
    AsepriteFormat format = new AsepriteFormat();
    format.setImage("test-sheet.png");
    format.setImageWidth(64);
    format.setImageHeight(32);
    format.getFrames().add(new AsepriteFormat.Frame("test 0.png", 0, 0, 32, 32, 100));
    format.getFrames().add(new AsepriteFormat.Frame("test 1.png", 32, 0, 32, 32, 200));

    String json = format.writeToString(false);
    assertTrue(json.contains("\"frames\""));
    assertTrue(json.contains("\"test 0.png\""));
    assertTrue(json.contains("\"duration\":100"));
    assertTrue(json.contains("\"image\":\"test-sheet.png\""));
  }

  @Test
  void importAseprite_throwsIOExceptionWhenImageMissing(@TempDir Path tempDir) throws IOException {
    // copy the JSON but intentionally do NOT create the sibling sprite sheet image
    Path jsonPath = tempDir.resolve("gurke-idle-right.json");
    try (InputStream in = AnimationsTests.class.getClassLoader().getResourceAsStream(JSON_RESOURCE)) {
      assertNotNull(in, "Test JSON resource not found: " + JSON_RESOURCE);
      Files.copy(in, jsonPath);
    }

    IOException ex = assertThrows(IOException.class, () -> Resources.animations().importAseprite(jsonPath));
    assertTrue(ex.getMessage().contains("gurke-idle-right.png"),
      "Expected error message to mention the missing image file, got: " + ex.getMessage());
  }

  /**
   * Copies the embedded test JSON to {@code tempDir} and writes a matching 120x40 PNG sprite sheet
   * next to it so the loader can resolve the referenced image.
   */
  private static Path copyJsonAndImage(Path tempDir) throws IOException {
    Path jsonPath = tempDir.resolve("gurke-idle-right.json");
    try (InputStream in = AnimationsTests.class.getClassLoader().getResourceAsStream(JSON_RESOURCE)) {
      assertNotNull(in, "Test JSON resource not found: " + JSON_RESOURCE);
      Files.copy(in, jsonPath);
    }

    BufferedImage image = new BufferedImage(120, 40, BufferedImage.TYPE_INT_ARGB);
    Path imagePath = tempDir.resolve("gurke-idle-right.png");
    ImageIO.write(image, "png", imagePath.toFile());
    return jsonPath;
  }
}


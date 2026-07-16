package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssetFileExporterTest {
  @TempDir
  Path tempDir;

  @Test
  void exportsSpritesheetAsImageWithSafeName() throws Exception {
    BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(2, 1, Color.CYAN.getRGB());
    SpritesheetResource asset = new SpritesheetResource(source, "folder/name", 3, 2);

    List<Path> files = AssetFileExporter.export(asset, this.tempDir);

    assertEquals(1, files.size());
    assertEquals("folder_name.png", files.getFirst().getFileName().toString());
    BufferedImage exported = ImageIO.read(files.getFirst().toFile());
    assertEquals(3, exported.getWidth());
    assertEquals(2, exported.getHeight());
    assertEquals(Color.CYAN.getRGB(), exported.getRGB(2, 1));
    assertTrue(files.getFirst().startsWith(this.tempDir));
  }
}

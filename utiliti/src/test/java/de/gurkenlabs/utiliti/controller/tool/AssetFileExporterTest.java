package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

  @Test
  void rejectsAnimationSpritesheetPathTraversalWithoutCreatingEitherFile() throws Exception {
    Spritesheet sheet = mock(Spritesheet.class);
    when(sheet.isLoaded()).thenReturn(true);
    when(sheet.getName()).thenReturn("../outside");
    Animation animation = new Animation("walk", sheet, true, 100);

    List<Path> files = AssetFileExporter.export(animation, this.tempDir);

    assertTrue(files.isEmpty());
    assertFalse(java.nio.file.Files.exists(this.tempDir.resolve("walk.json")));
    assertFalse(java.nio.file.Files.exists(this.tempDir.getParent().resolve("outside.png")));
  }

  @Test
  void doesNotPublishAnimationJsonWhenSpritesheetImageIsMissing() throws Exception {
    Spritesheet sheet = mock(Spritesheet.class);
    when(sheet.isLoaded()).thenReturn(true);
    when(sheet.getName()).thenReturn("sheet");
    when(sheet.getImage()).thenReturn(null);
    Animation animation = new Animation("walk", sheet, true, 100);

    List<Path> files = AssetFileExporter.export(animation, this.tempDir);

    assertTrue(files.isEmpty());
    assertFalse(java.nio.file.Files.exists(this.tempDir.resolve("walk.json")));
    assertFalse(java.nio.file.Files.exists(this.tempDir.resolve("sheet.png")));
  }

  @Test
  void publishesCompleteAnimationPairInsideExportDirectory() throws Exception {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Spritesheet sheet = mock(Spritesheet.class);
    when(sheet.isLoaded()).thenReturn(true);
    when(sheet.getName()).thenReturn("sheet");
    when(sheet.getImage()).thenReturn(image);
    when(sheet.getSpriteWidth()).thenReturn(1);
    when(sheet.getSpriteHeight()).thenReturn(1);
    when(sheet.getColumns()).thenReturn(1);
    when(sheet.getRows()).thenReturn(1);
    when(sheet.getTotalNumberOfSprites()).thenReturn(1);
    Animation animation = new Animation("walk", sheet, true, 100);

    List<Path> files = AssetFileExporter.export(animation, this.tempDir);

    assertEquals(List.of(this.tempDir.resolve("walk.json"), this.tempDir.resolve("sheet.png")), files);
    assertTrue(files.stream().allMatch(java.nio.file.Files::isRegularFile));
    assertTrue(files.stream().allMatch(path -> path.normalize().startsWith(this.tempDir.normalize())));
  }

  @Test
  void restoresExistingAnimationPairWhenSecondPublicationFails() throws Exception {
    Animation animation = animation("sheet", new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    Path json = this.tempDir.resolve("walk.json");
    Path image = this.tempDir.resolve("sheet.png");
    java.nio.file.Files.writeString(json, "old-json");
    java.nio.file.Files.writeString(image, "old-image");
    AtomicInteger publications = new AtomicInteger();

    assertThrows(java.io.IOException.class, () -> AssetFileExporter.exportAnimation(
        animation,
        json,
        (source, target) -> {
          if (publications.incrementAndGet() == 2) {
            throw new java.io.IOException("injected JSON publication failure");
          }
          java.nio.file.Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }));

    assertEquals("old-json", java.nio.file.Files.readString(json));
    assertEquals("old-image", java.nio.file.Files.readString(image));
  }

  @Test
  void retainsRecoverableBackupWhenRollbackFails() throws Exception {
    Animation animation = animation("sheet", new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    Path json = this.tempDir.resolve("walk.json");
    Path image = this.tempDir.resolve("sheet.png");
    java.nio.file.Files.writeString(json, "old-json");
    java.nio.file.Files.writeString(image, "old-image");
    AtomicInteger publications = new AtomicInteger();

    java.io.IOException failure = assertThrows(java.io.IOException.class,
        () -> AssetFileExporter.exportAnimation(
            animation,
            json,
            (source, target) -> {
              if (publications.incrementAndGet() == 2) {
                throw new java.io.IOException("injected publication failure");
              }
              java.nio.file.Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            },
            (source, target) -> {
              if (source.getFileName().toString().equals("animation.json")) {
                throw new java.io.IOException("injected rollback failure");
              }
              java.nio.file.Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }));

    Path staging;
    try (var children = java.nio.file.Files.list(this.tempDir)) {
      staging = children
          .filter(path -> path.getFileName().toString().startsWith(".utiliti-animation-"))
          .findFirst()
          .orElseThrow();
    }
    assertTrue(java.util.Arrays.stream(failure.getSuppressed())
        .anyMatch(error -> error.getMessage().contains(staging.toString())));
    assertEquals("old-json", java.nio.file.Files.readString(
        staging.resolve(".previous").resolve("animation.json")));
    assertEquals("old-image", java.nio.file.Files.readString(image));
  }

  @Test
  void rejectsSymbolicLinkExportParentWhenSupported() throws Exception {
    Path realDirectory = java.nio.file.Files.createDirectory(this.tempDir.resolve("real"));
    Path linkedDirectory = this.tempDir.resolve("linked");
    assumeTrue(createSymbolicLink(linkedDirectory, realDirectory));
    SpritesheetResource asset = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "image", 1, 1);

    assertThrows(java.io.IOException.class, () -> AssetFileExporter.export(asset, linkedDirectory));
    assertFalse(java.nio.file.Files.exists(realDirectory.resolve("image.png")));
  }

  @Test
  void rejectsSymbolicLinkExportTargetWhenSupported() throws Exception {
    Path outside = this.tempDir.resolve("outside.png");
    java.nio.file.Files.writeString(outside, "unchanged");
    Path linkedTarget = this.tempDir.resolve("image.png");
    assumeTrue(createSymbolicLink(linkedTarget, outside));
    SpritesheetResource asset = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "image", 1, 1);

    assertThrows(java.io.IOException.class, () -> AssetFileExporter.export(asset, this.tempDir));
    assertEquals("unchanged", java.nio.file.Files.readString(outside));
  }

  @Test
  void createsWindowsSafeFileNames() {
    assertEquals("CON_", AssetFileExporter.safeFileName("CON"));
    assertEquals("name", AssetFileExporter.safeFileName("name. "));
    assertEquals("a_b", AssetFileExporter.safeFileName("a:b"));
  }

  private static Animation animation(String sheetName, BufferedImage image) {
    Spritesheet sheet = mock(Spritesheet.class);
    when(sheet.isLoaded()).thenReturn(true);
    when(sheet.getName()).thenReturn(sheetName);
    when(sheet.getImage()).thenReturn(image);
    when(sheet.getSpriteWidth()).thenReturn(1);
    when(sheet.getSpriteHeight()).thenReturn(1);
    when(sheet.getColumns()).thenReturn(1);
    when(sheet.getRows()).thenReturn(1);
    when(sheet.getTotalNumberOfSprites()).thenReturn(1);
    return new Animation("walk", sheet, true, 100);
  }

  private static boolean createSymbolicLink(Path link, Path target) {
    try {
      java.nio.file.Files.createSymbolicLink(link, target);
      return true;
    } catch (java.io.IOException | UnsupportedOperationException | SecurityException ignored) {
      return false;
    }
  }
}

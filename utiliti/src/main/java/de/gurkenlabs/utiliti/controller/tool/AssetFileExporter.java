package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundFormat;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public final class AssetFileExporter {
  private AssetFileExporter() {
  }

  public static boolean supports(Object asset) {
    return asset instanceof SpritesheetResource
        || asset instanceof Tileset
        || asset instanceof EmitterAttributes
        || asset instanceof Blueprint
        || asset instanceof SoundResource sound && sound.getFormat() != SoundFormat.UNSUPPORTED
        || asset instanceof Animation animation && animation.getSpritesheet() != null;
  }

  public static List<Path> export(Object asset, Path directory) throws IOException {
    if (!supports(asset) || directory == null) {
      return List.of();
    }
    Path exportDirectory = prepareDirectory(directory);
    if (asset instanceof SpritesheetResource spritesheet) {
      return exportSpritesheet(spritesheet, exportDirectory);
    }
    if (asset instanceof Tileset tileset) {
      return exportXml(tileset, tileset.getName(), Tileset.FILE_EXTENSION, exportDirectory);
    }
    if (asset instanceof EmitterAttributes emitter) {
      return exportXml(emitter, emitter.getName(), "xml", exportDirectory);
    }
    if (asset instanceof Blueprint blueprint) {
      return exportXml(
          blueprint, blueprint.getName(), Blueprint.BLUEPRINT_FILE_EXTENSION, exportDirectory);
    }
    if (asset instanceof SoundResource sound) {
      Path target = resolveTarget(exportDirectory,
          safeFileName(sound.getName()) + sound.getFormat().toFileExtension());
      Files.write(target, Codec.decode(sound.getData()));
      return List.of(target);
    }
    if (asset instanceof Animation animation) {
      return exportAnimation(animation, resolveTarget(
          exportDirectory, safeFileName(animation.getName()) + ".json"));
    }
    return List.of();
  }

  public static BufferedImage getSpritesheetImage(SpritesheetResource resource) {
    Spritesheet loaded = Resources.spritesheets().get(resource.getName());
    if (loaded != null && loaded.getImage() != null) {
      return loaded.getImage();
    }
    return resource.getImage() != null ? Codec.decodeImage(resource.getImage()) : null;
  }

  private static List<Path> exportSpritesheet(
      SpritesheetResource resource, Path directory) throws IOException {
    BufferedImage image = getSpritesheetImage(resource);
    if (image == null) {
      return List.of();
    }
    ImageFormat format = resource.getImageFormat();
    if (format == null || format == ImageFormat.UNSUPPORTED) {
      format = ImageFormat.PNG;
    }
    Path target = resolveTarget(
        directory, safeFileName(resource.getName()) + format.toFileExtension());
    if (!ImageIO.write(image, format.toString(), target.toFile())) {
      throw new IOException("No image writer for " + format);
    }
    return List.of(target);
  }

  public static List<Path> exportAnimation(Animation animation, Path destinationJson)
      throws IOException {
    return exportAnimation(animation, destinationJson, AssetFileExporter::moveReplacing);
  }

  static List<Path> exportAnimation(
      Animation animation, Path destinationJson, FileMover publisher) throws IOException {
    return exportAnimation(animation, destinationJson, publisher, AssetFileExporter::moveReplacing);
  }

  static List<Path> exportAnimation(
      Animation animation, Path destinationJson, FileMover publisher, FileMover rollbackMover)
      throws IOException {
    if (animation == null || destinationJson == null) {
      return List.of();
    }

    Spritesheet sheet = animation.getSpritesheet();
    if (sheet == null) {
      return List.of();
    }
    String sheetName = sheet.getName();
    if (!isSafeFileName(sheetName)) {
      return List.of();
    }

    Path json = destinationJson.toAbsolutePath().normalize();
    Path directory = json.getParent();
    if (directory == null) {
      return List.of();
    }
    directory = prepareDirectory(directory);
    json = resolveTarget(directory, json.getFileName().toString());
    Path image = resolveTarget(directory, sheetName + ".png");
    Path staging = Files.createTempDirectory(directory, ".utiliti-animation-");
    Path stagedJson = resolveTarget(staging, json.getFileName().toString());
    Path stagedImage = resolveTarget(staging, sheetName + ".png");
    Path backupDirectory = Files.createDirectory(staging.resolve(".previous"));
    Path backupJson = backupDirectory.resolve("animation.json");
    Path backupImage = backupDirectory.resolve("spritesheet.png");
    boolean jsonBackedUp = false;
    boolean imageBackedUp = false;
    boolean imagePublished = false;
    boolean jsonPublished = false;
    boolean cleanupStaging = true;
    try {
      if (!Resources.animations().exportAseprite(animation, stagedJson)
          || !Files.isRegularFile(stagedJson)
          || !Files.isRegularFile(stagedImage)) {
        return List.of();
      }

      // Revalidate immediately before publication in case a target was replaced while staging.
      json = resolveTarget(directory, json.getFileName().toString());
      image = resolveTarget(directory, image.getFileName().toString());
      if (Files.exists(image, LinkOption.NOFOLLOW_LINKS)) {
        moveReplacing(image, backupImage);
        imageBackedUp = true;
      }
      if (Files.exists(json, LinkOption.NOFOLLOW_LINKS)) {
        moveReplacing(json, backupJson);
        jsonBackedUp = true;
      }

      publisher.move(stagedImage, image);
      imagePublished = true;
      publisher.move(stagedJson, json);
      jsonPublished = true;
      return List.of(json, image);
    } catch (IOException publicationFailure) {
      boolean jsonRestored = rollbackTarget(
          json, backupJson, jsonPublished, jsonBackedUp, rollbackMover, publicationFailure);
      boolean imageRestored = rollbackTarget(
          image, backupImage, imagePublished, imageBackedUp, rollbackMover, publicationFailure);
      cleanupStaging = jsonRestored && imageRestored;
      if (!cleanupStaging) {
        publicationFailure.addSuppressed(new IOException(
            "Animation rollback failed; recoverable files retained at " + staging));
      }
      throw publicationFailure;
    } finally {
      if (cleanupStaging) {
        deleteTree(staging);
      }
    }
  }

  private static List<Path> exportXml(
      Object asset, String name, String extension, Path directory) throws IOException {
    Path target = resolveTarget(directory, safeFileName(name) + "." + extension);
    XmlUtilities.save(asset, target);
    if (!Files.isRegularFile(target)) {
      throw new IOException("Could not export resource to " + target);
    }
    return List.of(target);
  }

  public static String safeFileName(String name) {
    String safe = name == null
        ? "resource"
        : name.replaceAll("[\\x00-\\x1f\\\\/:*?\"<>|]", "_").trim().replaceAll("[. ]+$", "");
    if (safe.isEmpty() || ".".equals(safe) || "..".equals(safe)) {
      return "resource";
    }
    String deviceName = safe.split("\\.", 2)[0].toUpperCase(Locale.ROOT);
    if (deviceName.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
      safe += "_";
    }
    return safe;
  }

  private static boolean isSafeFileName(String name) {
    return name != null && name.equals(safeFileName(name));
  }

  private static Path resolveTarget(Path directory, String fileName) throws IOException {
    Path normalizedDirectory = directory.toAbsolutePath().normalize();
    Path target = normalizedDirectory.resolve(fileName).normalize();
    if (!target.getParent().equals(normalizedDirectory)
        || !Files.isDirectory(normalizedDirectory, LinkOption.NOFOLLOW_LINKS)
        || containsSymbolicLink(normalizedDirectory)
        || Files.isSymbolicLink(target)
        || Files.exists(target, LinkOption.NOFOLLOW_LINKS)
            && !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
      throw new IOException("Export target escapes destination directory");
    }
    return target;
  }

  private static Path prepareDirectory(Path directory) throws IOException {
    Path normalized = directory.toAbsolutePath().normalize();
    if (containsSymbolicLink(normalized)) {
      throw new IOException("Export directory contains a symbolic link");
    }
    Files.createDirectories(normalized);
    if (containsSymbolicLink(normalized)
        || !Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)) {
      throw new IOException("Export directory is not a regular directory");
    }
    return normalized;
  }

  private static boolean containsSymbolicLink(Path path) {
    Path current = path.getRoot();
    for (Path part : path) {
      current = current == null ? part : current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }

  private static void moveReplacing(Path source, Path target) throws IOException {
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  private static boolean rollbackTarget(
      Path target, Path backup, boolean published, boolean backedUp, FileMover rollbackMover,
      IOException failure) {
    try {
      if (published) {
        Files.deleteIfExists(target);
      }
      if (backedUp) {
        rollbackMover.move(backup, target);
      }
      return true;
    } catch (IOException rollbackFailure) {
      failure.addSuppressed(rollbackFailure);
      return false;
    }
  }

  @FunctionalInterface
  interface FileMover {
    void move(Path source, Path target) throws IOException;
  }

  static boolean deleteTree(Path root) {
    if (root == null || !Files.exists(root)) {
      return true;
    }
    boolean[] deleted = {true};
    try (var paths = Files.walk(root)) {
      paths.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
          deleted[0] = false;
        }
      });
    } catch (IOException ignored) {
      deleted[0] = false;
    }
    return deleted[0] && !Files.exists(root);
  }
}

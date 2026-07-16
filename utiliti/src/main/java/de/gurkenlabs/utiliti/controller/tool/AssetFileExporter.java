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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    Files.createDirectories(directory);
    if (asset instanceof SpritesheetResource spritesheet) {
      return exportSpritesheet(spritesheet, directory);
    }
    if (asset instanceof Tileset tileset) {
      return exportXml(tileset, tileset.getName(), Tileset.FILE_EXTENSION, directory);
    }
    if (asset instanceof EmitterAttributes emitter) {
      return exportXml(emitter, emitter.getName(), "xml", directory);
    }
    if (asset instanceof Blueprint blueprint) {
      return exportXml(
          blueprint, blueprint.getName(), Blueprint.BLUEPRINT_FILE_EXTENSION, directory);
    }
    if (asset instanceof SoundResource sound) {
      Path target = directory.resolve(safeName(sound.getName()) + sound.getFormat().toFileExtension());
      Files.write(target, Codec.decode(sound.getData()));
      return List.of(target);
    }
    if (asset instanceof Animation animation) {
      return exportAnimation(animation, directory);
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
    Path target = directory.resolve(safeName(resource.getName()) + format.toFileExtension());
    if (!ImageIO.write(image, format.toString(), target.toFile())) {
      throw new IOException("No image writer for " + format);
    }
    return List.of(target);
  }

  private static List<Path> exportAnimation(Animation animation, Path directory) {
    Path json = directory.resolve(safeName(animation.getName()) + ".json");
    if (!Resources.animations().exportAseprite(animation, json)) {
      return List.of();
    }
    List<Path> files = new ArrayList<>();
    files.add(json);
    Path image = directory.resolve(animation.getSpritesheet().getName() + ".png");
    if (Files.isRegularFile(image)) {
      files.add(image);
    }
    return List.copyOf(files);
  }

  private static List<Path> exportXml(
      Object asset, String name, String extension, Path directory) throws IOException {
    Path target = directory.resolve(safeName(name) + "." + extension);
    XmlUtilities.save(asset, target);
    if (!Files.isRegularFile(target)) {
      throw new IOException("Could not export resource to " + target);
    }
    return List.of(target);
  }

  private static String safeName(String name) {
    String safe = name == null ? "resource" : name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    return safe.isEmpty() || ".".equals(safe) || "..".equals(safe) ? "resource" : safe;
  }
}

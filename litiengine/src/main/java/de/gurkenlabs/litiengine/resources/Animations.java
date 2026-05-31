package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Resource container for {@link Animation} resources.
 *
 * <p>
 * The container can import animations from the JSON format exported by
 * <a href="https://www.aseprite.org/docs/cli/#filename-format">Aseprite</a> and export existing
 * animations back to that format. This makes it possible to author animations in Aseprite and load
 * them directly at runtime via {@link Resources#animations()}.
 * </p>
 *
 * <p>
 * Aseprite exports a sprite sheet image together with a JSON sidecar describing each frame's
 * location and display duration. When loading, the container expects the sprite sheet image to live
 * next to the JSON file (as referenced by the {@code meta.image} field). All frames must have the
 * same width and height; the engine's {@link Spritesheet} model is grid-based and does not support
 * frames of arbitrary sizes.
 * </p>
 */
public final class Animations extends ResourcesContainer<Animation> {
  private static final Logger log = Logger.getLogger(Animations.class.getName());

  /** File extension recognised for Aseprite JSON sidecar files. */
  public static final String ASEPRITE_FILE_EXTENSION = "json";

  Animations() {
  }

  @Override
  protected Animation load(URL resourceName) throws IOException {
    String path = resourceName.toString();
    String ext = FileUtilities.getExtension(path);
    if (!ASEPRITE_FILE_EXTENSION.equalsIgnoreCase(ext)) {
      throw new IOException("Unsupported animation file extension: '" + ext + "'. Expected '" + ASEPRITE_FILE_EXTENSION + "'.");
    }

    AsepriteFormat format = AsepriteFormat.read(resourceName);
    String animationName = FileUtilities.getFileName(path);
    return fromAseprite(format, animationName, resourceName);
  }

  /**
   * Imports an Aseprite-exported animation from the given JSON file.
   *
   * <p>
   * The loaded animation is registered with this container under the file name (without extension)
   * of the JSON file. The associated sprite sheet is registered with {@link Resources#spritesheets()}.
   * </p>
   *
   * @param asepriteJsonPath The path to the Aseprite JSON sidecar file.
   * @return The imported animation.
   * @throws IOException If the file cannot be read or parsed, or if the referenced image cannot be
   *                     loaded.
   */
  public Animation importAseprite(Path asepriteJsonPath) throws IOException {
    AsepriteFormat format = AsepriteFormat.read(asepriteJsonPath);
    String animationName = FileUtilities.getFileName(asepriteJsonPath.getFileName().toString());
    URL baseUrl = asepriteJsonPath.toUri().toURL();
    Animation animation = fromAseprite(format, animationName, baseUrl);
    add(animationName, animation);
    return animation;
  }

  /**
   * Exports the specified animation to the Aseprite JSON format.
   *
   * <p>
   * The sprite sheet image referenced by the animation is written next to the JSON file using the
   * sprite sheet's {@link Spritesheet#getName() name} and the {@code .png} file extension. The
   * resulting {@code .json} file follows the layout produced by the Aseprite CLI when exported with
   * the {@code --format json-hash} option.
   * </p>
   *
   * @param animation     The animation to export.
   * @param destinationJson The path of the JSON file to write.
   * @return {@code true} if the export was successful; {@code false} otherwise.
   */
  public boolean exportAseprite(Animation animation, Path destinationJson) {
    if (animation == null) {
      return false;
    }

    Spritesheet sheet = animation.getSpritesheet();
    if (sheet == null) {
      log.log(Level.WARNING, "Cannot export animation ''{0}'' - no spritesheet attached.", animation.getName());
      return false;
    }

    AsepriteFormat format = toAseprite(animation);

    try {
      Path imagePath = destinationJson.resolveSibling(sheet.getName() + ".png");
      BufferedImage image = sheet.getImage();
      if (image != null) {
        ImageIO.write(image, "png", imagePath.toFile());
      }
      format.write(destinationJson);
      return true;
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Builds an Aseprite JSON model from the given animation.
   *
   * @param animation The animation to describe.
   * @return The Aseprite JSON model.
   */
  public static AsepriteFormat toAseprite(Animation animation) {
    AsepriteFormat result = new AsepriteFormat();
    Spritesheet sheet = animation.getSpritesheet();
    if (sheet == null) {
      return result;
    }

    result.setImage(sheet.getName() + ".png");
    result.setImageWidth(sheet.getImage() != null ? sheet.getImage().getWidth() : sheet.getSpriteWidth() * sheet.getColumns());
    result.setImageHeight(sheet.getImage() != null ? sheet.getImage().getHeight() : sheet.getSpriteHeight() * sheet.getRows());

    int spriteWidth = sheet.getSpriteWidth();
    int spriteHeight = sheet.getSpriteHeight();
    int cols = Math.max(1, sheet.getColumns());

    int[] durations = animation.getKeyFrameDurations();
    for (int i = 0; i < durations.length; i++) {
      int col = i % cols;
      int row = i / cols;
      String frameName = animation.getName() + " " + i + ".png";
      result.getFrames().add(new AsepriteFormat.Frame(
        frameName,
        col * spriteWidth,
        row * spriteHeight,
        spriteWidth,
        spriteHeight,
        durations[i]));
    }
    return result;
  }

  /**
   * Builds an {@link Animation} (and its backing {@link Spritesheet}) from the given Aseprite model.
   *
   * <p>
   * The image referenced by {@link AsepriteFormat#getImage()} is resolved relative to the supplied
   * {@code baseUrl} and loaded via the engine's image resource container.
   * </p>
   *
   * @param format        The Aseprite model to convert.
   * @param animationName The name to assign to the resulting animation.
   * @param baseUrl       The URL used to resolve the referenced image. May be {@code null} if the
   *                      image is referenced by an absolute path.
   * @return The created animation.
   * @throws IOException If the referenced image cannot be loaded or if the frames are not uniform in
   *                     size.
   */
  public static Animation fromAseprite(AsepriteFormat format, String animationName, URL baseUrl) throws IOException {
    List<AsepriteFormat.Frame> frames = format.getFrames();
    if (frames.isEmpty()) {
      throw new IOException("Aseprite document contains no frames.");
    }

    int spriteWidth = frames.get(0).getWidth();
    int spriteHeight = frames.get(0).getHeight();
    for (AsepriteFormat.Frame f : frames) {
      if (f.getWidth() != spriteWidth || f.getHeight() != spriteHeight) {
        throw new IOException(
          "Aseprite frames have inconsistent dimensions; the engine only supports uniform frame sizes.");
      }
    }

    if (format.getImage() == null || format.getImage().isEmpty()) {
      throw new IOException("Aseprite document does not reference a sprite sheet image.");
    }

    URL imageUrl = resolveImageUrl(baseUrl, format.getImage());
    if (imageUrl == null) {
      throw new IOException("Could not resolve sprite sheet image '" + format.getImage()
        + "' referenced by the Aseprite document. Make sure the image file exists next to the JSON file.");
    }

    BufferedImage image;
    try {
      image = Resources.images().get(imageUrl);
    } catch (RuntimeException e) {
      // ResourcesContainer wraps IO failures in ResourceLoadException
      throw new IOException("Could not load sprite sheet image '" + format.getImage()
        + "' referenced by the Aseprite document (resolved to " + imageUrl + ").", e);
    }
    if (image == null) {
      throw new IOException("Could not load sprite sheet image: " + imageUrl);
    }

    String sheetName = FileUtilities.getFileName(format.getImage());
    Spritesheet existing = Resources.spritesheets().get(sheetName);
    Spritesheet sheet = existing != null
      ? existing
      : Resources.spritesheets().load(image, format.getImage(), spriteWidth, spriteHeight);

    int[] durations = new int[frames.size()];
    for (int i = 0; i < frames.size(); i++) {
      durations[i] = frames.get(i).getDuration();
    }

    return new Animation(animationName, sheet, true, durations);
  }

  private static URL resolveImageUrl(URL baseUrl, String image) throws MalformedURLException {
    if (baseUrl == null) {
      URL fromClasspath = Resources.getLocation(image);
      if (fromClasspath != null) {
        return fromClasspath;
      }
      Path local = Path.of(image);
      return Files.exists(local) ? local.toUri().toURL() : null;
    }

    // For file: URLs we resolve the image path against the parent directory of the JSON file using
    // Path APIs - this correctly handles spaces and non-ASCII characters in file names.
    if ("file".equalsIgnoreCase(baseUrl.getProtocol())) {
      try {
        Path basePath = Path.of(baseUrl.toURI());
        Path parent = basePath.getParent();
        Path candidate = parent != null ? parent.resolve(image) : Path.of(image);
        if (Files.exists(candidate)) {
          return candidate.toUri().toURL();
        }
        // fall back to classpath lookup if the sibling file does not exist
        URL fromClasspath = Resources.getLocation(image);
        return fromClasspath != null ? fromClasspath : candidate.toUri().toURL();
      } catch (java.net.URISyntaxException e) {
        throw new MalformedURLException("Invalid base URL: " + baseUrl);
      }
    }

    // generic fallback for non-file URLs (e.g. jar:, http:): naive sibling resolution
    String basePath = baseUrl.toString();
    int slash = basePath.lastIndexOf('/');
    String parent = slash >= 0 ? basePath.substring(0, slash + 1) : basePath;
    return new URL(parent + image);
  }

  /**
   * Convenience method: returns all loaded animations indexed by name.
   *
   * @return An ordered map of loaded animation name to {@link Animation} instance.
   */
  public Map<String, Animation> getAllByName() {
    LinkedHashMap<String, Animation> map = new LinkedHashMap<>();
    for (Map.Entry<String, Animation> e : getResources().entrySet()) {
      map.put(e.getKey(), e.getValue());
    }
    return map;
  }

  /**
   * Convenience overload that loads an Aseprite JSON file from the runtime classpath or file system.
   *
   * @param asepriteJsonPath The path to the Aseprite JSON sidecar file.
   * @return The imported animation.
   * @throws IOException If the file cannot be read or parsed, or if the referenced image cannot be
   *                     loaded.
   */
  public Animation importAseprite(String asepriteJsonPath) throws IOException {
    URL location = Resources.getLocation(asepriteJsonPath);
    if (location == null) {
      throw new IOException("Could not locate Aseprite JSON file: " + asepriteJsonPath);
    }
    AsepriteFormat format = AsepriteFormat.read(location);
    String animationName = FileUtilities.getFileName(asepriteJsonPath);
    Animation animation = fromAseprite(format, animationName, location);
    add(animationName, animation);
    return animation;
  }
}



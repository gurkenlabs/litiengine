package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.io.JsonUtilities;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java representation of the JSON export format produced by <a href="https://www.aseprite.org">Aseprite</a>.
 *
 * <p>
 * The Aseprite CLI can export a sprite sheet image together with a JSON sidecar that describes how
 * each frame of the sprite sheet is laid out and how long it should be displayed. See the
 * <a href="https://www.aseprite.org/docs/cli/#filename-format">Aseprite CLI documentation</a> for
 * details.
 * </p>
 *
 * <p>
 * This class supports both the "hash" (frames as an object map) and the "array" (frames as a JSON
 * array) layouts. When writing, the engine emits the "hash" form, which is the Aseprite default.
 * </p>
 *
 * <p>
 * Serialization is delegated to {@link JsonUtilities}, which is backed by the Jakarta JSON
 * Processing and Jakarta JSON Binding APIs.
 * </p>
 */
public final class AsepriteFormat {

  /**
   * Describes a single rectangular region within an Aseprite sprite sheet.
   */
  public static final class Frame {
    private final String name;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int duration;

    /**
     * Creates a new frame entry.
     *
     * @param name     Name of the frame, typically of the form {@code "<animation> <index>.png"}.
     * @param x        X position of the frame within the sprite sheet, in pixels.
     * @param y        Y position of the frame within the sprite sheet, in pixels.
     * @param width    Width of the frame, in pixels.
     * @param height   Height of the frame, in pixels.
     * @param duration Display duration of the frame, in milliseconds.
     */
    public Frame(String name, int x, int y, int width, int height, int duration) {
      this.name = name;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.duration = duration;
    }

    public String getName() {
      return name;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public int getDuration() {
      return duration;
    }
  }

  private final List<Frame> frames = new ArrayList<>();
  private String image;
  private int imageWidth;
  private int imageHeight;
  private String format = "RGBA8888";
  private String app = "https://litiengine.com";
  private String version = "1.0";

  /** Creates an empty Aseprite format instance. */
  public AsepriteFormat() {
  }

  /**
   * Reads an Aseprite JSON document from the given URL.
   *
   * @param url The URL of the JSON file.
   * @return The parsed Aseprite format.
   * @throws IOException If the file cannot be read or parsed.
   */
  public static AsepriteFormat read(URL url) throws IOException {
    try (InputStream in = url.openStream(); Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      return fromTree(JsonUtilities.readTree(reader));
    } catch (RuntimeException e) {
      throw new IOException("Failed to parse Aseprite JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Reads an Aseprite JSON document from the given path.
   *
   * @param path The path to the JSON file.
   * @return The parsed Aseprite format.
   * @throws IOException If the file cannot be read or parsed.
   */
  public static AsepriteFormat read(Path path) throws IOException {
    try {
      return fromTree(JsonUtilities.readTree(path));
    } catch (RuntimeException e) {
      throw new IOException("Failed to parse Aseprite JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Parses an Aseprite JSON document from the given JSON string.
   *
   * @param json The JSON text.
   * @return The parsed Aseprite format.
   * @throws IOException If the JSON is malformed or does not represent an Aseprite document.
   */
  public static AsepriteFormat parse(String json) throws IOException {
    try (Reader reader = new java.io.StringReader(json)) {
      return fromTree(JsonUtilities.readTree(reader));
    } catch (RuntimeException e) {
      throw new IOException("Failed to parse Aseprite JSON: " + e.getMessage(), e);
    }
  }

  private static AsepriteFormat fromTree(JsonStructure root) throws IOException {
    if (root == null || root.getValueType() != JsonValue.ValueType.OBJECT) {
      throw new IOException("Aseprite JSON root must be an object.");
    }

    JsonObject obj = (JsonObject) root;
    AsepriteFormat result = new AsepriteFormat();

    JsonValue framesNode = obj.get("frames");
    if (framesNode != null && framesNode.getValueType() == JsonValue.ValueType.OBJECT) {
      for (Map.Entry<String, JsonValue> e : framesNode.asJsonObject().entrySet()) {
        result.frames.add(readFrame(e.getKey(), e.getValue()));
      }
    } else if (framesNode != null && framesNode.getValueType() == JsonValue.ValueType.ARRAY) {
      for (JsonValue entry : framesNode.asJsonArray()) {
        if (entry.getValueType() != JsonValue.ValueType.OBJECT) {
          throw new IOException("Aseprite frames array must contain frame objects.");
        }
        JsonObject frameObj = entry.asJsonObject();
        String name = stringOrEmpty(frameObj, "filename");
        result.frames.add(readFrame(name, entry));
      }
    } else if (framesNode != null && framesNode.getValueType() != JsonValue.ValueType.NULL) {
      throw new IOException("Unsupported 'frames' node type in Aseprite JSON.");
    }

    JsonValue metaNode = obj.get("meta");
    if (metaNode != null && metaNode.getValueType() == JsonValue.ValueType.OBJECT) {
      JsonObject meta = metaNode.asJsonObject();
      String image = stringOrNull(meta, "image");
      if (image != null) {
        result.image = image;
      }
      JsonValue sizeNode = meta.get("size");
      if (sizeNode != null && sizeNode.getValueType() == JsonValue.ValueType.OBJECT) {
        JsonObject size = sizeNode.asJsonObject();
        result.imageWidth = intValue(size, "w");
        result.imageHeight = intValue(size, "h");
      }
      String fmt = stringOrNull(meta, "format");
      if (fmt != null) {
        result.format = fmt;
      }
      String app = stringOrNull(meta, "app");
      if (app != null) {
        result.app = app;
      }
      String version = stringOrNull(meta, "version");
      if (version != null) {
        result.version = version;
      }
    }

    return result;
  }

  private static Frame readFrame(String name, JsonValue node) throws IOException {
    if (node == null || node.getValueType() != JsonValue.ValueType.OBJECT) {
      throw new IOException("Aseprite frame entry must be an object.");
    }
    JsonObject frameObj = node.asJsonObject();
    JsonValue rectNode = frameObj.get("frame");
    if (rectNode == null || rectNode.getValueType() != JsonValue.ValueType.OBJECT) {
      throw new IOException("Aseprite frame entry is missing a 'frame' rectangle.");
    }
    JsonObject rect = rectNode.asJsonObject();
    int x = intValue(rect, "x");
    int y = intValue(rect, "y");
    int w = intValue(rect, "w");
    int h = intValue(rect, "h");
    int duration = intValue(frameObj, "duration");
    return new Frame(name, x, y, w, h, duration);
  }

  private static int intValue(JsonObject obj, String key) {
    JsonValue value = obj.get(key);
    if (value == null) {
      return 0;
    }
    return switch (value.getValueType()) {
      case NUMBER -> ((jakarta.json.JsonNumber) value).intValue();
      case STRING -> {
        try {
          yield Integer.parseInt(((JsonString) value).getString());
        } catch (NumberFormatException ignored) {
          yield 0;
        }
      }
      default -> 0;
    };
  }

  private static String stringOrNull(JsonObject obj, String key) {
    JsonValue value = obj.get(key);
    if (value == null || value.getValueType() != JsonValue.ValueType.STRING) {
      return null;
    }
    return ((JsonString) value).getString();
  }

  private static String stringOrEmpty(JsonObject obj, String key) {
    String value = stringOrNull(obj, key);
    return value == null ? "" : value;
  }

  /**
   * Writes this Aseprite document to the given path as a (pretty-printed) JSON file.
   *
   * @param path The destination path.
   * @throws IOException If writing fails.
   */
  public void write(Path path) throws IOException {
    if (JsonUtilities.saveTree(toJsonTree(), path, true) == null) {
      throw new IOException("Could not write Aseprite JSON to " + path);
    }
  }

  /**
   * Writes this Aseprite document as a JSON string.
   *
   * @param pretty Whether to format the output with indentation and line breaks.
   * @return The JSON representation.
   */
  public String writeToString(boolean pretty) {
    return JsonUtilities.writeTreeToString(toJsonTree(), pretty);
  }

  private JsonObject toJsonTree() {
    JsonObjectBuilder root = Json.createObjectBuilder();

    JsonObjectBuilder framesNode = Json.createObjectBuilder();
    for (Frame f : frames) {
      JsonObjectBuilder frameNode = Json.createObjectBuilder();

      JsonObjectBuilder rect = Json.createObjectBuilder()
        .add("x", f.x)
        .add("y", f.y)
        .add("w", f.width)
        .add("h", f.height);
      frameNode.add("frame", rect);

      frameNode.add("rotated", false);
      frameNode.add("trimmed", false);

      JsonObjectBuilder sss = Json.createObjectBuilder()
        .add("x", 0)
        .add("y", 0)
        .add("w", f.width)
        .add("h", f.height);
      frameNode.add("spriteSourceSize", sss);

      JsonObjectBuilder ss = Json.createObjectBuilder()
        .add("w", f.width)
        .add("h", f.height);
      frameNode.add("sourceSize", ss);

      frameNode.add("duration", f.duration);
      framesNode.add(f.name, frameNode);
    }
    root.add("frames", framesNode);

    JsonObjectBuilder meta = Json.createObjectBuilder();
    meta.add("app", app);
    meta.add("version", version);
    if (image != null) {
      meta.add("image", image);
    }
    meta.add("format", format);
    JsonObjectBuilder size = Json.createObjectBuilder()
      .add("w", imageWidth)
      .add("h", imageHeight);
    meta.add("size", size);
    meta.add("scale", "1");
    JsonArrayBuilder emptyArray = Json.createArrayBuilder();
    JsonArray empty = emptyArray.build();
    meta.add("frameTags", empty);
    meta.add("layers", empty);
    meta.add("slices", empty);
    root.add("meta", meta);

    return root.build();
  }

  /** @return The list of frames defined by this document. */
  public List<Frame> getFrames() {
    return frames;
  }

  /** @return The image filename referenced in the document's meta block (may be {@code null}). */
  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  /** @return The width of the referenced sprite sheet image, in pixels. */
  public int getImageWidth() {
    return imageWidth;
  }

  public void setImageWidth(int imageWidth) {
    this.imageWidth = imageWidth;
  }

  /** @return The height of the referenced sprite sheet image, in pixels. */
  public int getImageHeight() {
    return imageHeight;
  }

  public void setImageHeight(int imageHeight) {
    this.imageHeight = imageHeight;
  }

  /** @return The pixel format string from the document's meta block. */
  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}


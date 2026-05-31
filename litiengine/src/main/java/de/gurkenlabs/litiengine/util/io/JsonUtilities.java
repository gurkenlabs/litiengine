package de.gurkenlabs.litiengine.util.io;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Counterpart to {@link XmlUtilities} for working with JSON resources using the
 * <a href="https://jakarta.ee/specifications/jsonb/">Jakarta JSON Binding</a> (JSON-B) and
 * <a href="https://jakarta.ee/specifications/jsonp/">Jakarta JSON Processing</a> (JSON-P) APIs.
 *
 * <p>
 * The class provides two layers of access:
 * </p>
 * <ul>
 *   <li>A binding layer ({@link #read(Class, URL)}, {@link #read(Class, Path)}, {@link #save(Object, Path)})
 *       that maps JSON documents to plain Java objects via JSON-B, mirroring the API offered by
 *       {@link XmlUtilities} for JAXB.</li>
 *   <li>A tree-model layer ({@link #readTree(URL)}, {@link #readTree(Path)}, {@link #saveTree(JsonValue, Path)})
 *       that exposes the underlying {@link JsonStructure} for cases where direct DOM-style access is
 *       preferable to data binding.</li>
 * </ul>
 *
 * <p>
 * Both layers cache their factories and {@link Jsonb} instances per configuration so repeated use
 * does not pay the cost of recreating them.
 * </p>
 */
public final class JsonUtilities {
  private static final Logger log = Logger.getLogger(JsonUtilities.class.getName());

  private static final Map<Boolean, Jsonb> jsonbInstances = new ConcurrentHashMap<>();
  private static final JsonReaderFactory readerFactory =
    Json.createReaderFactory(Collections.emptyMap());
  private static final JsonWriterFactory prettyWriterFactory =
    Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
  private static final JsonWriterFactory compactWriterFactory =
    Json.createWriterFactory(Collections.emptyMap());

  private JsonUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a cached {@link Jsonb} instance configured with the requested formatting.
   *
   * @param pretty Whether the returned instance produces pretty-printed output.
   * @return A shared, thread-safe {@link Jsonb} instance.
   */
  public static Jsonb getJsonb(boolean pretty) {
    return jsonbInstances.computeIfAbsent(pretty, key -> {
      JsonbConfig config = new JsonbConfig().withFormatting(key);
      return JsonbBuilder.create(config);
    });
  }

  /**
   * Reads a JSON document from the given URL and binds it to an instance of the requested class.
   *
   * @param cls  The target class.
   * @param path The URL of the JSON document.
   * @param <T>  The bound type.
   * @return The bound instance, or {@code null} if the document cannot be read.
   */
  public static <T> T read(Class<T> cls, URL path) {
    if (path == null) {
      return null;
    }
    try (InputStream in = path.openStream()) {
      return getJsonb(false).fromJson(in, cls);
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Reads a JSON document from the given path and binds it to an instance of the requested class.
   *
   * @param cls      The target class.
   * @param filePath The path to the JSON document.
   * @param <T>      The bound type.
   * @return The bound instance, or {@code null} if the document cannot be read.
   */
  public static <T> T read(Class<T> cls, Path filePath) {
    if (filePath == null) {
      return null;
    }
    try (InputStream in = Files.newInputStream(filePath)) {
      return getJsonb(false).fromJson(in, cls);
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Reads a JSON document from the given reader and binds it to an instance of the requested class.
   *
   * @param cls    The target class.
   * @param reader The reader providing the JSON content.
   * @param <T>    The bound type.
   * @return The bound instance.
   */
  public static <T> T read(Class<T> cls, Reader reader) {
    return getJsonb(false).fromJson(reader, cls);
  }

  /**
   * Reads the JSON document referenced by the given URL into a {@link JsonStructure} (object or
   * array). Useful when the document does not follow a fixed schema.
   *
   * @param path The URL of the JSON document.
   * @return The parsed structure, or {@code null} if the document cannot be read.
   */
  public static JsonStructure readTree(URL path) {
    if (path == null) {
      return null;
    }
    try (InputStream in = path.openStream(); JsonReader reader = readerFactory.createReader(in, StandardCharsets.UTF_8)) {
      return reader.read();
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Reads the JSON document at the given path into a {@link JsonStructure} (object or array).
   *
   * @param filePath The path to the JSON document.
   * @return The parsed structure, or {@code null} if the document cannot be read.
   */
  public static JsonStructure readTree(Path filePath) {
    if (filePath == null) {
      return null;
    }
    try (InputStream in = Files.newInputStream(filePath); JsonReader reader = readerFactory.createReader(in, StandardCharsets.UTF_8)) {
      return reader.read();
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Reads the JSON document from the given reader into a {@link JsonStructure}.
   *
   * @param reader The reader providing the JSON content.
   * @return The parsed structure.
   */
  public static JsonStructure readTree(Reader reader) {
    try (JsonReader jsonReader = readerFactory.createReader(reader)) {
      return jsonReader.read();
    }
  }

  /**
   * Binds the given object to JSON and writes it to the specified path (pretty-printed).
   *
   * @param object   The object to serialize.
   * @param filePath The destination path.
   * @return The destination path, or {@code null} if the path is {@code null} or writing fails.
   */
  public static Path save(Object object, Path filePath) {
    if (filePath == null) {
      return null;
    }
    try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      getJsonb(true).toJson(object, out);
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
    return filePath;
  }

  /**
   * Binds the given object to JSON and writes it to the specified path, ensuring the path ends with
   * the supplied extension (e.g. {@code "json"} or {@code ".json"}).
   *
   * @param object    The object to serialize.
   * @param path      The destination path.
   * @param extension The file extension (with or without a leading dot).
   * @return The actual destination path used.
   */
  public static Path save(Object object, Path path, String extension) {
    String fullExtension = extension.startsWith(".") ? extension : "." + extension;
    Path fullPath = path;
    if (!fullPath.toString().endsWith(fullExtension)) {
      fullPath = path.resolveSibling(path.getFileName().toString() + fullExtension);
    }
    return save(object, fullPath);
  }

  /**
   * Binds the given object to JSON and writes it to the supplied writer.
   *
   * @param object The object to serialize.
   * @param writer The writer to write the JSON to.
   * @param pretty Whether to pretty-print the output.
   */
  public static void save(Object object, Writer writer, boolean pretty) {
    getJsonb(pretty).toJson(object, writer);
  }

  /**
   * Writes a {@link JsonValue} tree to the specified path.
   *
   * @param value    The JSON value to write.
   * @param filePath The destination path.
   * @param pretty   Whether to pretty-print the output.
   * @return The destination path, or {@code null} if writing fails.
   */
  public static Path saveTree(JsonValue value, Path filePath, boolean pretty) {
    if (filePath == null || value == null) {
      return null;
    }
    JsonWriterFactory factory = pretty ? prettyWriterFactory : compactWriterFactory;
    try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      JsonWriter writer = factory.createWriter(out, StandardCharsets.UTF_8)) {
      writer.write(value);
    } catch (IOException | RuntimeException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
    return filePath;
  }

  /**
   * Convenience overload that pretty-prints by default.
   *
   * @param value    The JSON value to write.
   * @param filePath The destination path.
   * @return The destination path.
   */
  public static Path saveTree(JsonValue value, Path filePath) {
    return saveTree(value, filePath, true);
  }

  /**
   * Serializes the given JSON value to a string.
   *
   * @param value  The JSON value to serialize.
   * @param pretty Whether to pretty-print the output.
   * @return The serialized JSON string.
   */
  public static String writeTreeToString(JsonValue value, boolean pretty) {
    JsonWriterFactory factory = pretty ? prettyWriterFactory : compactWriterFactory;
    java.io.StringWriter sw = new java.io.StringWriter();
    try (JsonWriter writer = factory.createWriter(sw)) {
      writer.write(value);
    }
    return sw.toString();
  }
}


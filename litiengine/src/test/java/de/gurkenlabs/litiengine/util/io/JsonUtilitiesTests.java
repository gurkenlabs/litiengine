package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonUtilitiesTests {

  public static class Sample {
    public String name;
    public int value;

    public Sample() {
    }

    public Sample(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }

  @Test
  void getJsonbReturnsCachedInstancePerFormatting() {
    assertSame(JsonUtilities.getJsonb(true), JsonUtilities.getJsonb(true));
    assertSame(JsonUtilities.getJsonb(false), JsonUtilities.getJsonb(false));
  }

  @Test
  void bindingRoundTrip(@TempDir Path tempDir) {
    Path file = tempDir.resolve("sample.json");
    Sample saved = new Sample("hello", 42);
    Path actualPath = JsonUtilities.save(saved, file);
    assertEquals(file, actualPath);
    assertTrue(Files.exists(file));

    Sample loaded = JsonUtilities.read(Sample.class, file);
    assertNotNull(loaded);
    assertEquals("hello", loaded.name);
    assertEquals(42, loaded.value);
  }

  @Test
  void saveAppendsExtensionWhenMissing(@TempDir Path tempDir) {
    Path base = tempDir.resolve("entry");
    Path actual = JsonUtilities.save(new Sample("x", 1), base, "json");
    assertEquals(tempDir.resolve("entry.json"), actual);
    assertTrue(Files.exists(actual));
  }

  @Test
  void treeRoundTrip(@TempDir Path tempDir) {
    JsonObject tree = Json.createObjectBuilder()
      .add("a", 1)
      .add("b", "two")
      .add("c", Json.createArrayBuilder().add(true).add(false))
      .build();

    Path file = tempDir.resolve("tree.json");
    assertEquals(file, JsonUtilities.saveTree(tree, file, true));

    JsonStructure loaded = JsonUtilities.readTree(file);
    assertNotNull(loaded);
    JsonObject obj = loaded.asJsonObject();
    assertEquals(1, obj.getInt("a"));
    assertEquals("two", obj.getString("b"));
    assertEquals(2, obj.getJsonArray("c").size());
  }

  @Test
  void readTreeFromReader() throws IOException {
    try (StringReader reader = new StringReader("{\"x\":7}")) {
      JsonStructure tree = JsonUtilities.readTree(reader);
      assertNotNull(tree);
      assertEquals(7, tree.asJsonObject().getInt("x"));
    }
  }

  @Test
  void writeTreeToStringRespectsFormatting() {
    JsonObject tree = Json.createObjectBuilder().add("k", 1).build();
    String compact = JsonUtilities.writeTreeToString(tree, false);
    String pretty = JsonUtilities.writeTreeToString(tree, true);
    assertEquals("{\"k\":1}", compact);
    assertTrue(pretty.contains("\n"));
    assertTrue(pretty.contains("\"k\""));
  }

  @Test
  void readReturnsNullOnMissingFile(@TempDir Path tempDir) {
    Path missing = tempDir.resolve("does-not-exist.json");
    assertNull(JsonUtilities.read(Sample.class, missing));
    assertNull(JsonUtilities.readTree(missing));
  }
}


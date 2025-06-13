package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled(
    value = "Disabled for now due to Windows/Linux incompatiblities on serialization of the line endings.")
class TmxCompatibilityTests {

  @Test
  void infiniteMapNeedsToBeCompatibleToDefaultTmxFormat() {
    Path source = Path.of("de", "gurkenlabs", "litiengine", "environment", "tilemap", "xml", "test-infinite-map.tmx");
    Path target = Path.of("de", "gurkenlabs", "litiengine", "environment", "tilemap", "xml", "test-map-out.tmx");

    IMap map = Resources.maps().get(source.toString());
    XmlUtilities.save(map, target);

    String original = Resources.read(source.toString());
    String output = Resources.read(target.toString());

    assertEquals(original, output);
  }

  @Test
  void mapObjectMapNeedsToBeCompatibleToDefaultTmxFormat() {
    Path source = Path.of("de", "gurkenlabs", "litiengine", "environment", "tilemap", "xml", "test-mapobject.tmx");
    Path target = Path.of("de", "gurkenlabs", "litiengine", "environment", "tilemap", "xml", "test-map-out.tmx");
    IMap map = Resources.maps().get(source.toString());
    XmlUtilities.save(map, target);

    String original = Resources.read(source.toString());
    String output = Resources.read(target.toString());

    assertEquals(original, output);
  }
}

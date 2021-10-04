package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled(
    value =
        "Disabled for now due to Windows/Linux incompatiblities on serialization of the line endings.")
public class TmxCompatibilityTests {
  @Test
  public void infiniteMapNeedsToBeCompatibleToDefaultTmxFormat() {

    String source = "de/gurkenlabs/litiengine/environment/tilemap/xml/test-infinite-map.tmx";
    String target = "de/gurkenlabs/litiengine/environment/tilemap/xml/test-map-out.tmx";
    IMap map = Resources.maps().get(source);
    XmlUtilities.save(map, target);

    String original = Resources.read(source);
    String output = Resources.read(target);

    assertEquals(original, output);
  }

  @Test
  public void mapObjectMapNeedsToBeCompatibleToDefaultTmxFormat() {

    String source = "de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx";
    String target = "de/gurkenlabs/litiengine/environment/tilemap/xml/test-map-out.tmx";
    IMap map = Resources.maps().get(source);
    XmlUtilities.save(map, target);

    String original = Resources.read(source);
    String output = Resources.read(target);

    assertEquals(original, output);
  }
}

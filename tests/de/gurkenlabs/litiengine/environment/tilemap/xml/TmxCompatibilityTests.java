package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@Disabled(value="Disabled for now due to Windows/Linux incompatiblities on serialization of the line endings.")
public class TmxCompatibilityTests {
  @Test
  public void infiniteMapNeedsToBeCompatibleToDefaultTmxFormat() {
    
    String source = "tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-infinite-map.tmx";
    String target = "tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map-out.tmx";
    IMap map = Resources.maps().get(source);
    XmlUtilities.save(map, target);

    String original = Resources.read(source);
    String output = Resources.read(target);

    assertEquals(original, output);
  }
  
  @Test
  public void mapObjectMapNeedsToBeCompatibleToDefaultTmxFormat() {
    
    String source = "tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx";
    String target = "tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map-out.tmx";
    IMap map = Resources.maps().get(source);
    XmlUtilities.save(map, target);

    String original = Resources.read(source);
    String output = Resources.read(target);

    assertEquals(original, output);
  }
}

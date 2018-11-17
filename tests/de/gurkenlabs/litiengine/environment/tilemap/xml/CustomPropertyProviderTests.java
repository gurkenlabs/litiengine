package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class CustomPropertyProviderTests {

  @Test
  public void testSetCustomProperty() {
    @SuppressWarnings("serial")
    CustomPropertyProvider propProvider = new CustomPropertyProvider() {};
    propProvider.setValue("test", "testvalue");

    assertEquals("testvalue", propProvider.getStringValue("test"));
    assertNull(propProvider.getStringValue("test2"));
    assertEquals(1, propProvider.getProperties().size());

    propProvider.setValue("test", "testvalue2");

    assertEquals("testvalue2", propProvider.getStringValue("test"));

    ArrayList<Property> props = new ArrayList<>();
    props.add(new Property("test2", "testvalue3"));
    props.add(new Property("test3", "testvalue4"));

    propProvider.setProperties(props);

    assertEquals(2, propProvider.getProperties().size());
    assertEquals("testvalue3", propProvider.getStringValue("test2"));
    assertEquals("testvalue4", propProvider.getStringValue("test3"));

    propProvider.setProperties(null);
    assertNotNull(propProvider.getProperties());
    assertEquals(0, propProvider.getProperties().size());
  }
}

package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Hashtable;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;

public class CustomPropertyProviderTests {

  @Test
  public void testSetCustomProperty() {
    @SuppressWarnings("serial")
    CustomPropertyProvider propProvider = new CustomPropertyProvider() {};
    propProvider.setValue("test", "testvalue");

    assertEquals("testvalue", propProvider.getStringValue("test"));
    assertFalse(propProvider.hasCustomProperty("test2"));
    assertEquals(1, propProvider.getProperties().size());

    propProvider.setValue("test", "testvalue2");

    assertEquals("testvalue2", propProvider.getStringValue("test"));

    java.util.Map<String, ICustomProperty> props = new Hashtable<>(2);
    props.put("test2", new CustomProperty("testvalue3"));
    props.put("test3", new CustomProperty("testvalue4"));

    propProvider.setProperties(props);

    assertEquals(2, propProvider.getProperties().size());
    assertEquals("testvalue3", propProvider.getStringValue("test2"));
    assertEquals("testvalue4", propProvider.getStringValue("test3"));

    propProvider.setProperties(null);
    assertNotNull(propProvider.getProperties());
    assertEquals(0, propProvider.getProperties().size());
  }
}

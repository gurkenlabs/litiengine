package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomPropertyAdapterTests {

  private CustomPropertyAdapter adapter;

  @BeforeEach
  public void setUp() {
    adapter = new CustomPropertyAdapter();
  }

  @Test
  void testUnmarshal() {
    // arrange
    CustomPropertyAdapter.Property property =
        new CustomPropertyAdapter.Property("PropertyA", "float");
    property.value = "42.0f";
    CustomPropertyAdapter.PropertyList propertyList =
        new CustomPropertyAdapter.PropertyList(Collections.singletonList(property));

    // act
    Map<String, ICustomProperty> unmarshal = adapter.unmarshal(propertyList);

    // assert
    assertNotNull(unmarshal.get("PropertyA"));
  }

  @Test
  void testUnmarshalLocation() throws MalformedURLException {
    // arrange
    CustomPropertyAdapter.Property property =
        new CustomPropertyAdapter.Property("PropertyA", "float");
    property.value = "42.0f";
    property.location = new URL("http://localhost/");
    CustomPropertyAdapter.PropertyList propertyList =
        new CustomPropertyAdapter.PropertyList(Collections.singletonList(property));

    // act
    Map<String, ICustomProperty> unmarshal = adapter.unmarshal(propertyList);

    // assert
    assertEquals("http://localhost/", unmarshal.get("PropertyA").getAsString());
  }

  @Test
  void testMarshal() {
    // arrange
    Map<String, ICustomProperty> properties = new HashMap<>();
    CustomProperty customProperty = new CustomProperty(CustomPropertyType.STRING, "value");
    properties.putIfAbsent("PropertyA", customProperty);

    // act
    CustomPropertyAdapter.PropertyList marshal = adapter.marshal(properties);

    // assert
    assertEquals("value", marshal.properties.get(0).value);
  }

  @Test
  void testMarshalWithLinebreakInValue() {
    // arrange
    Map<String, ICustomProperty> properties = new HashMap<>();
    CustomProperty customProperty = new CustomProperty(CustomPropertyType.STRING, "value\n");
    properties.putIfAbsent("PropertyA", customProperty);

    // act
    CustomPropertyAdapter.PropertyList marshal = adapter.marshal(properties);

    // assert
    assertEquals("value\n", marshal.properties.get(0).contents);
    assertNull(marshal.properties.get(0).value);
  }

  @Test
  void testMarshalSort() {
    // arrange
    Map<String, ICustomProperty> properties = new HashMap<>();
    CustomProperty customProperty1 = new CustomProperty(CustomPropertyType.STRING, "value1");
    CustomProperty customProperty2 = new CustomProperty(CustomPropertyType.STRING, "value2");
    CustomProperty customProperty3 = new CustomProperty(CustomPropertyType.STRING, "value3");
    properties.putIfAbsent("PropertyB", customProperty2);
    properties.putIfAbsent("PropertyA", customProperty1);
    properties.putIfAbsent("PropertyC", customProperty3);

    // act
    CustomPropertyAdapter.PropertyList marshal = adapter.marshal(properties);

    // assert
    assertEquals("value1", marshal.properties.get(0).value);
    assertEquals("value2", marshal.properties.get(1).value);
    assertEquals("value3", marshal.properties.get(2).value);
  }
}

package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import java.util.Hashtable;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomPropertyProviderTests {

  private CustomPropertyProvider provider;

  @BeforeEach
  public void setUp() {
    provider = new CustomPropertyProvider();
  }

  @Test
  void testSetProperty() {
    // arrange
    String value = "value";

    // act
    provider.setValue("test", value);

    // assert
    assertEquals(value, provider.getStringValue("test"));
  }

  @Test
  void testHasCustomProperty() {
    // arrange
    provider.setValue("test1", "value");

    // act
    boolean hasProperty = provider.hasCustomProperty("test1");

    // assert
    assertTrue(hasProperty);
  }

  @Test
  void testGetProperties() {
    // arrange
    provider.setValue("test1", "value1");
    provider.setValue("test2", "value2");

    // act
    Map<String, ICustomProperty> properties = provider.getProperties();

    // assert
    assertEquals(2, properties.size());
  }

  @Test
  void testSetProperties() {
    // arrange
    Map<String, ICustomProperty> props = new Hashtable<>(2);
    props.put("test1", new CustomProperty("value1"));
    props.put("test2", new CustomProperty("value2"));

    // act
    provider.setProperties(props);

    assertEquals(props, provider.getProperties());
  }

  @Test
  void testSetPropertiesNull() {
    // arrange
    Map<String, ICustomProperty> props = new Hashtable<>(2);
    props.put("test42", new CustomProperty("value42"));

    // act
    provider.setProperties(null);

    // assert
    assertEquals(0, provider.getProperties().size());
  }
}

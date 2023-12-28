package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;

import java.awt.*;
import java.util.Hashtable;
import java.util.Map;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
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

  @Test
  void testCustomPropertyTypes() {
    provider.setValue("mybool", true);
    provider.setValue("mychar", 'o');
    provider.setValue("myint", 111);
    provider.setValue("mybyte", (byte)111);
    provider.setValue("myshort", (short)111);
    provider.setValue("mylong", 111L);
    provider.setValue("myfloat", 111.0f);
    provider.setValue("mydouble", 111.0);
    provider.setValue("mycolor", Color.BLACK);

    var mapObjectMock = mock(IMapObject.class);
    when(mapObjectMock.getId()).thenReturn(123);
    provider.setValue("myobject", mapObjectMock);

    assertTrue(provider.getBoolValue("mybool"));
    assertEquals('o', provider.getCharValue("mychar"));
    assertEquals(111, provider.getIntValue("myint"));
    assertEquals((byte)111, provider.getByteValue("mybyte"));
    assertEquals((short)111, provider.getShortValue("myshort"));
    assertEquals(111L, provider.getLongValue("mylong"));
    assertEquals(111.0f, provider.getFloatValue("myfloat"));
    assertEquals(111.0, provider.getDoubleValue("mydouble"));
    assertEquals(Color.BLACK, provider.getColorValue("mycolor"));

    assertEquals(123, provider.getMapObjectId("myobject"));
  }
}

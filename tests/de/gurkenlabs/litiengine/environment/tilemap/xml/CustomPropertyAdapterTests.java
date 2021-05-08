package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CustomPropertyAdapterTests {

    private CustomPropertyAdapter adapter;

    @BeforeEach
    public void setUp(){
        adapter = new CustomPropertyAdapter();
    }

    @Test
    public void testUnmarshal(){
        // arrange
        CustomPropertyAdapter.Property property = new CustomPropertyAdapter.Property("PropertyA", "float");
        property.value = "42.0f";
        CustomPropertyAdapter.PropertyList propertyList = new CustomPropertyAdapter.PropertyList(Collections.singletonList(property));

        // act
        Map<String, ICustomProperty> unmarshal = adapter.unmarshal(propertyList);

        // assert
        assertNotNull(unmarshal.get("PropertyA"));
    }

    @Test
    public void testUnmarshalLocation() throws MalformedURLException {
        // arrange
        CustomPropertyAdapter.Property property = new CustomPropertyAdapter.Property("PropertyA", "float");
        property.value = "42.0f";
        property.location = new URL("http://localhost/");
        CustomPropertyAdapter.PropertyList propertyList = new CustomPropertyAdapter.PropertyList(Collections.singletonList(property));

        // act
        Map<String, ICustomProperty> unmarshal = adapter.unmarshal(propertyList);

        // assert
        assertEquals("http://localhost/", unmarshal.get("PropertyA").getAsString());
    }

    @Test
    public void testMarshal(){
        // arrange
        Map<String, ICustomProperty> properties = new HashMap<>();
        CustomProperty customProperty = new CustomProperty("string", "value");
        properties.putIfAbsent("PropertyA", customProperty);

        // act
        CustomPropertyAdapter.PropertyList marshal = adapter.marshal(properties);

        // assert
        assertEquals("value", marshal.properties.get(0).value);
    }

    @Test
    public void testMarshalWithLinebreakInValue(){
        // arrange
        Map<String, ICustomProperty> properties = new HashMap<>();
        CustomProperty customProperty = new CustomProperty("string", "value\n");
        properties.putIfAbsent("PropertyA", customProperty);

        // act
        CustomPropertyAdapter.PropertyList marshal = adapter.marshal(properties);

        // assert
        assertEquals("value\n", marshal.properties.get(0).contents);
        assertNull(marshal.properties.get(0).value);
    }

    @Test
    public void testMarshalSort(){
        // arrange
        Map<String, ICustomProperty> properties = new HashMap<>();
        CustomProperty customProperty1 = new CustomProperty("string", "value1");
        CustomProperty customProperty2 = new CustomProperty("string", "value2");
        CustomProperty customProperty3 = new CustomProperty("string", "value3");
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

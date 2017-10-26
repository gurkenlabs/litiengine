package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.ArrayList;

import org.junit.Test;

import junit.framework.Assert;

public class CustomPropertyProviderTests {

  @Test
  public void testSetCustomProperty() {
    CustomPropertyProvider propProvider = new CustomPropertyProvider();
    propProvider.setCustomProperty("test", "testvalue");

    Assert.assertEquals("testvalue", propProvider.getCustomProperty("test"));
    Assert.assertNull(propProvider.getCustomProperty("test2"));
    Assert.assertEquals(1, propProvider.getAllCustomProperties().size());

    propProvider.setCustomProperty("test", "testvalue2");

    Assert.assertEquals("testvalue2", propProvider.getCustomProperty("test"));

    ArrayList<Property> props = new ArrayList<>();
    props.add(new Property("test2", "testvalue3"));
    props.add(new Property("test3", "testvalue4"));

    propProvider.setCustomProperties(props);

    Assert.assertEquals(2, propProvider.getAllCustomProperties().size());
    Assert.assertEquals("testvalue3", propProvider.getCustomProperty("test2"));
    Assert.assertEquals("testvalue4", propProvider.getCustomProperty("test3"));

    propProvider.setCustomProperties(null);
    Assert.assertNotNull(propProvider.getAllCustomProperties());
    Assert.assertEquals(0, propProvider.getAllCustomProperties().size());
  }
}

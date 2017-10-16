package de.gurkenlabs.litiengine.environment.tilemap.xml;

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
  }
}

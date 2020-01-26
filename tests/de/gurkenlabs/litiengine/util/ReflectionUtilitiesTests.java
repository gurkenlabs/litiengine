package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ReflectionUtilitiesTests {
  @Test
  public void testGetField() {
    assertNotNull(ReflectionUtilities.getField(TestImpl.class, "integerField"));
    assertNotNull(ReflectionUtilities.getField(ChildImpl.class, "integerField"));
    assertNull(ReflectionUtilities.getField(TestImpl.class, "nananananan"));
  }

  private class TestImpl {
    @SuppressWarnings("unused")
    private int integerField;
  }
  
  private class ChildImpl extends TestImpl{
  }
}

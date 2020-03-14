package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.util.io.Serializer;

public class SerializerTests {

  @Test
  public void testSerialization() {
    SerializerTestClass test = new SerializerTestClass();
    test.test = 123;
    test.otherTest = true;
    test.anotherTest = "test";
    
    byte[] serialized = Serializer.serialize(test);
    SerializerTestClass deserialized = (SerializerTestClass) Serializer.deserialize(serialized);
    
    assertEquals(123, deserialized.test);
    assertEquals(true, deserialized.otherTest);
    assertEquals("test", deserialized.anotherTest);
  }
  
  public static class SerializerTestClass implements Serializable {
    private static final long serialVersionUID = 1L;
    int test;
    boolean otherTest;
    String anotherTest;
  }
}

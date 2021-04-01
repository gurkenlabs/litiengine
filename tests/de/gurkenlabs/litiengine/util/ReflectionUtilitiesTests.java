package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionUtilitiesTests {
  @Test
  public void testGetField() {
    assertNotNull(ReflectionUtilities.getField(TestImpl.class, "integerField"));
    assertNotNull(ReflectionUtilities.getField(ChildImpl.class, "integerField"));
    assertNull(ReflectionUtilities.getField(TestImpl.class, "nananananan"));
  }

  @ParameterizedTest
  @MethodSource("getWrapperParameters")
  public void testIsWrapperTypeTrue(Class<?> primitive, Class<?> wrapper){
    // act
    boolean isWrapper = ReflectionUtilities.isWrapperType(primitive, wrapper);

    // assert
    assertTrue(isWrapper);
  }

  @ParameterizedTest
  @MethodSource("getNonWrapperParameters")
  public void testIsWrapperTypeFalse(Class<?> primitive, Class<?> wrapper){
    // act
    boolean isWrapper = ReflectionUtilities.isWrapperType(primitive, wrapper);

    // assert
    assertFalse(isWrapper);
  }

  private static Stream<Arguments> getWrapperParameters(){
    // arrange
    return Stream.of(
            Arguments.of(boolean.class, Boolean.class),
            Arguments.of(char.class, Character.class),
            Arguments.of(byte.class, Byte.class),
            Arguments.of(short.class, Short.class),
            Arguments.of(int.class, Integer.class),
            Arguments.of(long.class, Long.class),
            Arguments.of(float.class, Float.class),
            Arguments.of(double.class, Double.class),
            Arguments.of(void.class, Void.class)
    );
  }

  private static Stream<Arguments> getNonWrapperParameters(){
    // arrange
    return Stream.of(
            Arguments.of(Boolean.class, boolean.class),
            Arguments.of(char.class, Byte.class)
    );
  }

  private class TestImpl {
    @SuppressWarnings("unused")
    private int integerField;
  }
  
  private class ChildImpl extends TestImpl{
  }
}

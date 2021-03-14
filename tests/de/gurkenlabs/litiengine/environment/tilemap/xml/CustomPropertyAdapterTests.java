package de.gurkenlabs.litiengine.environment.tilemap.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomPropertyAdapterTests {

    @Test
    public void testCompareTo(){
        // arrange
        CustomPropertyAdapter.Property propertyWithNameA = new CustomPropertyAdapter.Property("PropertyA", "float");
        CustomPropertyAdapter.Property propertyWithNameB = new CustomPropertyAdapter.Property("PropertyB", "float");

        // act
        int result1 = propertyWithNameA.compareTo(propertyWithNameB);
        int result2 = propertyWithNameB.compareTo(propertyWithNameA);
        int result3 = propertyWithNameA.compareTo(propertyWithNameA);

        // assert
        assertEquals(-1, result1);
        assertEquals(1, result2);
        assertEquals(0, result3);
    }

    @Test
    public void testCompareToNull(){
        // arrange
        CustomPropertyAdapter.Property propertyWithName = new CustomPropertyAdapter.Property("PropertyA", "float");
        CustomPropertyAdapter.Property propertyNullNameNoType = new CustomPropertyAdapter.Property(null, "");

        // act
        int result1 = propertyWithName.compareTo(null);
        int result2 = propertyNullNameNoType.compareTo(propertyWithName);
        int result3 = propertyNullNameNoType.compareTo(propertyNullNameNoType);

        // assert
        assertEquals(1, result1);
        assertEquals(-1, result2);
        assertEquals(0, result3);
    }
}

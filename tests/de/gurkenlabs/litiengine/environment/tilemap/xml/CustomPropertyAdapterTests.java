package de.gurkenlabs.litiengine.environment.tilemap.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomPropertyAdapterTests {

    @Test
    public void testPropertyComparison(){
        CustomPropertyAdapter.Property propertyWithNameA = new CustomPropertyAdapter.Property("PropertyA", "float");
        CustomPropertyAdapter.Property propertyWithNameB = new CustomPropertyAdapter.Property("PropertyB", "float");
        CustomPropertyAdapter.Property propertyNullNameA = new CustomPropertyAdapter.Property(null, "");
        CustomPropertyAdapter.Property propertyNullNameB = new CustomPropertyAdapter.Property(null, "");

        assertEquals(1, propertyWithNameA.compareTo(null));
        assertEquals(0, propertyNullNameA.compareTo(propertyNullNameB));
        assertEquals(-1, propertyNullNameA.compareTo(propertyWithNameA));

        assertEquals(-1, propertyWithNameA.compareTo(propertyWithNameB));
        assertEquals(1, propertyWithNameB.compareTo(propertyWithNameA));
        assertEquals(0, propertyWithNameA.compareTo(propertyWithNameA));
    }
}

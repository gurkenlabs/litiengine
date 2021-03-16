package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;



import java.util.Comparator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle. Updated by David Koelle in 2017.
 *
 */
public class AlphanumComparatorTests implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return 0;
    }

    @Test
    public void testCompareTo_NULL() {
        String s1 = null;
        String s2 = null;

        assertEquals(0, AlphanumComparator.compareTo(s1, s2));
    }

    @Test
    public void testCompareTo_EmptyString() {
        String s1 = "";
        String s2 = "";

        assertEquals(0, AlphanumComparator.compareTo(s1, s2));
    }

    @Test
    public void testCompareTo_NumericCharacters() {
        String s1 = "123";
        String s2 = "123";
        String s3 = "a456";
        String s4 = "456";

        assertEquals(0, AlphanumComparator.compareTo(s1, s2));
        assertEquals(-48, AlphanumComparator.compareTo(s1, s3));
        assertEquals(-3, AlphanumComparator.compareTo(s1, s4));
    }

    @Test
    public void testCompareTo_Strings() {
        String s1 = "test";
        String s2 = "test";
        String s3 = "Atest";

        assertEquals(0, AlphanumComparator.compareTo(s1, s2));
        assertEquals(51, AlphanumComparator.compareTo(s1, s3));
    }

}




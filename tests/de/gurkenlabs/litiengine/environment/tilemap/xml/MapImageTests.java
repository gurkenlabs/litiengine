package de.gurkenlabs.litiengine.environment.tilemap.xml;

import org.junit.jupiter.api.Test;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MapImageTests {

    /**
     * Tests the copy constructor and verifies that there are no side effects.
     */
    @Test
    public void testCopyConstructor() {
        MapImage original = new MapImage();
        MapImage copy = new MapImage(original);

        verifyUnmodified(copy);

        // Change original, verify it does not affect the copy
        original.setSource("source1");
        original.setAbsoluteSourcePath("abs1");
        original.setTransparentColor(Color.GREEN);
        original.setHeight(10);
        original.setWidth(10);

        verifyUnmodified(copy);

        // Change the copy, verify it does not affect the original
        copy.setSource("source2");
        copy.setAbsoluteSourcePath("abs2");
        copy.setTransparentColor(Color.RED);
        copy.setHeight(20);
        copy.setWidth(20);

        assertEquals("source1", original.getSource());
        assertEquals("abs1", original.getAbsoluteSourcePath());
        assertEquals(Color.GREEN, original.getTransparentColor());
        assertEquals(10, original.getWidth());
        assertEquals(10, original.getHeight());
    }

    /**
     * Helper method to verify that a newly created map image is valid.
     */
    public static void verifyUnmodified(MapImage image){
        assertNull(image.getSource());
        assertNull(image.getAbsoluteSourcePath());
        assertNull(image.getTransparentColor());
        assertEquals(0, image.getWidth());
        assertEquals(0, image.getHeight());
    }
}

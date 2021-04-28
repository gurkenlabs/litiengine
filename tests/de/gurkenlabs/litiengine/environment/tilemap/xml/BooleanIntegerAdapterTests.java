package de.gurkenlabs.litiengine.environment.tilemap.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BooleanIntegerAdapterTests {

    @Test
    public void testUnmarshallTrue() {
        BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
        assertTrue(booleanIntegerAdapter.unmarshal(1));
    }

    @Test
    public void testUnmarshallNull() {
        BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
        assertNull(booleanIntegerAdapter.unmarshal(null));
    }


    @Test
    public void testMarshallNull() {
        BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
        assertNull(booleanIntegerAdapter.marshal(null));
    }

    @Test
    public void testMarshallTrue() {
        BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
        assertEquals(1, booleanIntegerAdapter.marshal(true));
    }

    @Test
    public void testMarshallFalse() {
        BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
        assertEquals(0,booleanIntegerAdapter.marshal(false));
    }

}

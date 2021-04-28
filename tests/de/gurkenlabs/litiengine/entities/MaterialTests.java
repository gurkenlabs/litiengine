package de.gurkenlabs.litiengine.entities;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaterialTests {

    @Test
    public void testEmptyValue(){
        assertEquals(Material.UNDEFINED, Material.get(""));

    }

    @Test
    public void testNullValue() {
        assertEquals(Material.UNDEFINED, Material.get(null));
    }
}

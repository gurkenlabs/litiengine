package de.gurkenlabs.litiengine.resources;

import org.junit.jupiter.api.Test;


import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FontsTests {
    @Test
    public void get_SizeStyleNegative() {
        // arrange
        Font testFont = new Font("test", Font.BOLD, 1);
        Fonts fonts = new Fonts();
        fonts.add("test", testFont);

        // act
        Font result = fonts.get("test", -3, -3f);

        // assert
        assertNotNull(result);
        assertEquals(-2, result.getSize()); // TODO: this is kinda useless; set negative size to at least 0 in get()?
        assertEquals(0, result.getStyle());
    }

    @Test
    public void get_SizeStyleZero() {
        // arrange
        Font testFont = new Font("test", Font.BOLD, 1);
        Fonts fonts = new Fonts();
        fonts.add("test", testFont);

        // act
        Font result = fonts.get("test", Font.PLAIN, 0f);

        // assert
        assertNotNull(result);
        assertEquals(Font.PLAIN, result.getStyle());
        assertEquals(0, result.getSize());
    }

    @Test
    public void get_SizeStylePositive() {
        // arrange
        Font testFont = new Font("test", Font.PLAIN, 1);
        Fonts fonts = new Fonts();
        fonts.add("test", testFont);

        // act
        Font result = fonts.get("test", Font.ITALIC, 2f);

        // assert
        assertNotNull(result);
        assertEquals(Font.ITALIC, result.getStyle());
        assertEquals(2, result.getSize());
    }
}

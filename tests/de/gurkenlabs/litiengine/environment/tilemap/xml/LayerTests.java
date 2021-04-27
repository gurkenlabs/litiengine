package de.gurkenlabs.litiengine.environment.tilemap.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LayerTests {

    private Layer layer;

    @BeforeEach
    public void setUp(){
        // arrange
        layer = new TestLayer();
    }

    @Test
    public void testResetOffsetX_isReset(){
        // act
        layer.resetOffsetX();

        // assert
        assertEquals(0, layer.getOffsetX());
    }

    @Test
    public void testResetOffsetY_isReset(){
        // act
        layer.resetOffsetY();

        // assert
        assertEquals(0, layer.getOffsetY());
    }

    @ParameterizedTest(name = "testResetWidth_{0}, initialWidth={1}, expectedWidth={2}")
    @CsvSource({
            "'not-reset', 29, 29",
            "'reset', 0, 0"
    })
    public void testResetWidth(String caption, int initialWidth, int expectedWidth){
        // arrange
        layer.setWidth(initialWidth);

        // act
        layer.resetWidth();

        // assert
        assertEquals(expectedWidth, layer.getWidth());
    }

    @ParameterizedTest(name = "testResetHeight_{0}, initialHeight={1}, expectedHeight={2}")
    @CsvSource({
            "'not-reset', 42, 42",
            "'reset', 0, 0"
    })
    public void testResetHeight(String caption, int initialHeight, int expectedHeight){
        // arrange
        layer.setHeight(initialHeight);

        // act
        layer.resetHeight();

        // assert
        assertEquals(expectedHeight, layer.getHeight());
    }

    @ParameterizedTest(name = "testResetOpacity_{0}, initialOpacity={1}, expectedOpacity={2}")
    @CsvSource({
            "'not-reset', 42, 42",
            "'reset', 1.0f, 1.0f"
    })
    public void testResetOpacity(String caption, float initialOpacity, float expectedOpacity){
        // arrange
        layer.setOpacity(initialOpacity);

        // act
        layer.resetOpacity();

        // assert
        assertEquals(expectedOpacity, layer.getOpacity());
    }

    @ParameterizedTest(name = "testIsResettable_Integer, value={0}, expected={1}")
    @CsvSource({
            "42, false",
            ", false",
            "0, true"
    })
    public void testIsResettable_Integer(Integer value, boolean expected){
        // act
        boolean isResettable = layer.isResettable(value);

        // assert
        assertEquals(expected, isResettable);
    }

    @ParameterizedTest(name = "testIsResettable_Float, value={0}, expected={1}")
    @CsvSource({
            "42.0f, false",
            ", false",
            "1.0f, true"
    })
    public void testIsResettable_Float(Float value, boolean expected){
        // act
        boolean isResettable = layer.isResettable(value);

        // assert
        assertEquals(expected, isResettable);
    }

    private class TestLayer extends Layer{}
}

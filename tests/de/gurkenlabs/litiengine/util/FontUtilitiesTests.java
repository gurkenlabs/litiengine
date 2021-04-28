package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FontUtilitiesTests {

    @ParameterizedTest(name = "testGetFallbackFontIfNecessary text={0} expectedFont={1}")
    @CsvSource({
            "\uF061\uF072, Wingdings",
            "ထ, Arial"
    })
    public void testGetFallbackFontIfNecessary(String text, String expectedFont){
        // arrange
        float textSize = 20.0f;
        Font primaryFont = new Font("Wingdings", Font.BOLD, 12);
        Font primaryFontSpy = spy(primaryFont);
        when(primaryFontSpy.canDisplayUpTo("\uF061\uF072")).thenReturn(-1);
        when(primaryFontSpy.canDisplayUpTo("ထ")).thenReturn(0);

        Font fallbackFont = new Font("Arial", Font.PLAIN, 10);

        // act
        Font result = FontUtilities.getFallbackFontIfNecessary(text, textSize, primaryFontSpy, fallbackFont);

        // assert
        assertEquals(expectedFont, result.getName());
    }
}

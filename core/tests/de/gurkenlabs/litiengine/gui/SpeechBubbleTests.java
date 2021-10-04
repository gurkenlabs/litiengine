package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Font;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SpeechBubbleTests {

    private IEntity entity;

    @BeforeEach
    void setUp(){
        Game.init(Game.COMMANDLINE_ARG_NOGUI);
        entity = spy(new CombatEntity());
        Environment envMock = mock(Environment.class);
        when(entity.getEnvironment()).thenReturn(envMock);
    }

    @Test
    void testCreateEntityText(){
        // act
        SpeechBubble bubble = SpeechBubble.create(entity, "test");

        // assert
        assertEquals(entity, bubble.getEntity());
    }

    @Test
    void testCreateEntityTextAppreanceFont(){
        // arrange
        Font font = new Font("Times", 10, 10);
        SpeechBubbleAppearance appearance = new SpeechBubbleAppearance();

        // act
        SpeechBubble bubble = SpeechBubble.create(entity, "test", appearance, font);

        // assert
        assertEquals(entity, bubble.getEntity(), "IEntity must match with the supplied instance");
        assertEquals(appearance, bubble.getAppearance(), "Appearance must match with the supplied instance");
        assertEquals(font, bubble.getFont(), "Font must match with the supplied Font");
    }

    @ParameterizedTest
    @MethodSource("getCreateNullArguments")
    void testCreateNullArguments(SpeechBubbleAppearance appearance, Font font, SpeechBubbleAppearance expectedAppearance, Font expectedFont){
        // act
        SpeechBubble bubble = SpeechBubble.create(entity, "test", appearance, font);

        // assert
        assertEquals(expectedAppearance, bubble.getAppearance());
        assertEquals(expectedFont, bubble.getFont());
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> getCreateNullArguments(){
        Font font = new Font("Times", 10, 10);
        SpeechBubbleAppearance appearance = new SpeechBubbleAppearance();

        return Stream.of(
                Arguments.of(null, font, SpeechBubble.DEFAULT_APPEARANCE, font),
                Arguments.of(appearance, null, appearance, null)
        );
    }
}

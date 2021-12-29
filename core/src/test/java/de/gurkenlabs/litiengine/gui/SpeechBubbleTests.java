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

class SpeechBubbleTests {

  private IEntity entity;

  @BeforeEach
  void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    entity = spy(new CombatEntity());
    Environment envMock = mock(Environment.class);
    when(entity.getEnvironment()).thenReturn(envMock);
  }

  @Test
  void testCreateEntityText() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");

    // assert
    assertEquals(entity, bubble.getEntity());
  }

  @Test
  void testCreateEntityTextAppreanceFont() {
    // arrange
    Font font = new Font("Times", Font.BOLD, 10);

    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.setFont(font);

    // assert
    assertEquals(entity, bubble.getEntity(), "IEntity must match with the supplied instance");
    assertEquals(font, bubble.getFont(), "Font must match with the supplied Font");
  }

  @ParameterizedTest
  @MethodSource("getCreateNullArguments")
  void testCreateNullArguments(Font font, Font expectedFont) {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.setFont(font);

    // assert
    assertEquals(expectedFont, bubble.getFont());
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getCreateNullArguments() {
    Font font = new Font("Times", Font.BOLD, 10);

    return Stream.of(Arguments.of(font, font));
  }
}

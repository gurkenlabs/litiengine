package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;

import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Font;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SpeechBubbleTests {

  private IEntity entity;
  private Screen screen;

  @BeforeEach
  void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    entity = spy(new CombatEntity());
    Environment envMock = mock(Environment.class);
    when(entity.getEnvironment()).thenReturn(envMock);
    screen = mock(Screen.class);
    Game.screens().add(screen);
  }

  @Test
  void testCreateEntityText() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");

    // assert
    assertEquals(entity, bubble.getEntity());
  }

  @Test
  void testStartSpeechBubble() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.start();

    // assert
    assertTrue(bubble.isVisible());
    assertFalse(bubble.isSuspended());
  }

  @Test
  void testStopSpeechBubble() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.start();
    bubble.stop();

    // assert
    assertFalse(bubble.isVisible());
    assertTrue(bubble.isSuspended());
    assertFalse(screen.getComponents().contains(bubble));
  }

  @ParameterizedTest
  @MethodSource("getAlignArguments")
  void testSetBoxAlign(Align align) {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.setBoxAlign(align);

    // assert
    assertEquals(bubble.getBoxAlign(), align);
  }

  @Test
  void testShowTriangle() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    // assert
    assertTrue(bubble.isRenderingTriangle());

    // act
    bubble.setRenderTriangle(false);
    // assert
    assertFalse(bubble.isRenderingTriangle());
  }

  @Test
  void testSetTriangleSize() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    // assert
    assertEquals(bubble.getWidth() * 1 / 10d, bubble.getTriangleSize());

    // act
    bubble.setTriangleSize(50);
    // assert
    assertEquals(50, bubble.getTriangleSize());
  }

  @Test
  void testSetTypeDelay() {
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    // assert
    assertEquals((int) (bubble.getDisplayTime() * 0.5 / bubble.getTotalText().length()), bubble.getTypeDelay());

    // act
    bubble.setTypeDelay(400);
    // assert
    assertEquals(400, bubble.getTypeDelay());
  }

  @Test
  void testSetTypeSound() {
    Sound s1 = Resources.sounds().get("de/gurkenlabs/litiengine/resources/bip.ogg");
    Sound s2 = Resources.sounds().get("de/gurkenlabs/litiengine/resources/bop.ogg");
    // act
    SpeechBubble bubble = new SpeechBubble(entity, "test");
    bubble.setTypeSound(s1);
    // assert
    assertEquals(s1, bubble.getTypeSound());
    // act
    bubble.setTypeSound(s2);
    // assert
    assertEquals(s2, bubble.getTypeSound());
    assertNotEquals(s1, s2);
  }

  @Test
  void testCreateEntityTextWithFont() {
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

  @SuppressWarnings("unused")
  private static Stream<Arguments> getAlignArguments() {
    return Stream.of(Arguments.of((Object[]) Align.values()));
  }
}

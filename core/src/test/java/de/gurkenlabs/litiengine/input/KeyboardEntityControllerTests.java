package de.gurkenlabs.litiengine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KeyboardEntityControllerTests {
  @BeforeAll
  public static void initializeKeyboard() {
    // init required Game environment
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // init Keyboard
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

  @ParameterizedTest(name = "handleKeyPressed: {0}")
  @MethodSource("supplyHandleKeyPressedParameters")
  void testHandleKeyPressed(
      String key, int keyCode, char keyChar, int dX, int dY, int resX, int resY) {
    // arrange
    Component source = new TestComponent();
    KeyEvent keyEvent =
        new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
    controller.setDx(dX);
    controller.setDy(dY);

    // act
    controller.handlePressedKey(keyEvent);

    // assert
    assertEquals(resX, controller.getDx());
    assertEquals(resY, controller.getDy());
  }

  @Test
  void addUpKeyAdded() {
    // arrange
    int keyCode = KeyEvent.VK_P;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
    assertFalse(controller.getUpKeys().contains(keyCode));

    // act
    controller.addUpKey(keyCode);

    // assert
    assertTrue(controller.getUpKeys().contains(keyCode));
  }

  @Test
  void addUpKeyContained() {
    // arrange
    int keyCode = KeyEvent.VK_W;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);

    assertTrue(controller.getUpKeys().contains(keyCode));
    assertEquals(1, controller.getUpKeys().size());

    // act
    controller.addUpKey(keyCode);

    // assert
    assertTrue(controller.getUpKeys().contains(keyCode));
    assertEquals(1, controller.getUpKeys().size());
  }

  @Test
  void addDownKeyAdded() {
    // arrange
    int keyCode = KeyEvent.VK_P;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
    assertFalse(controller.getDownKeys().contains(keyCode));

    // act
    controller.addDownKey(keyCode);

    // assert
    assertTrue(controller.getDownKeys().contains(keyCode));
  }

  @Test
  void addDownKeyContained() {
    // arrange
    int keyCode = KeyEvent.VK_S;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);

    assertTrue(controller.getDownKeys().contains(keyCode));
    assertEquals(1, controller.getDownKeys().size());

    // act
    controller.addDownKey(keyCode);

    // assert
    assertTrue(controller.getDownKeys().contains(keyCode));
    assertEquals(1, controller.getDownKeys().size());
  }

  @Test
  void addLeftKeyAdded() {
    // arrange
    int keyCode = KeyEvent.VK_P;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
    assertFalse(controller.getLeftKeys().contains(keyCode));

    // act
    controller.addLeftKey(keyCode);

    // assert
    assertTrue(controller.getLeftKeys().contains(keyCode));
  }

  @Test
  void addLeftKeyContained() {
    // arrange
    int keyCode = KeyEvent.VK_A;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);

    assertTrue(controller.getLeftKeys().contains(keyCode));
    assertEquals(1, controller.getLeftKeys().size());

    // act
    controller.addLeftKey(keyCode);

    // assert
    assertTrue(controller.getLeftKeys().contains(keyCode));
    assertEquals(1, controller.getLeftKeys().size());
  }

  @Test
  void addRightKeyAdded() {
    // arrange
    int keyCode = KeyEvent.VK_P;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
    assertFalse(controller.getRightKeys().contains(keyCode));

    // act
    controller.addRightKey(keyCode);

    // assert
    assertTrue(controller.getRightKeys().contains(keyCode));
  }

  @Test
  void addRightKeyContained() {
    // arrange
    int keyCode = KeyEvent.VK_D;

    Creature entity = new Creature();
    KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);

    assertTrue(controller.getRightKeys().contains(keyCode));
    assertEquals(1, controller.getRightKeys().size());

    // act
    controller.addRightKey(keyCode);

    // assert
    assertTrue(controller.getRightKeys().contains(keyCode));
    assertEquals(1, controller.getRightKeys().size());
  }

  private class TestComponent extends Component {}

  private static Stream<Arguments> supplyHandleKeyPressedParameters() {
    return Stream.of(
        Arguments.of("Up", KeyEvent.VK_W, "W", 1, 1, 1, 0),
        Arguments.of("Down", KeyEvent.VK_S, "S", 0, 0, 0, 1),
        Arguments.of("Left", KeyEvent.VK_A, "A", 1, 1, 0, 1),
        Arguments.of("Right", KeyEvent.VK_D, "D", 0, 0, 1, 0),
        Arguments.of("Other", KeyEvent.VK_P, "P", 1, 1, 1, 1));
  }
}

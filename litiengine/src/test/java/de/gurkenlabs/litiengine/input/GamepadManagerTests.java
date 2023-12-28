package de.gurkenlabs.litiengine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GamepadManagerTests {
  private GamepadManager gamepadManager;

  @BeforeEach
  public void setUp() {
    gamepadManager = new GamepadManager();
  }

  @Test
  void testRemoveAddedListener() {
    GamepadManager.GamepadAddedListener listener = mock(GamepadManager.GamepadAddedListener.class);
    gamepadManager.onAdded(listener);

    gamepadManager.removeAddedListener(listener);

    Gamepad gamepad = mock(Gamepad.class);
    gamepadManager.getAll().add(gamepad);

    verify(listener, never()).added(gamepad);
  }

  @Test
  void testRemoveRemovedListener() {
    GamepadManager.GamepadRemovedListener listener = mock(GamepadManager.GamepadRemovedListener.class);
    gamepadManager.onRemoved(listener);

    gamepadManager.removeRemovedListener(listener);

    Gamepad gamepad = mock(Gamepad.class);
    gamepadManager.getAll().add(gamepad);
    gamepadManager.getAll().remove(gamepad);

    verify(listener, never()).removed(gamepad);
  }

  @Test
  void testGetAll() {
    Gamepad gamepad1 = mock(Gamepad.class);
    Gamepad gamepad2 = mock(Gamepad.class);

    gamepadManager.getAll().add(gamepad1);
    gamepadManager.getAll().add(gamepad2);

    List<Gamepad> allGamepads = gamepadManager.getAll();

    assertEquals(2, allGamepads.size());
    assertTrue(allGamepads.contains(gamepad1));
    assertTrue(allGamepads.contains(gamepad2));
  }

  @Test
  void testCurrent() {
    Gamepad gamepad = mock(Gamepad.class);
    gamepadManager.getAll().add(gamepad);

    assertEquals(gamepad, gamepadManager.current());
  }

  @Test
  void testGet() {
    Gamepad gamepad1 = mock(Gamepad.class);
    Gamepad gamepad2 = mock(Gamepad.class);

    gamepadManager.getAll().add(gamepad1);
    gamepadManager.getAll().add(gamepad2);

    assertEquals(gamepad1, gamepadManager.get(0));
    assertEquals(gamepad2, gamepadManager.get(1));
  }

  @Test
  void testGetById() {
    Gamepad gamepad1 = mock(Gamepad.class);
    Gamepad gamepad2 = mock(Gamepad.class);

    when(gamepad1.getId()).thenReturn(1);
    when(gamepad2.getId()).thenReturn(2);

    gamepadManager.getAll().add(gamepad1);
    gamepadManager.getAll().add(gamepad2);

    assertEquals(gamepad1, gamepadManager.getById(1));
    assertEquals(gamepad2, gamepadManager.getById(2));
    assertNull(gamepadManager.getById(3));
  }
}

package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;


public class KeyboardEntityControllerTests {
    @BeforeAll
    public static void initializeKeyboard() {
        // init required Game environment
        Game.init(Game.COMMADLINE_ARG_NOGUI);

        // init Keyboard
        Input.InputGameAdapter adapter = new Input.InputGameAdapter();
        adapter.initialized();
    }

    @Test
    public void handleUpKeyPressed() {
        // arrange
        int keyCode = KeyEvent.VK_W;
        char keyChar = 'W';
        Component source = new TestComponent();
        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

        Creature entity = new Creature();
        KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
        controller.setDx(1);
        controller.setDy(1);

        // act
        controller.handlePressedKey(keyEvent);

        // assert
        assertEquals(1, controller.getDx()); // =
        assertEquals(0, controller.getDy()); // +1
    }

    @Test
    public void handleDownKeyPressed() {
        // arrange
        int keyCode = KeyEvent.VK_S;
        char keyChar = 'S';
        Component source = new TestComponent();
        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

        Creature entity = new Creature();
        KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
        controller.setDx(0);
        controller.setDy(0);

        // act
        controller.handlePressedKey(keyEvent);

        // assert
        assertEquals(0, controller.getDx()); // =
        assertEquals(1, controller.getDy()); // -1
    }

    @Test
    public void handleLeftKeyPressed() {
        // arrange
        int keyCode = KeyEvent.VK_A;
        char keyChar = 'A';
        Component source = new TestComponent();
        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

        Creature entity = new Creature();
        KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
        controller.setDx(1);
        controller.setDy(1);

        // act
        controller.handlePressedKey(keyEvent);

        // assert
        assertEquals(0, controller.getDx()); // -1
        assertEquals(1, controller.getDy()); // =
    }

    @Test
    public void handleRightKeyPressed() {
        // arrange
        int keyCode = KeyEvent.VK_D;
        char keyChar = 'D';
        Component source = new TestComponent();
        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

        Creature entity = new Creature();
        KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
        controller.setDx(0);
        controller.setDy(0);

        // act
        controller.handlePressedKey(keyEvent);

        // assert
        assertEquals(1, controller.getDx()); // +1
        assertEquals(0, controller.getDy()); // =
    }

    @Test
    public void unchangedForOtherKeyPressed() {
        // arrange
        int keyCode = KeyEvent.VK_P;
        char keyChar = 'P';
        Component source = new TestComponent();
        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);

        Creature entity = new Creature();
        KeyboardEntityController<Creature> controller = new KeyboardEntityController<>(entity);
        controller.setDx(1);
        controller.setDy(1);

        // act
        controller.handlePressedKey(keyEvent);

        // assert
        assertEquals(1, controller.getDx()); // =
        assertEquals(1, controller.getDy()); // =
    }

    @Test
    public void addUpKeyAdded() {
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
    public void addUpKeyContained() {
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
    public void addDownKeyAdded() {
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
    public void addDownKeyContained() {
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
    public void addLeftKeyAdded() {
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
    public void addLeftKeyContained() {
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
    public void addRightKeyAdded() {
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
    public void addRightKeyContained() {
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

    private class TestComponent extends Component {
    }
}

package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
    public void testHandleUpKeyPressed() {
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
    public void testHandleDownKeyPressed() {
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
    public void testHandleLeftKeyPressed() {
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
    public void testHandleRightKeyPressed() {
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

    private class TestComponent extends Component {
    }
}

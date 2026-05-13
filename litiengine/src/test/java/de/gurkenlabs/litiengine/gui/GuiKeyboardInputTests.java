package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Keyboard;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class GuiKeyboardInputTests {
  @BeforeAll
  static void init() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    new Input.InputGameAdapter().initialized();
  }

  @BeforeEach
  void setup() {
    assertTrue(SwingUtilities.isEventDispatchThread());
    Input.keyboard().clearExplicitListeners();
    GuiComponent focused = GuiComponent.getFocusedComponent();
    if (focused != null) {
      focused.setSelected(false);
    }
  }

  @Test
  void focusedGuiComponentIsTrackedGlobally() {
    CheckBox first = new CheckBox(0, 0, 10, 10, null, false);
    CheckBox second = new CheckBox(0, 0, 10, 10, null, false);

    first.setSelected(true);
    assertTrue(first.hasInputFocus());
    assertEquals(first, GuiComponent.getFocusedComponent());

    second.setSelected(true);
    assertFalse(first.isSelected());
    assertTrue(second.hasInputFocus());
    assertEquals(second, GuiComponent.getFocusedComponent());
  }

  @Test
  void menuSupportsKeyboardSelection() {
    Menu menu = new Menu(0, 0, 100, 100, Orientation.VERTICAL, "A", "B", "C");
    menu.prepare();
    menu.setSelected(true);

    pressKey(KeyEvent.VK_DOWN);
    assertEquals(1, menu.getCurrentSelection());

    pressKey(KeyEvent.VK_UP);
    assertEquals(0, menu.getCurrentSelection());
  }

  @Test
  void sliderSupportsKeyboardInput() {
    HorizontalSlider slider = new HorizontalSlider(0, 0, 100, 10, 0, 3, 1);
    slider.prepare();
    slider.setSelected(true);

    pressKey(KeyEvent.VK_RIGHT);
    assertEquals(1f, slider.getCurrentValue());

    pressKey(KeyEvent.VK_LEFT);
    assertEquals(0f, slider.getCurrentValue());
  }

  @Test
  void checkBoxCanBeTriggeredByKeyboard() {
    CheckBox checkBox = new CheckBox(0, 0, 10, 10, null, false);
    checkBox.prepare();
    checkBox.setSelected(true);

    pressKey(KeyEvent.VK_ENTER);
    assertTrue(checkBox.isChecked());
  }

  @Test
  void listFieldAndSpinnerSupportKeyboardInput() {
    ListField listField = new ListField(0, 0, 100, 50, new String[] {"A", "B", "C"}, 3);
    listField.prepare();
    listField.setArrowKeyNavigation(true);
    listField.setSelection(0, 0);
    listField.setSelected(true);

    pressKey(KeyEvent.VK_DOWN);
    assertEquals(1, listField.getSelectionRow());

    Spinner spinner = new Spinner(0, 0, 100, 30, 0, 10, 1, 1);
    spinner.prepare();
    spinner.setSelected(true);

    pressKey(KeyEvent.VK_UP);
    assertEquals(new BigDecimal("2.0"), spinner.getCurrentValue());
  }

  private static void pressKey(final int keyCode) {
    Keyboard keyboard = (Keyboard) Input.keyboard();
    KeyEvent event =
        new KeyEvent(
            new JLabel(),
            KeyEvent.KEY_RELEASED,
            System.currentTimeMillis(),
            0,
            keyCode,
            KeyEvent.CHAR_UNDEFINED);
    keyboard.dispatchKeyEvent(event);
    keyboard.update();
  }
}

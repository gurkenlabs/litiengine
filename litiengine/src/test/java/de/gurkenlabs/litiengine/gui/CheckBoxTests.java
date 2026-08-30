package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class CheckBoxTests {
  @BeforeAll
  static void initialize() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

  @Test
  void testSetChecked() {
    // arrange
    CheckBox box = new CheckBox(1, 2, 3, 4, null, false);

    assertFalse(box.isChecked());
    assertEquals(CheckBox.CROSS.getText(), box.getText());

    // act
    box.setChecked(true);

    // assert
    assertTrue(box.isChecked());
    assertEquals(CheckBox.CHECK.getText(), box.getText());
  }

  @Test
  void testSetUnchecked() {
    // arrange
    CheckBox box = new CheckBox(1, 2, 3, 4, null, true);

    assertTrue(box.isChecked());
    assertEquals(CheckBox.CHECK.getText(), box.getText());

    // act
    box.setChecked(false);

    // assert
    assertFalse(box.isChecked());
    assertEquals(CheckBox.CROSS.getText(), box.getText());
  }
}

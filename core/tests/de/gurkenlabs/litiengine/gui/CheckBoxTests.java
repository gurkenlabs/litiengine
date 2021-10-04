package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CheckBoxTests {
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

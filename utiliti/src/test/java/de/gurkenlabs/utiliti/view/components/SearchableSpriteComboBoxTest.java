package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.Test;

class SearchableSpriteComboBoxTest {

  @Test
  void editorPreservesMatchingLabelSelection() {
    SearchableSpriteComboBox comboBox = new SearchableSpriteComboBox();
    JLabel rooster = new JLabel("rooster");
    comboBox.addItem(rooster);
    comboBox.setSelectedItem(rooster);

    ((JTextComponent) comboBox.getEditor().getEditorComponent()).setText("rooster");

    assertSame(rooster, comboBox.getEditor().getItem());
    assertEquals("rooster", SearchableSpriteComboBox.selectedText(comboBox));
  }
}

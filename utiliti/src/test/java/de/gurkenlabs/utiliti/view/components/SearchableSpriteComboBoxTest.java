package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
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

  @Test
  void filtersByMultipleTermsWithoutCommittingFirstMatch() {
    SearchableSpriteComboBox comboBox = comboBox(
      "decormob-butterflyred",
      "decormob-butterflyblue",
      "decor-bluechair");
    AtomicInteger actions = new AtomicInteger();
    comboBox.addActionListener(event -> actions.incrementAndGet());

    comboBox.filterForTest("butter blue");

    assertEquals(1, comboBox.getItemCount());
    assertEquals("decormob-butterflyblue", comboBox.getItemAt(0).getText());
    assertNull(comboBox.getSelectedItem());
    assertNull(SearchableSpriteComboBox.selectedText(comboBox));
    assertEquals(0, actions.get());
    assertEquals(
      "butter blue",
      ((JTextComponent) comboBox.getEditor().getEditorComponent()).getText());
  }

  @Test
  void typingInEditorFiltersWithoutChangingSelection() throws Exception {
    SearchableSpriteComboBox comboBox = comboBox(
      "decormob-butterflyred",
      "decormob-butterflyblue",
      "decor-bluechair");
    AtomicInteger actions = new AtomicInteger();
    comboBox.addActionListener(event -> actions.incrementAndGet());
    JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();

    SwingUtilities.invokeAndWait(() -> editor.setText("butterflyblue"));
    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(1, comboBox.getItemCount());
    assertEquals("decormob-butterflyblue", comboBox.getItemAt(0).getText());
    assertNull(comboBox.getSelectedItem());
    assertEquals(0, actions.get());
  }

  @Test
  void disablesDefaultJumpToFirstPrefixMatch() {
    SearchableSpriteComboBox comboBox = comboBox(
      "decormob-butterflyred",
      "decormob-butterflyblue");

    assertFalse(comboBox.selectWithKeyChar('d'));
    assertNull(comboBox.getSelectedItem());
  }

  @Test
  void enterCommitsBestMatchAndRestoresCompleteList() {
    SearchableSpriteComboBox comboBox = comboBox(
      "blue-crate",
      "crate-blue",
      "crate-red");
    AtomicInteger actions = new AtomicInteger();
    comboBox.addActionListener(event -> actions.incrementAndGet());

    comboBox.filterForTest("crate");
    comboBox.commitEditorTextForTest();

    assertEquals("crate-blue", SearchableSpriteComboBox.selectedText(comboBox));
    assertEquals(3, comboBox.getItemCount());
    assertEquals(1, actions.get());
  }

  @Test
  void ranksExactAndPrefixMatchesBeforeContainsMatches() {
    List<JLabel> matches = SearchableSpriteComboBox.matchingItems(
      List.of(
        new JLabel("large-blue-crate"),
        new JLabel("crate"),
        new JLabel("crate-blue")),
      "crate");

    assertEquals(
      List.of("crate", "crate-blue", "large-blue-crate"),
      matches.stream().map(JLabel::getText).toList());
  }

  private static SearchableSpriteComboBox comboBox(String... values) {
    SearchableSpriteComboBox comboBox = new SearchableSpriteComboBox();
    for (String value : values) {
      comboBox.addItem(new JLabel(value));
    }
    return comboBox;
  }
}

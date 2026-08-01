package de.gurkenlabs.utiliti.view.components;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

/**
 * Editable sprite selector that filters its popup without committing a value while the user types.
 *
 * <p>Matches are case-insensitive, support multiple whitespace-separated terms, and rank exact and
 * prefix matches ahead of general substring matches. Enter commits the first result; Escape restores
 * the previously committed sprite.
 */
final class SearchableSpriteComboBox extends JComboBox<JLabel> {
  private final List<JLabel> allItems = new ArrayList<>();
  private final JTextComponent editorComponent;
  private boolean adjusting;
  private boolean filterPending;
  private JLabel committedItem;

  SearchableSpriteComboBox() {
    setEditable(true);
    setKeySelectionManager((key, model) -> -1);
    setEditor(new SpriteComboBoxEditor());
    this.editorComponent = (JTextComponent) getEditor().getEditorComponent();
    this.editorComponent
      .getDocument()
      .addDocumentListener(
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent event) {
            scheduleFilter();
          }

          @Override
          public void removeUpdate(DocumentEvent event) {
            scheduleFilter();
          }

          @Override
          public void changedUpdate(DocumentEvent event) {
            scheduleFilter();
          }
        });
    this.editorComponent.addKeyListener(
      new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent event) {
          if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            commitEditorText();
            event.consume();
          } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelSearch();
            event.consume();
          }
        }
      });
    this.editorComponent.addFocusListener(
      new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent event) {
          SwingUtilities.invokeLater(
            () -> {
              if (!editorComponent.isFocusOwner() && !isPopupVisible()) {
                cancelSearch();
              }
            });
        }
      });
  }

  @Override
  public void addItem(JLabel item) {
    if (item == null) {
      return;
    }

    this.allItems.add(item);
    runAdjusting(
      () -> {
        Object selected = super.getSelectedItem();
        super.addItem(item);
        super.setSelectedItem(selected);
      });
  }

  @Override
  public void insertItemAt(JLabel item, int index) {
    if (item == null) {
      return;
    }

    int targetIndex = Math.max(0, Math.min(index, this.allItems.size()));
    this.allItems.add(targetIndex, item);
    runAdjusting(
      () -> {
        Object selected = super.getSelectedItem();
        super.insertItemAt(item, Math.max(0, Math.min(index, getItemCount())));
        super.setSelectedItem(selected);
      });
  }

  @Override
  public void removeItem(Object item) {
    this.allItems.remove(item);
    if (item == this.committedItem) {
      this.committedItem = null;
    }
    runAdjusting(() -> super.removeItem(item));
  }

  @Override
  public void removeItemAt(int index) {
    JLabel item = getItemAt(index);
    this.allItems.remove(item);
    if (item == this.committedItem) {
      this.committedItem = null;
    }
    runAdjusting(() -> super.removeItemAt(index));
  }

  @Override
  public void removeAllItems() {
    this.allItems.clear();
    this.committedItem = null;
    runAdjusting(super::removeAllItems);
    setEditorText("");
  }

  @Override
  public void setSelectedItem(Object item) {
    if (this.adjusting) {
      super.setSelectedItem(item);
      return;
    }

    if (item instanceof JLabel label && this.allItems.contains(label)) {
      commit(label, true);
    } else if (item == null) {
      this.committedItem = null;
      restoreAllItems(null);
      setEditorText("");
      super.fireActionEvent();
    }
  }

  @Override
  protected void fireActionEvent() {
    if (!this.adjusting) {
      super.fireActionEvent();
    }
  }

  void resetFilter() {
    restoreAllItems(this.committedItem);
    setEditorText(this.committedItem != null ? this.committedItem.getText() : "");
  }

  private void scheduleFilter() {
    if (this.adjusting || this.filterPending) {
      return;
    }

    this.filterPending = true;
    SwingUtilities.invokeLater(
      () -> {
        this.filterPending = false;
        if (!this.adjusting) {
          applyFilter(this.editorComponent.getText(), true);
        }
      });
  }

  private void applyFilter(String query, boolean updatePopup) {
    List<JLabel> matches = matchingItems(this.allItems, query);
    String editorText = query == null ? "" : query;
    int caret = Math.min(this.editorComponent.getCaretPosition(), editorText.length());

    runAdjusting(
      () -> {
        super.removeAllItems();
        for (JLabel item : matches) {
          super.addItem(item);
        }
        super.setSelectedItem(null);
      });
    setEditorText(editorText);
    this.editorComponent.setCaretPosition(caret);

    if (!updatePopup || !this.editorComponent.isFocusOwner() || !isShowing()) {
      return;
    }
    if (matches.isEmpty()) {
      hidePopup();
    } else {
      showPopup();
    }
  }

  private void commitEditorText() {
    String query = this.editorComponent.getText();
    JLabel item =
      this.allItems.stream()
        .filter(candidate -> candidate.getText().equalsIgnoreCase(query.trim()))
        .findFirst()
        .orElseGet(() -> getItemCount() > 0 ? getItemAt(0) : null);
    if (item != null) {
      commit(item, true);
    }
    hidePopup();
  }

  private void commit(JLabel item, boolean fireAction) {
    this.committedItem = item;
    restoreAllItems(item);
    setEditorText(item.getText());
    if (fireAction) {
      super.fireActionEvent();
    }
  }

  private void cancelSearch() {
    restoreAllItems(this.committedItem);
    setEditorText(this.committedItem != null ? this.committedItem.getText() : "");
    hidePopup();
  }

  private void restoreAllItems(JLabel selection) {
    runAdjusting(
      () -> {
        super.removeAllItems();
        for (JLabel item : this.allItems) {
          super.addItem(item);
        }
        super.setSelectedItem(selection);
      });
  }

  private void setEditorText(String text) {
    runAdjusting(
      () -> {
        this.editorComponent.setText(text == null ? "" : text);
        this.editorComponent.setCaretPosition(this.editorComponent.getDocument().getLength());
      });
  }

  private void runAdjusting(Runnable action) {
    boolean wasAdjusting = this.adjusting;
    this.adjusting = true;
    try {
      action.run();
    } finally {
      this.adjusting = wasAdjusting;
    }
  }

  static List<JLabel> matchingItems(List<JLabel> items, String query) {
    String normalizedQuery = normalize(query);
    if (normalizedQuery.isEmpty()) {
      return List.copyOf(items);
    }

    String[] terms = normalizedQuery.split("\\s+");
    return items.stream()
      .filter(item -> matchesAllTerms(normalize(item.getText()), terms))
      .sorted(
        Comparator.comparingInt((JLabel item) -> matchRank(normalize(item.getText()), normalizedQuery))
          .thenComparing(JLabel::getText, String.CASE_INSENSITIVE_ORDER))
      .toList();
  }

  private static boolean matchesAllTerms(String value, String[] terms) {
    for (String term : terms) {
      if (!value.contains(term)) {
        return false;
      }
    }
    return true;
  }

  private static int matchRank(String value, String query) {
    if (value.equals(query)) {
      return 0;
    }
    if (value.startsWith(query)) {
      return 1;
    }
    return 2;
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  void filterForTest(String query) {
    applyFilter(query, false);
  }

  void commitEditorTextForTest() {
    commitEditorText();
  }

  private final class SpriteComboBoxEditor extends BasicComboBoxEditor {
    private Object currentItem;

    @Override
    public void setItem(Object item) {
      if (adjusting) {
        return;
      }
      this.currentItem = item;
      runAdjusting(
        () ->
          this.editor.setText(
            item instanceof JLabel label
              ? label.getText()
              : item == null ? "" : item.toString()));
    }

    @Override
    public Object getItem() {
      String text = this.editor.getText();
      if (this.currentItem instanceof JLabel label && label.getText().equals(text)) {
        return this.currentItem;
      }
      return allItems.stream()
        .filter(item -> item.getText().equalsIgnoreCase(text))
        .findFirst()
        .orElse(null);
    }
  }

  static String selectedText(JComboBox<?> comboBox) {
    Object selected = comboBox.getSelectedItem();
    if (selected instanceof JLabel label) {
      return label.getText();
    }
    return selected instanceof String text && !text.isBlank() ? text : null;
  }
}

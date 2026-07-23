package de.gurkenlabs.utiliti.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

/** Sprite selector with type-to-filter support while preserving preview labels. */
final class SearchableSpriteComboBox extends JComboBox<JLabel> {
  private final List<JLabel> items = new ArrayList<>();
  private boolean filtering;

  SearchableSpriteComboBox() {
    setEditable(true);
    setEditor(new BasicComboBoxEditor() {
      private Object currentItem;

      @Override public void setItem(Object item) {
        this.currentItem = item;
        this.editor.setText(item instanceof JLabel label ? label.getText() : item == null ? "" : item.toString());
      }

      @Override public Object getItem() {
        String text = this.editor.getText();
        if (this.currentItem instanceof JLabel label && label.getText().equals(text)) {
          return this.currentItem;
        }
        return items.stream().filter(item -> item.getText().equals(text)).findFirst().orElse(null);
      }
    });
    ((JTextComponent) getEditor().getEditorComponent()).addKeyListener(new java.awt.event.KeyAdapter() {
      @Override public void keyReleased(java.awt.event.KeyEvent e) {
        javax.swing.SwingUtilities.invokeLater(SearchableSpriteComboBox.this::filter);
      }
    });
  }

  @Override public void addItem(JLabel item) {
    if (!this.filtering) this.items.add(item);
    super.addItem(item);
  }

  @Override public void removeAllItems() {
    if (!this.filtering) this.items.clear();
    super.removeAllItems();
  }

  private void filter() {
    if (this.filtering) return;
    String query = ((JTextComponent) getEditor().getEditorComponent()).getText().trim().toLowerCase(Locale.ROOT);
    this.filtering = true;
    super.removeAllItems();
    for (JLabel item : this.items) if (query.isEmpty() || item.getText().toLowerCase(Locale.ROOT).contains(query)) super.addItem(item);
    this.filtering = false;
    if (getItemCount() > 0) showPopup();
  }

  static String selectedText(JComboBox<?> comboBox) {
    Object selected = comboBox.getSelectedItem();
    if (selected instanceof JLabel label) {
      return label.getText();
    }
    return selected instanceof String text && !text.isBlank() ? text : null;
  }
}

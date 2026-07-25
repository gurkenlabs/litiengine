package de.gurkenlabs.utiliti.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

final class SearchableResourceComboBox extends JComboBox<String> {
  private final List<String> items = new ArrayList<>();
  private boolean filtering;

  SearchableResourceComboBox() {
    setEditable(true);
    ((JTextComponent) getEditor().getEditorComponent()).addKeyListener(new java.awt.event.KeyAdapter() {
      @Override public void keyReleased(java.awt.event.KeyEvent e) {
        javax.swing.SwingUtilities.invokeLater(SearchableResourceComboBox.this::filter);
      }
    });
  }

  @Override public void addItem(String item) { if (!this.filtering) this.items.add(item); super.addItem(item); }
  @Override public void removeAllItems() { if (!this.filtering) this.items.clear(); super.removeAllItems(); }

  private void filter() {
    if (this.filtering) return;
    String query = ((JTextComponent) getEditor().getEditorComponent()).getText().trim().toLowerCase(Locale.ROOT);
    this.filtering = true;
    super.removeAllItems();
    for (String item : this.items) if (query.isEmpty() || item.toLowerCase(Locale.ROOT).contains(query)) super.addItem(item);
    this.filtering = false;
    if (getItemCount() > 0) showPopup();
  }
}

package de.gurkenlabs.utiliti.view.components;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JTextArea;

public class RowLimitedTextArea extends JTextArea {

  public RowLimitedTextArea(int rows, int columns) {
    super(rows, columns);
  }

  @Override
  public Dimension getPreferredSize() {
    Insets insets = getInsets();
    return new Dimension(
        getColumns() * getColumnWidth() + insets.left + insets.right,
        getRows() * getRowHeight() + insets.top + insets.bottom);
  }
}

package de.gurkenlabs.utiliti.swing;

import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Insets;

public class RowLimitedTextArea extends JTextArea {

    public RowLimitedTextArea(int rows, int columns) {
        super(rows, columns);
    }

    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        return new Dimension(
                getColumns() * getColumnWidth() + insets.left + insets.right,
                getRows() * getRowHeight() + insets.top + insets.bottom);
    }
}
